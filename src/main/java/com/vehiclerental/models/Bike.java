package com.vehiclerental.models;

/**
 * Bike class extending Vehicle.
 * Incorporates bike-specific properties and polymorphic rental calculation.
 */
public class Bike extends Vehicle {
    private int engineCc;
    private String bikeType; // "Standard", "Cruiser", "Sport", "Scooter"

    public Bike() {
        super();
        setVehicleType("BIKE");
    }

    public Bike(int id, int agencyId, String brand, String model, int year, String licensePlate,
                double dailyRate, VehicleStatus status, int engineCc, String bikeType) {
        super(id, agencyId, "BIKE", brand, model, year, licensePlate, dailyRate, status);
        this.engineCc = engineCc;
        this.bikeType = bikeType;
    }

    public Bike(int id, int agencyId, String brand, String model, int year, String licensePlate,
                double dailyRate, VehicleStatus status, int engineCc, String bikeType,
                String colour, String mileage, boolean tyreConditionGood, boolean engineConditionGood,
                boolean gpsEnabled, boolean insuranceVerified) {
        super(id, agencyId, "BIKE", brand, model, year, licensePlate, dailyRate, status,
              colour, mileage, tyreConditionGood, engineConditionGood, gpsEnabled, insuranceVerified);
        this.engineCc = engineCc;
        this.bikeType = bikeType;
    }

    /**
     * Polymorphic implementation:
     * Base rental = dailyRate * days
     * Flat helmet & safety gear charge = $10.00
     */
    @Override
    public double calculateRentalCharge(int days) {
        if (days <= 0) return 0.0;
        double base = getDailyRate() * days;
        double safetyKit = 10.00; // Flat safety helmet and gear charge
        return Math.round((base + safetyKit) * 100.0) / 100.0;
    }

    @Override
    public String getPricingBreakdown(int days) {
        if (days <= 0) return "0 days selected";
        double base = getDailyRate() * days;
        double safetyKit = 10.00;
        double total = calculateRentalCharge(days);
        return String.format("Base: $%.2f (%d days @ $%.2f/day) + Safety Gear Kit: $%.2f = Total: $%.2f",
                base, days, getDailyRate(), safetyKit, total);
    }

    // Getters and Setters
    public int getEngineCc() { return engineCc; }
    public void setEngineCc(int engineCc) { this.engineCc = engineCc; }

    public String getBikeType() { return bikeType; }
    public void setBikeType(String bikeType) { this.bikeType = bikeType; }
}
