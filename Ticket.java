package com.bridgeout.ticatt;

public class Ticket {
    private String ticketId;
    private String userId;
    private String event;
    private String eventDescription;
    private String eventDate;
    private String eventTime;
    private String eventLocation;
    private double price;
    private int quantityOfTicket;
    private boolean isAvailable;

    // Default constructor (required for Firebase).
    public Ticket() {}

    // Getter methods.
    public String getTicketId() {
        return ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEvent() {
        return event;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantityOfTicket() {
        return quantityOfTicket;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    // Setter method for ticketId (Firebase key).
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }
}