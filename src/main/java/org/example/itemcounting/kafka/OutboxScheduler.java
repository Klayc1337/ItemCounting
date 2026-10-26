package org.example.itemcounting.kafka;

import lombok.extern.slf4j.Slf4j;
import org.example.itemcounting.entity.OutboxEvent;
import org.example.itemcounting.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OutboxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxScheduler(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${replenishment.request-topic}")
    private String requestTopic;

    @Scheduled(fixedDelayString = "${replenishment.request-time}")
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                String topic = requestTopic;

                kafkaTemplate.send(topic, event.getId().toString(), event.getJsonEvent()).get(30, TimeUnit.SECONDS);
                event.setSentAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                log.info("сохранено в БД и отправлено");
            } catch (Exception e) {
                log.error("ошибка отправки outbox-события id={}: {}", event.getId(), e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
