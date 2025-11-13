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

// TO READ CSV FILE - ITERATION 1
public class CSVRouteParser {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    
    public List<Route> parseRoutes(String csvFilePath) throws IOException {
        List<Route> routes = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilePath))) {
            String header = reader.readLine(); // skip header line
            if (header == null) {
                return routes; // empty file
            }

            String line;
            int lineNo = 1;   // header = line 1
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
            String[] fields = line.split(",", -1);  // keep empty fields

            if (fields.length < 9) {
                System.err.println("Skipping line " + lineNo +
                                   " (expected at least 9 columns, got " + fields.length + ")");
                return null;
            }

            String routeId       = fields[0].trim();
            String depCity       = fields[1].trim();
            String arrCity       = fields[2].trim();
            String depTimeStr    = fields[3].trim();
            String arrTimeStr    = fields[4].trim();
            String trainTypeStr  = fields[5].trim();
            String daysStr       = fields[6].trim();
            String firstPriceStr = fields[7].trim();
            String secondPriceStr= fields[8].trim();

            // Build stations 
            Station departureStation = parseStation(depCity);
            Station arrivalStation   = parseStation(arrCity);

            // Times
            LocalTime departureTime = LocalTime.parse(depTimeStr, TIME_FORMATTER);
            LocalTime arrivalTime   = LocalTime.parse(arrTimeStr, TIME_FORMATTER);

            // Train type 
            TrainType trainType =
                    TrainType.valueOf(trainTypeStr.toUpperCase().replace(" ", "_"));

            // Money
            Money priceFirst  = new Money(new BigDecimal(firstPriceStr),  "EUR");
            Money priceSecond = new Money(new BigDecimal(secondPriceStr), "EUR");

            // Days of operation 
            DaySet dayPattern = parseDayPattern(daysStr);

            // Create Route
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
            System.err.println("Error parsing line " + lineNo + ": " + e.getMessage());
            return null; // skip invalid line
        }
    }

    
    private Station parseStation(String city) {
        String name = city.trim();
        String country = ""; 
        String code = generateStationCode(name);
        
        return new Station(name, city.trim(), country, code);
    }

    //3-letter code from the station name
    private String generateStationCode(String stationName) {
        String letters = stationName.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "XXX";
        }
        return letters.substring(0, Math.min(3, letters.length()));
    }

    
    private DaySet parseDayPattern(String token) {
        String s = token.trim();

        // Case 1: pure 7-bit binary string "1111100" (Mon..Sun)
        if (s.matches("[01]{7}")) {
            int mask = Integer.parseInt(s, 2);
            return new DaySet(mask);
        }

        // Normalize
        s = s.replaceAll("\\s+", ""); // remove spaces

        // Case 2: range like "Fri-Sun", "Mon-Fri"
        if (s.contains("-")) {
            String[] parts = s.split("-");
            int start = dayIndex(parts[0]);
            int end   = dayIndex(parts[1]);
            int mask = 0;
            for (int i = start; i <= end; i++) {
                mask |= (1 << i);
            }
            return new DaySet(mask);
        }

        // Case 3: list like "Mon,Wed,Fri"
        int mask = 0;
        for (String part : s.split(",")) {
            mask |= (1 << dayIndex(part));
        }
        return new DaySet(mask);
    }

   
    private int dayIndex(String dayToken) {
        String d = dayToken.substring(0, Math.min(3, dayToken.length())).toLowerCase();
        switch (d) {
            case "mon": return 0;
            case "tue": return 1;
            case "wed": return 2;
            case "thu": return 3;
            case "fri": return 4;
            case "sat": return 5;
            case "sun": return 6;
            default:
                throw new IllegalArgumentException("Unknown day token: " + dayToken);
        }
    }
}
