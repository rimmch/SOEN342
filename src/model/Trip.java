/*package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Trip {
    private static final AtomicLong idGenerator = new AtomicLong(1);
    private final long tripId;
    private List<Reservation> reservations;
    private Connection connection;
    private LocalDate travelDate;

    public Trip(Connection connection, LocalDate travelDate) {
        gitthis.tripId = idGenerator.getAndIncrement();
        this.reservations = new ArrayList<>();
        this.connection = connection;
        this.travelDate = travelDate;
    }

    public void addReservation(Reservation reservation) {
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
        return travelDate.isAfter(LocalDate.now()) || travelDate.isEqual(LocalDate.now());
    }

    public boolean isPast() {
        return travelDate.isBefore(LocalDate.now());
    }
}*/