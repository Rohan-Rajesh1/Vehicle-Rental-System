package com.vehiclerental.pricing;

import com.vehiclerental.models.Vehicle;
import java.time.LocalDate;

/**
 * Long-term Loyalty Discount Strategy:
 * Applies a 10% discount on total rental cost for bookings of 7 or more days.
 */
public class LongTermDiscountStrategy implements PricingStrategy {
    private static final double DISCOUNT_RATE = 0.10; // 10% discount
    private static final int MIN_DAYS_FOR_DISCOUNT = 7;

    @Override
    public double calculateTotal(Vehicle vehicle, int days, LocalDate startDate) {
        if (vehicle == null || days <= 0) return 0.0;
        double baseTotal = vehicle.calculateRentalCharge(days);
        if (days >= MIN_DAYS_FOR_DISCOUNT) {
            return baseTotal * (1.0 - DISCOUNT_RATE);
        }
        return baseTotal;
    }

    @Override
    public String getName() {
        return "Long-Term Loyalty Discount (-10% for 7+ days)";
    }

    @Override
    public String getDescription(Vehicle vehicle, int days, LocalDate startDate) {
        if (vehicle == null) return "Long-term discount active.";
        String base = vehicle.getPricingBreakdown(days);
        if (days >= MIN_DAYS_FOR_DISCOUNT) {
            return base + " - Loyalty Discount (10% off for " + days + " days)";
        } else {
            int needed = MIN_DAYS_FOR_DISCOUNT - days;
            return base + " (Add " + needed + " more day(s) to unlock 10% Long-Term Discount)";
        }
    }
}
