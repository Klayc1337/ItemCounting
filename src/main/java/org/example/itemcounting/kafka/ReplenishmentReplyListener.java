package org.example.itemcounting.kafka;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.business.service.StockService;
import org.example.itemcounting.event.ReplenishmentReply;
import org.example.itemcounting.enums.RequestStatus;
import org.example.itemcounting.repository.ReplenishmentRequestRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ReplenishmentReplyListener {
    private final ObjectMapper objectMapper;
    private final ReplenishmentRequestRepository requests;
    private final StockService stockService;

    @Transactional
    @KafkaListener(topics = "${replenishment.reply-topic}", groupId = "itemcounting-replenishment")
    public void receive(String json) {
        ReplenishmentReply reply = objectMapper.readValue(json, ReplenishmentReply.class);
        validate(reply);
        var request = requests.findLockedById(reply.requestId()).orElseThrow();

        if (!request.getWarehouse().getId().equals(reply.warehouseId())
                || !request.getCentralWarehouse().getId().equals(reply.centralWarehouseId())) {
            throw new IllegalArgumentException("Reply warehouses не совпадает с реквестом");
        }

        if (request.getStatus() == RequestStatus.COMPLETED) {
            return;
        }

        for (var item : reply.items()) {
            if (item.quantity().signum() > 0) {
                stockService.addStock(item.productId(), reply.warehouseId(), null, item.quantity(),
                        "REPLENISHMENT", reply.invoiceId(), null,
                        "Replenishment request #" + reply.requestId());
            }
        }
        request.setStatus(RequestStatus.COMPLETED);
    }

    private void validate(ReplenishmentReply reply) {
        if (reply == null || reply.requestId() == null || reply.requestId() <= 0
                || reply.warehouseId() == null || reply.centralWarehouseId() == null
                || !"IN_DELIVERY".equals(reply.status())
                || reply.invoiceId() == null || reply.invoiceId() <= 0
                || reply.items() == null || reply.items().isEmpty()) {
            throw new IllegalArgumentException("неправильный запрос");
        }
        for (var item : reply.items()) {
            if (item == null || item.productId() == null || item.productId() <= 0
                    || item.quantity() == null || item.quantity().signum() < 0
                    || item.quantity().stripTrailingZeros().scale() > 3
                    || item.quantity().precision() - item.quantity().scale() > 12) {
                throw new IllegalArgumentException("неправильный запрос");
            }
        }
    }
}