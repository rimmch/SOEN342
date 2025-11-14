package model;

public class Reservation {
    private final Client client;
    private final Connection connection;
    private final Ticket ticket;
    private final TravelClass travelClass;

    public Reservation(Client clinent, Connection connection, Ticket ticket, TravelClass travelClass) {

        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        if (travelClass == null) {
            throw new IllegalArgumentException("Travel class cannot be null");
        }

        this.client = client;
        this.connection = connection;
        this.ticket = ticket;
        this.travelClass = travelClass;
    }

    public Client getClient() {
        return client;

    }

    public Connection getConnection() {
        return connection;

    }
    
    public Ticket getTicket() {
        return ticket;
    }

    public TravelClass getClass() {
        return travelClass;
    }

    @Override
    public String toString() {
        return "Reservation{" +
               "client=" + client +
               ", connection=" + connection +
               ", ticket=" + ticket +
               ", travelClass=" + travelClass +
               '}';
    }
}

 
