package com.vehiclerental.models;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Booking entity storing customer request details, rental timeline, pricing, and status.
 */
public class Booking {
    private int id;
    private int customerId;
    private String customerName;
    private String customerPhone;
    private int vehicleId;
    private String vehicleInfo; // Brand, model, plate
    private String vehicleType;
    private int agencyId;
    private String agencyName;
    private Date startDate;
    private Date endDate;
    private int totalDays;
    private double totalCost;
    private BookingStatus status;
    private String notes;
    private Timestamp createdAt;

    // Financial breakdown & Handover inspection fields
    private String pricingStrategyName = "Standard Rate";
    private double penaltyFee = 0.0;
    private double fuelFee = 0.0;
    private double platformFee = 0.0; // 5% platform third-party commission
    private String inspectionNotes = "";

    public Booking() {}

    public Booking(int id, int customerId, int vehicleId, Date startDate, Date endDate, 
                   int totalDays, double totalCost, BookingStatus status, String notes) {
        this.id = id;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.totalCost = totalCost;
        this.status = status;
        this.notes = notes;
        this.platformFee = Math.round(totalCost * 0.05 * 100.0) / 100.0;
    }

    public Booking(int id, int customerId, int vehicleId, Date startDate, Date endDate, 
                   int totalDays, double totalCost, BookingStatus status, String notes,
                   String pricingStrategyName, double penaltyFee, double fuelFee,
                   double platformFee, String inspectionNotes) {
        this(id, customerId, vehicleId, startDate, endDate, totalDays, totalCost, status, notes);
        this.pricingStrategyName = (pricingStrategyName != null && !pricingStrategyName.isEmpty()) ? pricingStrategyName : "Standard Rate";
        this.penaltyFee = penaltyFee;
        this.fuelFee = fuelFee;
        this.platformFee = platformFee;
        this.inspectionNotes = inspectionNotes;
    }

    /**
     * Calculates the time remaining until the rental due date.
     * Returns clear countdown status (e.g. "1 day left", "2 days left", "X days left", "Due today", "Overdue").
     */
    public String getDueStatus() {
        if (status == BookingStatus.RETURNED) {
            return "Returned / Completed";
        }
        if (status == BookingStatus.REJECTED) {
            return "Rejected";
        }
        if (endDate == null) return "-";
        
        LocalDate today = LocalDate.now();
        LocalDate end = endDate.toLocalDate();
        long diff = ChronoUnit.DAYS.between(today, end);

        String countdown;
        if (diff > 1) {
            countdown = diff + " days left";
        } else if (diff == 1) {
            countdown = "1 day left";
        } else if (diff == 0) {
            countdown = "Due today (0 days left)";
        } else {
            long overdue = Math.abs(diff);
            countdown = "Overdue by " + overdue + (overdue == 1 ? " day" : " days");
        }

        if (status == BookingStatus.PENDING) {
            return "Pending Approval (" + countdown + ")";
        }
        return countdown;
    }

    /**
     * Returns raw number of days left (positive if future, 0 if today, negative if overdue).
     * Works for active and pending bookings.
     */
    public Long getRawDaysLeft() {
        if (endDate == null || status == BookingStatus.RETURNED || status == BookingStatus.REJECTED) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate.toLocalDate());
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getAgencyId() { return agencyId; }
    public void setAgencyId(int agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getPricingStrategyName() { return pricingStrategyName; }
    public void setPricingStrategyName(String pricingStrategyName) { this.pricingStrategyName = pricingStrategyName; }

    public double getPenaltyFee() { return penaltyFee; }
    public void setPenaltyFee(double penaltyFee) { this.penaltyFee = penaltyFee; }

    public double getFuelFee() { return fuelFee; }
    public void setFuelFee(double fuelFee) { this.fuelFee = fuelFee; }

    public double getPlatformFee() { return platformFee; }
    public void setPlatformFee(double platformFee) { this.platformFee = platformFee; }

    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }

    public double getFinalSettlementTotal() {
        return Math.round((totalCost + penaltyFee + fuelFee) * 100.0) / 100.0;
    }
}
