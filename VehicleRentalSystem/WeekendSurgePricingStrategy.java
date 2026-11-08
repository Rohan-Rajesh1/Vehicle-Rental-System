package com.vehiclerental.pricing;

import com.vehiclerental.models.Vehicle;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Dynamic Weekend Surge Pricing Strategy:
 * Applies a 15% peak demand surge on weekend days (Saturday and Sunday).
 */
public class WeekendSurgePricingStrategy implements PricingStrategy {
    private static final double SURGE_RATE = 0.15; // +15% on weekend days

    @Override
    public double calculateTotal(Vehicle vehicle, int days, LocalDate startDate) {
        if (vehicle == null || days <= 0) return 0.0;
        
        LocalDate current = (startDate != null) ? startDate : LocalDate.now();
        int weekendDays = 0;
        for (int i = 0; i < days; i++) {
            DayOfWeek dow = current.plusDays(i).getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                weekendDays++;
            }
        }

        double baseTotal = vehicle.calculateRentalCharge(days);
        double dailyBase = baseTotal / days;
        double surgeAmount = weekendDays * dailyBase * SURGE_RATE;
        return baseTotal + surgeAmount;
    }

    @Override
    public String getName() {
        return "Weekend Dynamic Surge (+15% on Sat/Sun)";
    }

    @Override
    public String getDescription(Vehicle vehicle, int days, LocalDate startDate) {
        if (vehicle == null) return "Surge pricing active.";
        LocalDate current = (startDate != null) ? startDate : LocalDate.now();
        int weekendDays = 0;
        for (int i = 0; i < days; i++) {
            DayOfWeek dow = current.plusDays(i).getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                weekendDays++;
            }
        }
        return vehicle.getPricingBreakdown(days) + " + Weekend Surge (" + weekendDays + " weekend days @ +15%)";
    }
}
