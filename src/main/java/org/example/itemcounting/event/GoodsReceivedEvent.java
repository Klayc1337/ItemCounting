package org.example.itemcounting.event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.itemcounting.entity.Invoice;
import org.example.itemcounting.entity.InvoiceItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceivedEvent {
    private Long invoiceId;
    private String comment;
    private String createdAt;
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long productId;
        private BigDecimal quantity;
        private BigDecimal price;
    }

    public static GoodsReceivedEvent mapToEvent(Invoice invoice, List<InvoiceItem> items) {
        List<GoodsReceivedEvent.Item> eventItems = items.stream()
                .map(item -> new GoodsReceivedEvent.Item(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        return new GoodsReceivedEvent(
                invoice.getId(),
                invoice.getComment(),
                invoice.getCreatedAt().toString(),
                eventItems
        );
    }
}
