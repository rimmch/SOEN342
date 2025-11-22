package parser;

import model.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVRouteParser {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    // Parse time strings like "08:29" or "08:29 (+1d)" (next-day arrival)
    private LocalTime parseTime(String raw) {
        String cleaned = raw.trim();
        int spaceIdx = cleaned.indexOf(' ');
        if (spaceIdx > 0) {
            // Drop everything after the first space, e.g. "08:29 (+1d)" -> "08:29"
            cleaned = cleaned.substring(0, spaceIdx);
        }
        return LocalTime.parse(cleaned, TIME_FORMATTER);
    }

    // Parse prices that may contain non-numeric characters, e.g. "T 79.90", "EUR 45.00"
    private Money parsePrice(String raw, String currency, int lineNo, String fieldName) {
        String cleaned = raw == null ? "" : raw.trim();

        // Remove everything that is not a digit, decimal separator, minus sign, or comma
        cleaned = cleaned.replaceAll("[^0-9,.-]", "");

        // Convert comma decimal separators to dot
        cleaned = cleaned.replace(',', '.');

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + " price '" + raw + "' on line " + lineNo);
        }

        return new Money(new BigDecimal(cleaned), currency);
    }

    public List<Route> parseRoutes(String csvFilePath) throws IOException {
        List<Route> routes = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilePath))) {
            String header = reader.readLine(); // skip header
            if (header == null) return routes;

            String line;
            int lineNo = 1;

            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;

                Route route = parseRouteLine(line, lineNo);
                if (route != null) {
                    routes.add(route);
                }
            }
        }

        return routes;
    }

    private Route parseRouteLine(String line, int lineNo) {
        try {
            // Naive CSV split (we manually fix the days column when it has commas).
            String[] rawFields = line.split(",", -1);

            // Expected minimal layout:
            // 0: routeId
            // 1: depCity
            // 2: arrCity
            // 3: depTime
            // 4: arrTime
            // 5: trainType
            // ... one or more fields for days ...
            // last-2: first class price
            // last-1: second class price
            if (rawFields.length < 8) {
                System.err.println("Line " + lineNo + " skipped: expected at least 8 columns.");
                return null;
            }

            String routeId      = rawFields[0].trim();
            String depCity      = rawFields[1].trim();
            String arrCity      = rawFields[2].trim();
            String depTimeStr   = rawFields[3].trim();
            String arrTimeStr   = rawFields[4].trim();
            String trainTypeStr = rawFields[5].trim();

            // Prices are always the last two fields in the row.
            int firstPriceIndex  = rawFields.length - 2;
            int secondPriceIndex = rawFields.length - 1;

            String firstPriceStr  = rawFields[firstPriceIndex].trim();
            String secondPriceStr = rawFields[secondPriceIndex].trim();

            // Everything between index 6 (inclusive) and firstPriceIndex (exclusive)
            // belongs to the "days" column (may itself contain commas, e.g. "Mon,Wed,Fri").
            StringBuilder daysBuilder = new StringBuilder();
            for (int i = 6; i < firstPriceIndex; i++) {
                String part = rawFields[i].trim();
                if (part.isEmpty()) continue;
                if (daysBuilder.length() > 0) {
                    daysBuilder.append(",");
                }
                daysBuilder.append(part);
            }
            String daysStr = daysBuilder.toString();

            // Build Station objects
            Station departureStation = makeStation(depCity);
            Station arrivalStation   = makeStation(arrCity);

            // Times (handle values like "08:29 (+1d)")
            LocalTime departureTime = parseTime(depTimeStr);
            LocalTime arrivalTime   = parseTime(arrTimeStr);

            // Train type (with robust parsing / fallback)
            TrainType trainType = parseTrainType(trainTypeStr, lineNo);

            // Prices (clean up possible prefixes or currency symbols)
            Money priceFirst  = parsePrice(firstPriceStr, "EUR", lineNo, "first-class");
            Money priceSecond = parsePrice(secondPriceStr, "EUR", lineNo, "second-class");

            // Days pattern
            DaySet dayPattern = parseDayPattern(daysStr);

            // Build Route
            return new Route(
                    routeId,
                    departureStation,
                    arrivalStation,
                    departureTime,
                    arrivalTime,
                    trainType,
                    priceFirst,
                    priceSecond,
                    dayPattern
            );

        } catch (Exception e) {
            System.err.println("Error on line " + lineNo + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse train type in a robust way, handling unknown or slightly different labels.
     */
    private TrainType parseTrainType(String raw, int lineNo) {
        String normalized = raw == null ? "" : raw.trim().toUpperCase().replace(" ", "_");

        // Try direct match first
        try {
            if (!normalized.isEmpty()) {
                return TrainType.valueOf(normalized);
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to custom mappings / fallback
        }

        // Handle common special cases / synonyms explicitly
        if ("NIGHTJET".equals(normalized) || "NJ".equals(normalized)) {
            // Try to find a "night" related train type if it exists
            for (TrainType t : TrainType.values()) {
                String name = t.name().toUpperCase();
                if (name.contains("NIGHT") || name.contains("NJ")) {
                    return t;
                }
            }
        }

        // As a last resort, log and fall back to the first defined enum constant
        System.err.println("Unknown train type '" + raw + "' on line " + lineNo
                + " — defaulting to " + TrainType.values()[0]);
        return TrainType.values()[0];
    }

    private Station makeStation(String cityName) {
        String name = cityName;
        String city = cityName;
        String country = "Unknown";
        String code = generateStationCode(cityName);
        return new Station(name, city, country, code);
    }

    // Generate 3-letter station code
    private String generateStationCode(String stationName) {
        String letters = stationName.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) return "XXX";
        return letters.substring(0, Math.min(3, letters.length()));
    }

    private DaySet parseDayPattern(String token) {
        // Handle missing or empty patterns as "no service"
        if (token == null) {
            return new DaySet(0);
        }

        token = token.trim();
        if (token.isEmpty()) {
            return new DaySet(0);
        }

        // Remove any double quotes that may come from CSV like "Mon,Wed,Fri"
        token = token.replace("\"", "");

        String lower = token.toLowerCase();

        // Common textual patterns
        if (lower.equals("daily")) {
            // All days: Mon-Sun
            return new DaySet(0b1111111);
        }

        if (lower.startsWith("weekday")) {
            // Weekdays: Mon-Fri
            int mask = 0;
            for (int i = 0; i <= 4; i++) {
                mask |= (1 << i);
            }
            return new DaySet(mask);
        }

        if (lower.startsWith("weekend")) {
            // Weekends: Sat-Sun
            int mask = 0;
            mask |= (1 << 5); // Sat
            mask |= (1 << 6); // Sun
            return new DaySet(mask);
        }

        // Binary day mask "1111100"
        if (token.matches("[01]{7}")) {
            return new DaySet(Integer.parseInt(token, 2));
        }

        // Remove all whitespace
        token = token.replaceAll("\\s+", "");

        // Range: "Fri-Sun" or "Mon-Fri"
        if (token.contains("-")) {
            String[] p = token.split("-");
            int start = dayIndex(p[0]);
            int end   = dayIndex(p[1]);

            int mask = 0;
            for (int i = start; i <= end; i++) {
                mask |= (1 << i);
            }
            return new DaySet(mask);
        }

        // List: "Mon,Wed,Fri"
        int mask = 0;
        for (String part : token.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            mask |= (1 << dayIndex(p));
        }

        return new DaySet(mask);
    }

    // Monday=0 … Sunday=6, 3-letter abbreviations only
    private int dayIndex(String d) {
        if (d == null) {
            throw new IllegalArgumentException("Invalid day: null");
        }

        // Trim, lower-case, and remove quotes
        String cleaned = d.trim().toLowerCase().replace("\"", "");

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Invalid day: " + d);
        }

        // Normalise long names like "monday" -> "mon"
        if (cleaned.length() > 3) {
            cleaned = cleaned.substring(0, 3);
        }

        switch (cleaned) {
            case "mon":
                return 0;
            case "tue":
                return 1;
            case "wed":
                return 2;
            case "thu":
                return 3;
            case "fri":
                return 4;
            case "sat":
                return 5;
            case "sun":
                return 6;
            default:
                throw new IllegalArgumentException("Invalid day: " + d);
        }
    }
}