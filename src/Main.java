

import model.*;
import parser.CSVRouteParser;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== ITERATION 1 TEST ===");

            String csvPath = "eu_rail_network.csv";

            CSVRouteParser parser = new CSVRouteParser();
            List<Route> routes = parser.parseRoutes(csvPath);

            System.out.println("Loaded routes: " + routes.size());

            if (routes.isEmpty()) {
                System.out.println(" ERROR: No routes found. Check CSV path & parser.");
                return;
            }

            System.out.println("\n--- First Route ---");
            Route r1 = routes.get(0);
            System.out.println(r1.toPublicString());
            System.out.println("Duration: " + r1.getDurationMinutes() + " minutes");

            // Test second route if available
            if (routes.size() > 1) {
                Route r2 = routes.get(1);

                System.out.println("\n--- Second Route ---");
                System.out.println(r2.toPublicString());
                System.out.println("Duration: " + r2.getDurationMinutes() + " minutes");

                // Build a connection
                Connection conn = new Connection(List.of(r1, r2));

                System.out.println("\n--- Test Connection (2 routes) ---");
                System.out.println("Departure: " + conn.getTotalDepartureTime());
                System.out.println("Arrival:   " + conn.getTotalArrivalTime());
                System.out.println("Total Duration: " + conn.getFormattedTotalDuration());
                System.out.println("1st Class Price: " + conn.getTotalPriceFirstClass());
                System.out.println("2nd Class Price: " + conn.getTotalPriceSecondClass());
                System.out.println("Transfers: " + conn.getNumberOfTransfers());
            }

            System.out.println("\n=== TEST COMPLETED ===");

        } catch (Exception e) {
            System.out.println(" Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
