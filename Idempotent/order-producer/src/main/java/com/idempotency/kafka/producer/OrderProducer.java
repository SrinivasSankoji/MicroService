package com.idempotency.kafka.producer;

import com.idempotency.kafka.dto.OrderRequestEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {
    private final KafkaTemplate<String, OrderRequestEventDTO> template;
    private final String requestsTopic;

    public OrderProducer(KafkaTemplate<String, OrderRequestEventDTO> template,
                         @Value ("${app.topics.requests}") String requestsTopic) {
        this.template = template;
        this.requestsTopic = requestsTopic;
    }

    public void send(OrderRequestEventDTO event) {
        template.send(requestsTopic, event.orderId(), event);
    }
}
