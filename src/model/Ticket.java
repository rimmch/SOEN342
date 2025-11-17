package model;

public class Ticket {

    private static int ticketCounter = 1000;

    private final int ticketId;
    private final String travelerName;
    private final int travelerAge;
    private final String travelerId;
    private final Connection connection;
    private final TicketClass ticketClass;


    public Ticket(String travelerName, int travelerAge, String travelerId,
                  Connection connection, TicketClass ticketClass) {

        if (travelerName == null || travelerName.isBlank())
            throw new IllegalArgumentException("Traveler name cannot be null or blank");
        if (travelerAge < 0)
            throw new IllegalArgumentException("Age cannot be negative");
        if (travelerId == null || travelerId.isBlank())
            throw new IllegalArgumentException("Traveler ID cannot be null or blank");
        if (connection == null)
            throw new IllegalArgumentException("Connection cannot be null");
        if (ticketClass == null)
            throw new IllegalArgumentException("Ticket class cannot be null");

        this.ticketId = ticketCounter++;
        this.travelerName = travelerName;
        this.travelerAge = travelerAge;
        this.travelerId = travelerId;
        this.connection = connection;
        this.ticketClass = ticketClass;
    }

    public int getTicketId() { return ticketId; }
    public String getTravelerName() { return travelerName; }
    public int getTravelerAge() { return travelerAge; }
    public String getTravelerId() { return travelerId; }
    public Connection getConnection() { return connection; }
    public TicketClass getTicketClass() { return ticketClass; }


    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId=" + ticketId +
                ", traveler='" + travelerName + '\'' +
                ", class=" + ticketClass +
                '}';
    }
}

 
