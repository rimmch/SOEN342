package parser;

import model.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// TO READ CSV FILE - ITERATION 1 
public class CSVRouteParser {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public List<Route> parseRoutes(String csvFilePath) throws IOException {
        List<Route> routes = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilePath))) {
            String line = reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                Route route = parseRouteLine(line);
                if (route != null) {
                    routes.add(route);
                }
            }
        }

        return routes;
    }

    private Route parseRouteLine(String line) {
        try {
            String[] fields = line.split(",");

            if (fields.length < 12) {
                return null;
            }

            // Parse stations
            Station departureStation = parseStation(fields[0], fields[1], fields[2]);
            Station arrivalStation = parseStation(fields[3], fields[4], fields[5]);

            // Parse times
            LocalTime departureTime = LocalTime.parse(fields[6], TIME_FORMATTER);
            LocalTime arrivalTime = LocalTime.parse(fields[7], TIME_FORMATTER);

            // Parse train type
            TrainType trainType = TrainType.valueOf(fields[8].toUpperCase().replace(" ", "_"));

            // Parse prices
            Money priceFirst = new Money(new BigDecimal(fields[9]), "EUR");
            Money priceSecond = new Money(new BigDecimal(fields[10]), "EUR");

            // Parse day pattern
            DaySet dayPattern = new DaySet(Integer.parseInt(fields[11]));

            return new Route(
                    generateRouteId(departureStation, arrivalStation, departureTime),
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
            // Log error and skip invalid line
            System.err.println("Error parsing line: " + line + " - " + e.getMessage());
            return null;
        }
    }

    private Station parseStation(String name, String city, String country) {
        return new Station(name.trim(), city.trim(), country.trim(), generateStationCode(name));
    }

    private String generateRouteId(Station departure, Station arrival, LocalTime time) {
        return departure.getCode() + "-" + arrival.getCode() + "-" + time.toString().replace(":", "");
    }

    private String generateStationCode(String stationName) {
        return stationName.replaceAll("[^A-Z]", "").substring(0, Math.min(3, stationName.length()));
    }
}
