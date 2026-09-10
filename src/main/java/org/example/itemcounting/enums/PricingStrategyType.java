package org.example.itemcounting.enums;

public enum PricingStrategyType {
    DEFAULT,
    DELIVERY;

    public static PricingStrategyType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }

        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Неизвестная стратегия: " + value, exception);
        }
    }
}
