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
            String[] f = line.split(",", -1);
            if (f.length < 9) {
                System.err.println("Line " + lineNo + " skipped: expected 9 columns.");
                return null;
            }

            String routeId       = f[0].trim();
            String depCity       = f[1].trim();
            String arrCity       = f[2].trim();
            String depTimeStr    = f[3].trim();
            String arrTimeStr    = f[4].trim();
            String trainTypeStr  = f[5].trim();
            String daysStr       = f[6].trim();
            String firstPriceStr = f[7].trim();
            String secondPriceStr= f[8].trim();

            // Build Station objects
            Station departureStation = makeStation(depCity);
            Station arrivalStation   = makeStation(arrCity);

            // Times
            LocalTime departureTime = LocalTime.parse(depTimeStr, TIME_FORMATTER);
            LocalTime arrivalTime   = LocalTime.parse(arrTimeStr, TIME_FORMATTER);

            // Train type 
            TrainType trainType =
                    TrainType.valueOf(trainTypeStr.toUpperCase().replace(" ", "_"));

            // Prices
            Money priceFirst  = new Money(new BigDecimal(firstPriceStr), "EUR");
            Money priceSecond = new Money(new BigDecimal(secondPriceStr), "EUR");

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
        token = token.trim();

        // Binary day mask "1111100"
        if (token.matches("[01]{7}")) {
            return new DaySet(Integer.parseInt(token, 2));
        }

        // Remove spaces
        token = token.replaceAll("\\s+", "");

        // Range: "Fri-Sun"
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
            mask |= (1 << dayIndex(part));
        }

        return new DaySet(mask);
    }

    // Monday=0 … Sunday=6 
    private int dayIndex(String d) {
        d = d.substring(0, Math.min(3, d.length())).toLowerCase();
        switch (d) {
            case "mon": return 0;
            case "tue": return 1;
            case "wed": return 2;
            case "thu": return 3;
            case "fri": return 4;
            case "sat": return 5;
            case "sun": return 6;
            default:
                throw new IllegalArgumentException("Invalid day: " + d);
        }
    }
}
