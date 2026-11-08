package com.vehiclerental.pricing;

import com.vehiclerental.models.Vehicle;
import java.time.LocalDate;

/**
 * Standard pricing strategy: uses vehicle's native polymorphic rate calculation.
 */
public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public double calculateTotal(Vehicle vehicle, int days, LocalDate startDate) {
        if (vehicle == null || days <= 0) return 0.0;
        return vehicle.calculateRentalCharge(days);
    }

    @Override
    public String getName() {
        return "Standard Rate";
    }

    @Override
    public String getDescription(Vehicle vehicle, int days, LocalDate startDate) {
        if (vehicle == null) return "Standard daily rate calculation.";
        return vehicle.getPricingBreakdown(days);
    }
}
