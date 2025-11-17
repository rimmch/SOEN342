package service;

import model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class BookingService {
    private final Map<String, Client> clients = new HashMap<>();


    public Trip bookTrip(Connection connection, LocalDate travelDate,
                         TravelerInfo traveler, TicketClass ticketClass) {

        if (traveler == null) {
            throw new IllegalArgumentException("At least one traveler is required");
        }

        return bookGroupTrip(connection, travelDate, List.of(traveler), ticketClass);

    }

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

        Trip trip  = new Trip(connection, travelDate);

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


    public Client getClient(String lastName, String id) {
        if (lastName == null || id == null) return null;
        String key = makeClientKey(lastName, id);
        return clients.get(key);
    }


    private Client getOrCreateClient(String lastName, String id) {
        String key = makeClientKey(lastName, id);
        return clients.computeIfAbsent(key, k -> new Client(lastName, id));
    }

    private String makeClientKey(String lastName, String id) {
        return lastName.toLowerCase() + "|" + id;
    }


    private String extractLastName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

}







