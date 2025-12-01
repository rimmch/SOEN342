import model.*;
import service.*;
import parser.CSVRouteParser;
import persistence.DataLoader;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    private static List<Route> allRoutes;
    private static BookingService bookingService;
    private static Scanner scanner;
    private static List<Connection> foundConnections;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        foundConnections = new ArrayList<>();

        try {
            // Load train route data from CSV into database on application startup
            System.out.println("=== Loading train route data into database ===");
            DataLoader.loadRoutes("src/db/eu_rail_network.csv");
            System.out.println("=== Database loading complete ===\n");

            // Load routes into memory for searching
            System.out.println("=== Loading routes into memory ===");
            CSVRouteParser parser = new CSVRouteParser();
            allRoutes = parser.parseRoutes("src/db/eu_rail_network.csv");
            System.out.println("Loaded " + allRoutes.size() + " routes into memory.\n");

            bookingService = new BookingService();

            // Start interactive menu
            runInteractiveMenu();

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static void runInteractiveMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("   TRAIN RESERVATION SYSTEM");
            System.out.println("========================================");
            System.out.println("1. Search for Connections");
            System.out.println("2. Book a Trip");
            System.out.println("3. View My Trips");
            System.out.println("4. Exit");
            System.out.print("\nSelect an option (1-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    searchForConnections();
                    break;
                case "2":
                    bookTrip();
                    break;
                case "3":
                    viewTrips();
                    break;
                case "4":
                    System.out.println("\nThank you for using the Train Reservation System. Goodbye!");
                    return;
                default:
                    System.out.println("\nInvalid option. Please select 1-4.");
            }
        }
    }

    private static void searchForConnections() {
        System.out.println("\n=== SEARCH FOR CONNECTIONS ===");

        // Get origin station
        System.out.print("Enter origin city or station name: ");
        String originInput = scanner.nextLine().trim();
        Station origin = findStation(originInput);
        if (origin == null) {
            System.out.println("Origin station not found. Please try again.");
            return;
        }
        System.out.println("Selected origin: " + origin);

        // Get destination station
        System.out.print("Enter destination city or station name: ");
        String destInput = scanner.nextLine().trim();
        Station destination = findStation(destInput);
        if (destination == null) {
            System.out.println("Destination station not found. Please try again.");
            return;
        }
        System.out.println("Selected destination: " + destination);

        // Get travel date
        LocalDate travelDate = getTravelDate();
        if (travelDate == null) {
            return;
        }

        // Search for connections
        System.out.println("\nSearching for connections...");
        foundConnections = findConnections(origin, destination, travelDate);

        if (foundConnections.isEmpty()) {
            System.out.println("\nNo connections found for your criteria.");
            return;
        }

        // Display results
        System.out.println("\n=== FOUND " + foundConnections.size() + " CONNECTION(S) ===");
        for (int i = 0; i < foundConnections.size(); i++) {
            Connection conn = foundConnections.get(i);
            System.out.println("\n--- Connection " + (i + 1) + " ---");
            System.out.println("Transfers: " + conn.getNumberOfTransfers());
            System.out.println("Duration: " + conn.getFormattedTotalDuration());
            System.out.println("Departure: " + conn.getTotalDepartureTime());
            System.out.println("Arrival: " + conn.getTotalArrivalTime());
            System.out.println("1st Class: " + conn.getTotalPriceFirstClass());
            System.out.println("2nd Class: " + conn.getTotalPriceSecondClass());
            
            // Show route details
            List<Route> routes = conn.getRoutes();
            for (int j = 0; j < routes.size(); j++) {
                Route r = routes.get(j);
                System.out.println("  Leg " + (j + 1) + ": " + r.getDepartureStation().getName() + 
                                 " → " + r.getArrivalStation().getName() + 
                                 " (" + r.getDepartureTime() + " - " + r.getArrivalTime() + ")");
            }
        }
    }

    private static void bookTrip() {
        System.out.println("\n=== BOOK A TRIP ===");

        if (foundConnections.isEmpty()) {
            System.out.println("No connections available. Please search for connections first (Option 1).");
            return;
        }

        // Select connection
        System.out.print("Enter connection number (1-" + foundConnections.size() + "): ");
        String connInput = scanner.nextLine().trim();
        int connIndex;
        try {
            connIndex = Integer.parseInt(connInput) - 1;
            if (connIndex < 0 || connIndex >= foundConnections.size()) {
                System.out.println("Invalid connection number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        Connection selectedConnection = foundConnections.get(connIndex);

        // Get travel date
        LocalDate travelDate = getTravelDate();
        if (travelDate == null) {
            return;
        }

        // Check if connection respects layover policy
        if (!selectedConnection.respectsLayoverPolicy()) {
            System.out.println("\nERROR: This connection violates the layover policy and cannot be booked.");
            System.out.println(LayoverPolicy.getPolicyDescription());
            return;
        }

        // Get ticket class
        System.out.print("Select class (1=First Class, 2=Second Class): ");
        String classInput = scanner.nextLine().trim();
        TicketClass ticketClass;
        if (classInput.equals("1")) {
            ticketClass = TicketClass.FIRST_CLASS;
        } else if (classInput.equals("2")) {
            ticketClass = TicketClass.SECOND_CLASS;
        } else {
            System.out.println("Invalid class selection.");
            return;
        }

        // Get number of travelers
        System.out.print("Number of travelers: ");
        String numInput = scanner.nextLine().trim();
        int numTravelers;
        try {
            numTravelers = Integer.parseInt(numInput);
            if (numTravelers < 1) {
                System.out.println("Must have at least 1 traveler.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        // Get traveler information
        List<TravelerInfo> travelers = new ArrayList<>();
        for (int i = 0; i < numTravelers; i++) {
            System.out.println("\n--- Traveler " + (i + 1) + " ---");
            System.out.print("Full name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Age: ");
            String ageInput = scanner.nextLine().trim();
            int age;
            try {
                age = Integer.parseInt(ageInput);
                if (age < 0) {
                    System.out.println("Invalid age.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid age input.");
                return;
            }
            System.out.print("ID (passport/state ID): ");
            String id = scanner.nextLine().trim();

            travelers.add(new TravelerInfo(name, age, id));
        }

        // Book the trip
        try {
            Trip trip;
            if (travelers.size() == 1) {
                trip = bookingService.bookTrip(selectedConnection, travelDate, travelers.get(0), ticketClass);
            } else {
                trip = bookingService.bookGroupTrip(selectedConnection, travelDate, travelers, ticketClass);
            }

            System.out.println("\n=== BOOKING CONFIRMED ===");
            System.out.println("Trip ID: " + trip.getTripId());
            System.out.println("Travel Date: " + trip.getTravelDate());
            System.out.println("Number of Reservations: " + trip.getReservations().size());
            System.out.println("\nReservations:");
            for (Reservation res : trip.getReservations()) {
                System.out.println("  - " + res);
            }

        } catch (IllegalArgumentException e) {
            System.out.println("\nERROR: " + e.getMessage());
        }
    }

    private static void viewTrips() {
        System.out.println("\n=== VIEW MY TRIPS ===");
        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Enter your ID: ");
        String id = scanner.nextLine().trim();

        Client client = bookingService.getClient(lastName, id);
        if (client == null) {
            System.out.println("\nNo client found with that information.");
            return;
        }

        System.out.println("\n=== CLIENT INFORMATION ===");
        System.out.println("Name: " + client.getLastName());
        System.out.println("ID: " + client.getId());
        System.out.println("Total trips: " + client.getAllTrips().size());

        List<Trip> currentTrips = client.getCurrentTrips();
        List<Trip> pastTrips = client.getPastTrips();

        System.out.println("\n=== CURRENT/UPCOMING TRIPS (" + currentTrips.size() + ") ===");
        if (currentTrips.isEmpty()) {
            System.out.println("No current trips.");
        } else {
            for (Trip trip : currentTrips) {
                System.out.println("\n" + trip);
                System.out.println("  Connection: " + trip.getConnection().getNumberOfTransfers() + " transfer(s)");
                System.out.println("  Duration: " + trip.getConnection().getFormattedTotalDuration());
                for (Reservation res : trip.getReservations()) {
                    System.out.println("  - " + res);
                }
            }
        }

        System.out.println("\n=== PAST TRIPS (" + pastTrips.size() + ") ===");
        if (pastTrips.isEmpty()) {
            System.out.println("No past trips.");
        } else {
            for (Trip trip : pastTrips) {
                System.out.println("\n" + trip);
                System.out.println("  Connection: " + trip.getConnection().getNumberOfTransfers() + " transfer(s)");
                for (Reservation res : trip.getReservations()) {
                    System.out.println("  - " + res);
                }
            }
        }
    }

    private static Station findStation(String input) {
        input = input.toLowerCase();
        for (Route route : allRoutes) {
            Station dep = route.getDepartureStation();
            Station arr = route.getArrivalStation();
            
            if (dep.getCity().toLowerCase().contains(input) || 
                dep.getName().toLowerCase().contains(input) ||
                dep.getCode().toLowerCase().equals(input)) {
                return dep;
            }
            if (arr.getCity().toLowerCase().contains(input) || 
                arr.getName().toLowerCase().contains(input) ||
                arr.getCode().toLowerCase().equals(input)) {
                return arr;
            }
        }
        return null;
    }

    private static LocalDate getTravelDate() {
        System.out.print("Enter travel date (YYYY-MM-DD) or press Enter for today: ");
        String dateInput = scanner.nextLine().trim();
        
        if (dateInput.isEmpty()) {
            return LocalDate.now();
        }

        try {
            LocalDate date = LocalDate.parse(dateInput, DateTimeFormatter.ISO_DATE);
            if (date.isBefore(LocalDate.now())) {
                System.out.println("Travel date cannot be in the past.");
                return null;
            }
            return date;
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return null;
        }
    }

    private static List<Connection> findConnections(Station origin, Station destination, LocalDate travelDate) {
        List<Connection> connections = new ArrayList<>();
        DayOfWeek dayOfWeek = travelDate.getDayOfWeek();

        // Direct connections (1 leg)
        for (Route route : allRoutes) {
            if (route.getDepartureStation().equals(origin) &&
                route.getArrivalStation().equals(destination) &&
                route.getDayPattern().isOperatingOn(dayOfWeek)) {
                try {
                    Connection conn = new Connection(List.of(route));
                    if (conn.respectsLayoverPolicy()) {
                        connections.add(conn);
                    }
                } catch (Exception e) {
                    // Skip invalid connections
                }
            }
        }

        // 1-stop connections (2 legs)
        for (Route r1 : allRoutes) {
            if (!r1.getDepartureStation().equals(origin) ||
                !r1.getDayPattern().isOperatingOn(dayOfWeek)) {
                continue;
            }

            Station intermediate = r1.getArrivalStation();
            if (intermediate.equals(destination)) {
                continue; // Already handled as direct
            }

            for (Route r2 : allRoutes) {
                if (r2.getDepartureStation().equals(intermediate) &&
                    r2.getArrivalStation().equals(destination) &&
                    r2.getDayPattern().isOperatingOn(dayOfWeek)) {
                    
                    // Check if r2 departs after r1 arrives
                    if (r2.getDepartureTime().isAfter(r1.getArrivalTime()) ||
                        r2.getDepartureTime().equals(r1.getArrivalTime())) {
                        try {
                            Connection conn = new Connection(List.of(r1, r2));
                            if (conn.respectsLayoverPolicy()) {
                                connections.add(conn);
                            }
                        } catch (Exception e) {
                            // Skip invalid connections
                        }
                    }
                }
            }
        }

        // 2-stop connections (3 legs)
        for (Route r1 : allRoutes) {
            if (!r1.getDepartureStation().equals(origin) ||
                !r1.getDayPattern().isOperatingOn(dayOfWeek)) {
                continue;
            }

            Station stop1 = r1.getArrivalStation();
            if (stop1.equals(destination)) {
                continue;
            }

            for (Route r2 : allRoutes) {
                if (!r2.getDepartureStation().equals(stop1) ||
                    !r2.getDayPattern().isOperatingOn(dayOfWeek)) {
                    continue;
                }

                Station stop2 = r2.getArrivalStation();
                if (stop2.equals(destination) || stop2.equals(origin)) {
                    continue;
                }

                // Check timing
                if (!r2.getDepartureTime().isAfter(r1.getArrivalTime()) &&
                    !r2.getDepartureTime().equals(r1.getArrivalTime())) {
                    continue;
                }

                for (Route r3 : allRoutes) {
                    if (r3.getDepartureStation().equals(stop2) &&
                        r3.getArrivalStation().equals(destination) &&
                        r3.getDayPattern().isOperatingOn(dayOfWeek)) {
                        
                        // Check timing
                        if (r3.getDepartureTime().isAfter(r2.getArrivalTime()) ||
                            r3.getDepartureTime().equals(r2.getArrivalTime())) {
                            try {
                                Connection conn = new Connection(List.of(r1, r2, r3));
                                if (conn.respectsLayoverPolicy()) {
                                    connections.add(conn);
                                }
                            } catch (Exception e) {
                                // Skip invalid connections
                            }
                        }
                    }
                }
            }
        }

        // Sort by duration (shortest first)
        connections.sort(Comparator.comparing(Connection::getTotalDurationMinutes));

        return connections;
    }
}
