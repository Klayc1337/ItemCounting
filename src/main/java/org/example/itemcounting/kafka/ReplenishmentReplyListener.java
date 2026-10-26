package org.example.itemcounting.kafka;

import lombok.RequiredArgsConstructor;
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

    @Transactional
    @KafkaListener(topics = "${replenishment.reply-topic}", groupId = "itemcounting-replenishment")
    public void receive(String json) {
        ReplenishmentReply reply = objectMapper.readValue(json, ReplenishmentReply.class);
        var request = requests.findById(reply.requestId()).orElseThrow();

        if (!request.getWarehouse().getId().equals(reply.warehouseId())
                || !request.getCentralWarehouse().getId().equals(reply.centralWarehouseId())) {
            throw new IllegalArgumentException("склад не совпадает");
        }
        if (!"ACCEPTED".equals(reply.status())) {
            throw new IllegalArgumentException("неправильный статус");
        }
        if (request.getStatus() == RequestStatus.PENDING) {
            request.setStatus(RequestStatus.APPROVED);
        }
    }
}
