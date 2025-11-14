package model;

public class Reservation {
    private final Client client;
    private final Connection connection;
    private final Ticket ticket;
    private final TicketClass ticketClass;

    public Reservation(Client client, Connection connection, Ticket ticket, TicketClass ticketClass) {

        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        if (ticketClass == null) {
            throw new IllegalArgumentException("Ticket class cannot be null");
        }

        this.client = client;
        this.connection = connection;
        this.ticket = ticket;
        this.ticketClass = ticketClass;
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

    public TicketClass getTicketClass() {
        return ticketClass;
    }

    @Override
    public String toString() {
        return "Reservation{" +
               "client=" + client +
               ", connection=" + connection +
               ", ticket=" + ticket +
               ", ticketClass=" + ticketClass +
               '}';
    }
}

 
