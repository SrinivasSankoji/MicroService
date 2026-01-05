package com.idempotency.kafka.consumer;

import com.idempotency.kafka.dto.OrderProcessedEventDTO;
import com.idempotency.kafka.dto.OrderRequestEventDTO;
import com.idempotency.kafka.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    private final OrderService service;
    private final KafkaTemplate<String, OrderProcessedEventDTO> processedTemplate;
    private final String processedTopic;

    public OrderConsumer(OrderService service, KafkaTemplate<String, OrderProcessedEventDTO> processedTemplate,
                         @Value ("${app.topics.processed}") String processedTopic) {
        this.service = service;
        this.processedTemplate = processedTemplate;
        this.processedTopic = processedTopic;
    }

    @KafkaListener (topics = "${app.topics.requests}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(OrderRequestEventDTO event) {
        // Business validation to publish the message to DLT
        if (event.quantity() <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }
        boolean inserted = service.saveOrIgnoreOrder(event);
        // Recommended: only publish output once per eventId (true exactly-once effect)
        if (inserted) {
            processedTemplate.send(processedTopic, event.orderId(), service.processedEvent(event));
        }
        // If duplicate: do nothing; offsets still commit as part of Kafka tx.
    }
}
