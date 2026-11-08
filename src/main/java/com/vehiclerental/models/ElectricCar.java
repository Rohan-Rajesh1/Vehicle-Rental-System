package com.vehiclerental.models;

/**
 * ElectricCar class demonstrating multi-level inheritance:
 * Vehicle -> Car -> ElectricCar.
 * Incorporates zero-emission eco features, battery capacity, range, and carbon savings calculations.
 */
public class ElectricCar extends Car {
    private double batteryCapacityKwh; // e.g. 60.0 kWh
    private int rangeKm;               // e.g. 450 km
    private double chargingTimeHours;  // e.g. 0.75 hours (45 mins fast charge)

    public ElectricCar() {
        super();
        setFuelType("Electric");
    }

    public ElectricCar(int id, int agencyId, String brand, String model, int year, String licensePlate,
                       double dailyRate, VehicleStatus status, int seatingCapacity, String transmission,
                       double batteryCapacityKwh, int rangeKm, double chargingTimeHours,
                       String colour, String mileage, boolean tyreConditionGood, boolean engineConditionGood,
                       boolean gpsEnabled, boolean insuranceVerified) {
        super(id, agencyId, brand, model, year, licensePlate, dailyRate, status,
              seatingCapacity, transmission, "Electric",
              colour, mileage, tyreConditionGood, engineConditionGood, gpsEnabled, insuranceVerified);
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.rangeKm = rangeKm;
        this.chargingTimeHours = chargingTimeHours;
    }

    /**
     * Calculates the estimated CO2 emissions saved compared to a gasoline vehicle.
     * Standard gasoline car emits ~120g CO2 per km (~9.6 kg/day at 80 km/day).
     */
    public double calculateCarbonSavedKg(int days) {
        if (days <= 0) return 0.0;
        return Math.round(days * 9.6 * 10.0) / 10.0;
    }

    @Override
    public double calculateRentalCharge(int days) {
        // Electric cars enjoy a 5% green eco rebate on insurance
        if (days <= 0) return 0.0;
        double base = getDailyRate() * days;
        double ecoInsurance = base * 0.02; // Reduced 2% insurance due to zero emissions incentive
        return Math.round((base + ecoInsurance) * 100.0) / 100.0;
    }

    @Override
    public String getPricingBreakdown(int days) {
        if (days <= 0) return "0 days selected";
        double base = getDailyRate() * days;
        double ecoInsurance = base * 0.02;
        double total = calculateRentalCharge(days);
        return String.format("⚡ EV Green Rate: $%.2f (%d days @ $%.2f/day) + Eco Insurance (2%%): $%.2f = Total: $%.2f",
                base, days, getDailyRate(), ecoInsurance, total);
    }

    // Getters and Setters
    public double getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public void setBatteryCapacityKwh(double batteryCapacityKwh) { this.batteryCapacityKwh = batteryCapacityKwh; }

    public int getRangeKm() { return rangeKm; }
    public void setRangeKm(int rangeKm) { this.rangeKm = rangeKm; }

    public double getChargingTimeHours() { return chargingTimeHours; }
    public void setChargingTimeHours(double chargingTimeHours) { this.chargingTimeHours = chargingTimeHours; }
}
