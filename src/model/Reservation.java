package model;

public class Reservation {
    private Ticket ticket;
    private String travelerName;

    public Reservation(Ticket ticket, String travelerName) {
        this.ticket = ticket;
        this.travelerName = travelerName;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public String getTravelerName() {
        return travelerName;
    }
}