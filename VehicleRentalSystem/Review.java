package com.vehiclerental.models;

import java.sql.Timestamp;

/**
 * Model representing a customer review & star rating for a completed rental.
 */
public class Review {
    private int id;
    private int bookingId;
    private int customerId;
    private String customerName;
    private int agencyId;
    private int vehicleId;
    private String vehicleInfo;
    private int rating; // 1 to 5 stars
    private String comment;
    private Timestamp createdAt;

    public Review() {}

    public Review(int id, int bookingId, int customerId, String customerName,
                  int agencyId, int vehicleId, String vehicleInfo,
                  int rating, String comment, Timestamp createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.agencyId = agencyId;
        this.vehicleId = vehicleId;
        this.vehicleInfo = vehicleInfo;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getAgencyId() { return agencyId; }
    public void setAgencyId(int agencyId) { this.agencyId = agencyId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getStarsDisplay() {
        return "★".repeat(Math.max(0, Math.min(5, rating))) + "☆".repeat(Math.max(0, 5 - rating));
    }
}
