

import model.*;
import service.*;
import parser.CSVRouteParser;

import java.time.LocalDate;
import java.util.*;

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

        try {
            System.out.println("=== ITERATION 2 TEST START ===");

            // 1) Load CSV as in iteration 1
            CSVRouteParser parser = new CSVRouteParser();
            List<Route> routes = parser.parseRoutes("eu_rail_network.csv");

            if (routes.size() < 2) {
                System.out.println(" Not enough routes to create a connection.");
                return;
            }

            // Build a sample connection from first 2 routes
            Connection connection = new Connection(List.of(routes.get(0), routes.get(1)));

            System.out.println("\n--- Loaded Sample Connection ---");
            System.out.println(connection);

            // 2) Create booking service
            BookingService service = new BookingService();

            LocalDate travelDate = LocalDate.now().plusDays(5);

            // 3) Create traveler info
            TravelerInfo t1 = new TravelerInfo("Alice Dupont", 28, "ID-A1");

            Trip soloTrip = service.bookTrip(
                    connection,
                    travelDate,
                    t1,
                    TicketClass.SECOND_CLASS
            );

            System.out.println("\n=== SOLO BOOKING ===");
            System.out.println(soloTrip);
            soloTrip.getReservations().forEach(System.out::println);

            // 4) Group trip
            TravelerInfo f1 = new TravelerInfo("Karim Haddad", 45, "ID-F1");
            TravelerInfo f2 = new TravelerInfo("Leila Haddad", 42, "ID-M1");
            TravelerInfo c1 = new TravelerInfo("Nour Haddad", 16, "ID-C1");
            TravelerInfo c2 = new TravelerInfo("Rami Haddad", 12, "ID-C2");

            List<TravelerInfo> family = List.of(f1, f2, c1, c2);

            Trip familyTrip = service.bookGroupTrip(
                    connection,
                    travelDate.plusDays(1),
                    family,
                    TicketClass.FIRST_CLASS
            );

            System.out.println("\n=== FAMILY BOOKING ===");
            System.out.println(familyTrip);
            familyTrip.getReservations().forEach(System.out::println);

            // 5) Check client history
            System.out.println("\n=== CLIENT HISTORY ===");
            Client client = service.getClient("Dupont", "ID-A1");
            if (client != null) {
                System.out.println(client);
                System.out.println("Current trips: " + client.getCurrentTrips());
            }

            System.out.println("\n=== ITERATION 2 TEST DONE ===");
        }
        catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
