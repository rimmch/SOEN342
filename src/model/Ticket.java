package model;

public class Ticket {

    private static int ticketCounter = 1000;
    private final int ticketId;


    public Ticket() {
        this.ticketId = ticketCounter++;

    }

    public int getTicketId() {
        return ticketId;
    }

    @Override
    public String toString() {
        return "Ticket{" + "ticketId=" + ticketId + '}';
    }
    
}

 
