package com.vehiclerental.pricing;

import com.vehiclerental.models.Vehicle;
import java.time.LocalDate;

/**
 * Strategy Pattern Interface for Vehicle Rental Pricing.
 * Allows interchangeable pricing algorithms at runtime (Standard, Weekend Surge, Long-term Discount).
 */
public interface PricingStrategy {
    /**
     * Calculates the total rental charge for the given vehicle, duration, and start date.
     */
    double calculateTotal(Vehicle vehicle, int days, LocalDate startDate);

    /**
     * Returns human-readable strategy name.
     */
    String getName();

    /**
     * Returns a detailed pricing formula and breakdown string for presentation.
     */
    String getDescription(Vehicle vehicle, int days, LocalDate startDate);
}
