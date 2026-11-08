package com.vehiclerental.models;

import java.sql.Timestamp;

/**
 * Abstract base class for vehicles demonstrating Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class Vehicle {
    private int id;
    private int agencyId;
    private String agencyName; // Display helper
    private String vehicleType; // "CAR" or "BIKE"
    private String brand;
    private String model;
    private int year;
    private String licensePlate;
    private double dailyRate;
    private VehicleStatus status;
    
    // Detailed specifications & conditions
    private String colour = "White";
    private String mileage = "18 km/l";
    private boolean tyreConditionGood = true;
    private boolean engineConditionGood = true;
    private boolean gpsEnabled = true;
    private boolean insuranceVerified = true;
    
    private Timestamp createdAt;

    public Vehicle() {}

    public Vehicle(int id, int agencyId, String vehicleType, String brand, String model, int year, 
                   String licensePlate, double dailyRate, VehicleStatus status) {
        this.id = id;
        this.agencyId = agencyId;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    public Vehicle(int id, int agencyId, String vehicleType, String brand, String model, int year, 
                   String licensePlate, double dailyRate, VehicleStatus status,
                   String colour, String mileage, boolean tyreConditionGood, boolean engineConditionGood,
                   boolean gpsEnabled, boolean insuranceVerified) {
        this(id, agencyId, vehicleType, brand, model, year, licensePlate, dailyRate, status);
        this.colour = colour;
        this.mileage = mileage;
        this.tyreConditionGood = tyreConditionGood;
        this.engineConditionGood = engineConditionGood;
        this.gpsEnabled = gpsEnabled;
        this.insuranceVerified = insuranceVerified;
    }

    /**
     * Polymorphic method to calculate rental charge.
     * Overridden by subclasses (Car, Bike) based on vehicle-specific pricing rules.
     *
     * @param days Number of rental days
     * @return Total rental cost
     */
    public abstract double calculateRentalCharge(int days);

    /**
     * Explanation of the polymorphic pricing formula for UI display.
     */
    public abstract String getPricingBreakdown(int days);

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAgencyId() { return agencyId; }
    public void setAgencyId(int agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public String getMileage() { return mileage; }
    public void setMileage(String mileage) { this.mileage = mileage; }

    public boolean isTyreConditionGood() { return tyreConditionGood; }
    public void setTyreConditionGood(boolean tyreConditionGood) { this.tyreConditionGood = tyreConditionGood; }

    public boolean isEngineConditionGood() { return engineConditionGood; }
    public void setEngineConditionGood(boolean engineConditionGood) { this.engineConditionGood = engineConditionGood; }

    public boolean isGpsEnabled() { return gpsEnabled; }
    public void setGpsEnabled(boolean gpsEnabled) { this.gpsEnabled = gpsEnabled; }

    public boolean isInsuranceVerified() { return insuranceVerified; }
    public void setInsuranceVerified(boolean insuranceVerified) { this.insuranceVerified = insuranceVerified; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return brand + " " + model + " (" + year + ") - " + licensePlate + " [$" + dailyRate + "/day]";
    }
}
