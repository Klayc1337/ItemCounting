package org.example.itemcounting.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.itemcounting.entity.OutboxEvent;
import org.example.itemcounting.event.GoodsReceivedEvent;
import org.example.itemcounting.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveGoodsReceivedEvent(GoodsReceivedEvent event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType("GoodsReceivedEvent");
        outboxEvent.setJsonEvent(objectMapper.writeValueAsString(event));
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEventRepository.save(outboxEvent);
        log.info("Событие с invoiceId={} сохранено в outbox", event.getInvoiceId());
    }
}
