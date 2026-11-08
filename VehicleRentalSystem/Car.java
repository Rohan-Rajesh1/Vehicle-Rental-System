package com.vehiclerental.models;

/**
 * Car class extending Vehicle.
 * Incorporates car-specific properties and polymorphic rental calculation.
 */
public class Car extends Vehicle {
    private int seatingCapacity;
    private String transmission; // "Manual", "Automatic"
    private String fuelType;      // "Petrol", "Diesel", "Electric", "Hybrid"

    public Car() {
        super();
        setVehicleType("CAR");
    }

    public Car(int id, int agencyId, String brand, String model, int year, String licensePlate,
               double dailyRate, VehicleStatus status, int seatingCapacity, String transmission, String fuelType) {
        super(id, agencyId, "CAR", brand, model, year, licensePlate, dailyRate, status);
        this.seatingCapacity = seatingCapacity;
        this.transmission = transmission;
        this.fuelType = fuelType;
    }

    public Car(int id, int agencyId, String brand, String model, int year, String licensePlate,
               double dailyRate, VehicleStatus status, int seatingCapacity, String transmission, String fuelType,
               String colour, String mileage, boolean tyreConditionGood, boolean engineConditionGood,
               boolean gpsEnabled, boolean insuranceVerified) {
        super(id, agencyId, "CAR", brand, model, year, licensePlate, dailyRate, status,
              colour, mileage, tyreConditionGood, engineConditionGood, gpsEnabled, insuranceVerified);
        this.seatingCapacity = seatingCapacity;
        this.transmission = transmission;
        this.fuelType = fuelType;
    }

    /**
     * Polymorphic implementation:
     * Base rental = dailyRate * days
     * Comprehensive vehicle insurance / maintenance surcharge = 5% of base
     */
    @Override
    public double calculateRentalCharge(int days) {
        if (days <= 0) return 0.0;
        double base = getDailyRate() * days;
        double insurance = base * 0.05; // 5% car insurance cover
        return Math.round((base + insurance) * 100.0) / 100.0;
    }

    @Override
    public String getPricingBreakdown(int days) {
        if (days <= 0) return "0 days selected";
        double base = getDailyRate() * days;
        double insurance = base * 0.05;
        double total = calculateRentalCharge(days);
        return String.format("Base: $%.2f (%d days @ $%.2f/day) + Insurance (5%%): $%.2f = Total: $%.2f",
                base, days, getDailyRate(), insurance, total);
    }

    // Getters and Setters
    public int getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(int seatingCapacity) { this.seatingCapacity = seatingCapacity; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
}
