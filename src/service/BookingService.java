/*package service;

import model.*;

import java.time.LocalDate;
import java.util.*;

public class BookingService {
    private Map<String, Client> clients;

    public BookingService() {
        this.clients = new HashMap<>();
    }

    public Trip bookTrip(Connection connection, LocalDate travelDate,
                         List<TravelerInfo> travelers, TicketClass ticketClass) {
        if (travelers == null || travelers.isEmpty()) {
            throw new IllegalArgumentException("At least one traveler is required");
        }

        Trip trip = new Trip(connection, travelDate);

        for (TravelerInfo traveler : travelers) {
            Ticket ticket = new Ticket(traveler.getName(), traveler.getAge(),
                    traveler.getId(), connection, ticketClass);
            Reservation reservation = new Reservation(ticket, traveler.getName());
            trip.addReservation(reservation);

            String clientKey = traveler.getName() + "|" + traveler.getId();
            Client client = clients.computeIfAbsent(clientKey,
                    k -> new Client(extractLastName(traveler.getName()), traveler.getId()));
            client.addTrip(trip);
        }

        return trip;
    }

    public Client getClient(String lastName, String id) {
        String key = lastName + "|" + id;
        return clients.values().stream()
                .filter(c -> c.getLastName().equalsIgnoreCase(lastName) && c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private String extractLastName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}

 */