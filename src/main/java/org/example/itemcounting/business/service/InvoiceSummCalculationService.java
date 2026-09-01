package org.example.itemcounting.business.service;
import org.example.itemcounting.entity.Invoice;
import org.example.itemcounting.entity.InvoiceItem;
import org.example.itemcounting.entity.UserCoeff;
import org.example.itemcounting.repository.InvoiceRepository;
import org.example.itemcounting.repository.InvoiceItemRepository;
import org.example.itemcounting.repository.UserCoeffRepository;
import lombok.RequiredArgsConstructor;
import org.example.itemcounting.strategy.InvoicePricingStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvoiceSummCalculationService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final UserCoeffRepository coefficientRepository;
    private final Map<String, InvoicePricingStrategy> strategies;

    @Transactional(readOnly = true)
    public BigDecimal calculateInvoiceTotal(Long invoiceId, String strategyType, LocalDateTime deliveryTime) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("накладной с id нет: " + invoiceId));


        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoiceId);


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String group = authentication == null ? null : authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);


        BigDecimal coefficient = group == null ? BigDecimal.ONE : coefficientRepository.findByGroupName(group)
                .map(UserCoeff::getCoefficient)
                .orElse(BigDecimal.ONE);


        String bean = strategyType + "PricingStrategy";
        InvoicePricingStrategy strategy = strategies.get(bean);
        if (strategy == null) {
            throw new IllegalArgumentException("неизвестая стратегия: " + strategyType);
        }

        return strategy.calculateTotal(items, coefficient, deliveryTime);
    }
}
