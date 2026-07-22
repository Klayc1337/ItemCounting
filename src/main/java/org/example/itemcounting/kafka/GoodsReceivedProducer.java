package org.example.itemcounting.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.itemcounting.event.GoodsReceivedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GoodsReceivedProducer {

    private static final String TOPIC = "goods-received";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendSync(GoodsReceivedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, json).get();
            log.info("Event sent: {}", json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send event", e);
        }
    }
}