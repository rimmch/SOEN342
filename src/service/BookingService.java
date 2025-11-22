package service;

import model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingService {
    private final Map<String, Client> clients = new HashMap<>();

    /**
     * Convenience method for booking a single traveler.
     */
    public Trip bookTrip(Connection connection,
                         LocalDate travelDate,
                         TravelerInfo traveler,
                         TicketClass ticketClass) {
        return bookGroupTrip(connection, travelDate, List.of(traveler), ticketClass);
    }

    /**
     * Books a trip for one or more travelers on a given connection and date.
     * Enforces the layover policy before creating the trip.
     */
    public Trip bookGroupTrip(Connection connection,
                              LocalDate travelDate,
                              List<TravelerInfo> travelers,
                              TicketClass ticketClass) {

        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        if (travelDate == null) {
            throw new IllegalArgumentException("Travel date cannot be null");
        }
        if (travelers == null || travelers.isEmpty()) {
            throw new IllegalArgumentException("At least one traveler is required");
        }
        if (ticketClass == null) {
            throw new IllegalArgumentException("Ticket class cannot be null");
        }

        // Layover policy check (Iteration 3 requirement)
        if (!connection.respectsLayoverPolicy()) {
            throw new IllegalArgumentException("Connection violates layover policy — booking not allowed.");
        }

        Trip trip = new Trip(connection, travelDate);

        for (TravelerInfo t : travelers) {
            String lastName = extractLastName(t.getFullName());
            Client client = getOrCreateClient(lastName, t.getId());

            Ticket ticket = new Ticket(t.getFullName(), t.getAge(), t.getId(), connection, ticketClass);
            Reservation reservation = new Reservation(client, connection, ticket, ticketClass);

            trip.addReservation(reservation);
            client.addTrip(trip);
        }

        return trip;
    }

    private Client getOrCreateClient(String lastName, String id) {
        String key = lastName + "#" + id;
        Client existing = clients.get(key);
        if (existing != null) {
            return existing;
        }
        Client client = new Client(id, lastName);
        clients.put(key, client);
        return client;
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Unknown";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    public Client getClient(String lastName, String id) {
        String key = lastName + "#" + id;
        return clients.get(key);
    }
}