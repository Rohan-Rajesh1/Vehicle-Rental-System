package com.vehiclerental.pricing;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating and retrieving pricing strategies.
 */
public class PricingStrategyFactory {
    public static final String STANDARD = "Standard Rate";
    public static final String WEEKEND_SURGE = "Weekend Dynamic Surge";
    public static final String LONG_TERM = "Long-Term Loyalty Discount";

    public static PricingStrategy getStrategy(String strategyType) {
        if (strategyType == null) return new StandardPricingStrategy();
        if (strategyType.contains("Weekend")) {
            return new WeekendSurgePricingStrategy();
        } else if (strategyType.contains("Long-Term") || strategyType.contains("Loyalty")) {
            return new LongTermDiscountStrategy();
        }
        return new StandardPricingStrategy();
    }

    public static List<PricingStrategy> getAllStrategies() {
        List<PricingStrategy> list = new ArrayList<>();
        list.add(new StandardPricingStrategy());
        list.add(new WeekendSurgePricingStrategy());
        list.add(new LongTermDiscountStrategy());
        return list;
    }

    public static String[] getStrategyNames() {
        return new String[]{
            "Standard Rate",
            "Weekend Dynamic Surge (+15% on Sat/Sun)",
            "Long-Term Loyalty Discount (-10% for 7+ days)"
        };
    }
}
