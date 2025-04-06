package com.bridgeout.ticatt;

public class Booking {
    private String bookingId;
    private String userId;
    private String ticketId;
    private String eventName;
    private String eventLocation;
    private String eventDate;
    private double ticketPrice;

    // Default constructor (required for Firebase).
    public Booking() {}

    // Getter methods.
    public String getBookingId() {
        return bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public String getEventDate() {
        return eventDate;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    // Setter methods.
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
}