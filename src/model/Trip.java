package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Trip {
    private static final AtomicLong idGenerator = new AtomicLong(1);
    
    private final long tripId;
    private final List<Reservation> reservations;
    private final Connection connection;
    private LocalDate travelDate;

    public Trip(Connection connection, LocalDate travelDate) {

        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        if (travelDate == null) {
            throw new IllegalArgumentException("Travel date cannot be null");
        }
        
        this.tripId = idGenerator.getAndIncrement();
        this.reservations = new ArrayList<>();
        this.connection = connection;
        this.travelDate = travelDate;
    }

    public void addReservation(Reservation reservation) {
         if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }
        reservations.add(reservation);
    }

    public long getTripId() {
        return tripId;
    }

    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }

    public Connection getConnection() {
        return connection;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public boolean isFuture() {
        LocalDate today = LocalDate.now();
        return travelDate.isAfter(today) || travelDate.isEqual(today);
    }

    public boolean isPast() {
        return travelDate.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Trip " + tripId + " on " + travelDate +
               " (" + reservations.size() + " reservation(s))";
    }
}
