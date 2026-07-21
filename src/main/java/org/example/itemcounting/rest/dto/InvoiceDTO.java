package org.example.itemcounting.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.itemcounting.entity.Invoice;
import org.example.itemcounting.entity.InvoiceItem;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private Long id;

    private InvoiceType type;

    private InvoiceStatus status;

    private String comment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private BigDecimal totalAmount;

    private List<InvoiceItemDTO> items;

    public static InvoiceDTO fromEntity(Invoice invoice, List<InvoiceItem> items) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setType(invoice.getType());
        dto.setStatus(invoice.getStatus());
        dto.setComment(invoice.getComment());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());

        List<InvoiceItemDTO> itemDTOs = items.stream()
                .map(InvoiceItemDTO::fromEntity)
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);

        BigDecimal total = items.stream()
                .map(item -> {
                    if (item.getPrice() != null) {
                        return item.getPrice().multiply(item.getQuantity());
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setTotalAmount(total);

        return dto;
    }
}
