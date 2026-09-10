package org.example.itemcounting.business.service;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.UserCoeff;
import org.example.itemcounting.repository.UserCoeffRepository;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.example.itemcounting.strategy.InvoicePricingStrategy;
import org.example.itemcounting.strategy.InvoicePricingStrategyFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InvoiceSummCalculationService {
    private final InvoiceService invoiceService;
    private final UserCoeffRepository coefficientRepository;
    private final InvoicePricingStrategyFactory strategyFactory;

    @Transactional(readOnly = true)
    public BigDecimal calculateInvoiceTotal(Long invoiceId, String strategyType) {
        InvoiceDTO invoiceDto = invoiceService.getInvoiceById(invoiceId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String group = authentication == null ? null : authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);

        BigDecimal coefficient = group == null
                ? BigDecimal.ONE
                : coefficientRepository.findByGroupName(group)
                .map(UserCoeff::getCoefficient)
                .orElse(BigDecimal.ONE);

        InvoicePricingStrategy strategy = strategyFactory.getStrategy(strategyType);
        return strategy.calculateTotal(invoiceDto, coefficient);
    }
}
