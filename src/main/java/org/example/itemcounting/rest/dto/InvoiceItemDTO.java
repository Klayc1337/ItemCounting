package org.example.itemcounting.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.itemcounting.entity.Invoice;
import org.example.itemcounting.entity.InvoiceItem;
import org.example.itemcounting.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemDTO {

    private Long id;

    private Long productId;

    private String productName;

    private BigDecimal quantity;

    private BigDecimal price;

    private LocalDateTime createdAt;

    public static InvoiceItemDTO fromEntity(InvoiceItem invoiceItem) {
        return new InvoiceItemDTO(
                invoiceItem.getId(),
                invoiceItem.getProduct().getId(),
                invoiceItem.getProduct().getName(),
                invoiceItem.getQuantity(),
                invoiceItem.getPrice(),
                invoiceItem.getCreatedAt()
        );
    }

    public InvoiceItem toEntity(Invoice invoice, Product product) {
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(invoice);
        item.setProduct(product);
        item.setQuantity(this.quantity);
        item.setPrice(this.price);
        return item;
    }
}

