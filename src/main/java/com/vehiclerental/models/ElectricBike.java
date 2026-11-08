package com.vehiclerental.models;

/**
 * ElectricBike class demonstrating multi-level inheritance:
 * Vehicle -> Bike -> ElectricBike.
 * Incorporates electric scooter/motorcycle attributes and eco calculations.
 */
public class ElectricBike extends Bike {
    private double batteryCapacityKwh; // e.g. 3.5 kWh
    private int rangeKm;               // e.g. 120 km

    public ElectricBike() {
        super();
        setBikeType("Electric Scooter / EV Bike");
    }

    public ElectricBike(int id, int agencyId, String brand, String model, int year, String licensePlate,
                        double dailyRate, VehicleStatus status, double batteryCapacityKwh, int rangeKm,
                        String colour, String mileage, boolean tyreConditionGood, boolean engineConditionGood,
                        boolean gpsEnabled, boolean insuranceVerified) {
        super(id, agencyId, brand, model, year, licensePlate, dailyRate, status,
              0, "Electric Scooter / EV Bike",
              colour, mileage, tyreConditionGood, engineConditionGood, gpsEnabled, insuranceVerified);
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.rangeKm = rangeKm;
    }

    public double calculateCarbonSavedKg(int days) {
        if (days <= 0) return 0.0;
        return Math.round(days * 3.5 * 10.0) / 10.0;
    }

    @Override
    public double calculateRentalCharge(int days) {
        if (days <= 0) return 0.0;
        double base = getDailyRate() * days;
        double safetyKit = 5.00; // Discounted green safety helmet fee
        return Math.round((base + safetyKit) * 100.0) / 100.0;
    }

    @Override
    public String getPricingBreakdown(int days) {
        if (days <= 0) return "0 days selected";
        double base = getDailyRate() * days;
        double safetyKit = 5.00;
        double total = calculateRentalCharge(days);
        return String.format("⚡ EV Bike Green Rate: $%.2f (%d days @ $%.2f/day) + Eco Helmet Kit: $%.2f = Total: $%.2f",
                base, days, getDailyRate(), safetyKit, total);
    }

    // Getters and Setters
    public double getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public void setBatteryCapacityKwh(double batteryCapacityKwh) { this.batteryCapacityKwh = batteryCapacityKwh; }

    public int getRangeKm() { return rangeKm; }
    public void setRangeKm(int rangeKm) { this.rangeKm = rangeKm; }
}
