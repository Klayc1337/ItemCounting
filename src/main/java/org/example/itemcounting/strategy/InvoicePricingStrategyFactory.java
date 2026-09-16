package org.example.itemcounting.strategy;

import org.example.itemcounting.enums.PricingStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class InvoicePricingStrategyFactory {
    private final Map<PricingStrategyType, InvoicePricingStrategy> strategies;

    public InvoicePricingStrategyFactory(List<InvoicePricingStrategy> strategiesList) {
        strategies = new EnumMap<>(PricingStrategyType.class);

        for (InvoicePricingStrategy strategie : strategiesList){
            strategies.put(strategie.getType(), strategie);
        }
    }

    public InvoicePricingStrategy getStrategy(PricingStrategyType strategyName) {
        return strategies.get(strategyName);
    }
}
