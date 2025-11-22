
import model.*;
import service.*;
import parser.CSVRouteParser;
import persistence.DataLoader;

import java.time.LocalDate;
import java.util.*;

import model.*;
import service.*;
import parser.CSVRouteParser;
import persistence.DataLoader;

import java.time.LocalDate;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Load train route data from CSV into database on application startup
            System.out.println("=== Loading train route data into database ===");
            DataLoader.loadRoutes("src/db/eu_rail_network.csv");
            System.out.println("=== Database loading complete ===\n");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load data from CSV: " + e.getMessage());
            e.printStackTrace();
        }

        // ==================== ITERATION 1 ====================
        try {
            System.out.println("=== ITERATION 1 TEST ===");

            String csvPath = "src/db/eu_rail_network.csv";

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

            if (routes.size() > 1) {
                Route r2 = routes.get(1);

                System.out.println("\n--- Second Route ---");
                System.out.println(r2.toPublicString());
                System.out.println("Duration: " + r2.getDurationMinutes() + " minutes");

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


        // ==================== ITERATION 2 — UPDATED ====================
        try {
            System.out.println("=== ITERATION 2 TEST START ===");

            CSVRouteParser parser = new CSVRouteParser();
            List<Route> routes = parser.parseRoutes("src/db/eu_rail_network.csv");

            if (routes.size() < 2) {
                System.out.println("Not enough routes.");
                return;
            }

            System.out.println("Searching for a valid 2-leg connection...");

            BookingService probe = new BookingService();
            LocalDate probeDate = LocalDate.now().plusDays(5);

            Connection validConnection = null;

            outer:
            for (int i = 0; i < routes.size(); i++) {
                for (int j = i + 1; j < routes.size(); j++) {

                    Route a = routes.get(i);
                    Route b = routes.get(j);

                    // Ensure city continuity
                    if (!a.getArrivalStation().getCity()
                            .equals(b.getDepartureStation().getCity())) continue;

                    Connection candidate = new Connection(List.of(a, b));
                    TravelerInfo dummy = new TravelerInfo("Temp", 30, "ID-T");

                    try {
                        // If this passes, the connection is valid
                        probe.bookTrip(candidate, probeDate, dummy, TicketClass.SECOND_CLASS);
                        validConnection = candidate;

                        System.out.println("VALID connection found: "
                                + a.getRouteId() + " → " + b.getRouteId());
                        break outer;

                    } catch (Exception ignored) {}
                }
            }

            if (validConnection == null) {
                System.out.println("Could not find a valid connection.");
                return;
            }

            // print connection
            System.out.println("\n--- Loaded Valid Connection ---");
            System.out.println(validConnection);

            // Now use it for real booking
            BookingService service = new BookingService();
            LocalDate travelDate = LocalDate.now().plusDays(5);

            TravelerInfo t1 = new TravelerInfo("Alice Dupont", 28, "ID-A1");

            Trip soloTrip = service.bookTrip(
                    validConnection,
                    travelDate,
                    t1,
                    TicketClass.SECOND_CLASS
            );

            System.out.println("\n=== SOLO BOOKING ===");
            System.out.println(soloTrip);
            soloTrip.getReservations().forEach(System.out::println);

            // Family booking
            TravelerInfo f1 = new TravelerInfo("Karim Haddad", 45, "ID-F1");
            TravelerInfo f2 = new TravelerInfo("Leila Haddad", 42, "ID-M1");
            TravelerInfo c1 = new TravelerInfo("Nour Haddad", 16, "ID-C1");
            TravelerInfo c2 = new TravelerInfo("Rami Haddad", 12, "ID-C2");

            List<TravelerInfo> family = List.of(f1, f2, c1, c2);

            Trip familyTrip = service.bookGroupTrip(
                    validConnection,
                    travelDate.plusDays(1),
                    family,
                    TicketClass.FIRST_CLASS
            );

            System.out.println("\n=== FAMILY BOOKING ===");
            System.out.println(familyTrip);
            familyTrip.getReservations().forEach(System.out::println);

            // Check history
            System.out.println("\n=== CLIENT HISTORY ===");
            Client client = service.getClient("Dupont", "ID-A1");
            if (client != null) {
                System.out.println(client);
                System.out.println("Current trips: " + client.getCurrentTrips());
            }

            System.out.println("\n=== ITERATION 2 TEST DONE ===");

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}