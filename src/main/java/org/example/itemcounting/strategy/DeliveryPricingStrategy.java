package org.example.itemcounting.strategy;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.business.service.DistanceService;
import org.example.itemcounting.entity.DeliveryTimeCoefficient;
import org.example.itemcounting.entity.InvoiceItem;
import org.example.itemcounting.repository.DeliveryTimeCoefficientRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component("deliveryPricingStrategy")
@RequiredArgsConstructor
public class DeliveryPricingStrategy implements InvoicePricingStrategy{
    private final DistanceService distanceService;
    private final DeliveryTimeCoefficientRepository deliveryTimeCoefficientRepository;

    private final BigDecimal PRICE_PER_KM = new BigDecimal("10.00");

    @Override
    public BigDecimal calculateTotal(List<InvoiceItem> items,
                                     BigDecimal userCoefficient,
                                     LocalDateTime deliveryTime) {

        BigDecimal itemsSum = items.stream()
                .map(item -> item.getQuantity().multiply(item.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal baseTotal = itemsSum.multiply(userCoefficient);


        double distanceKm = distanceService.getRandomDistance();
        BigDecimal distance = BigDecimal.valueOf(distanceKm);

        LocalTime timeOfDay = (deliveryTime != null) ? deliveryTime.toLocalTime() : LocalTime.now();
        BigDecimal timeCoeff = deliveryTimeCoefficientRepository.findByTime(timeOfDay)
                .map(DeliveryTimeCoefficient::getCoefficient)
                .orElse(BigDecimal.ONE);

        BigDecimal deliveryCost = PRICE_PER_KM
                .multiply(distance)
                .multiply(timeCoeff)
                .setScale(2, RoundingMode.HALF_UP);


        return baseTotal.add(deliveryCost);
    }
}
