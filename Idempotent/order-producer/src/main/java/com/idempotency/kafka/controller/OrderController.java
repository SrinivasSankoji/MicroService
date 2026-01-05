package com.idempotency.kafka.controller;

import com.idempotency.kafka.dto.OrderRequestEventDTO;
import com.idempotency.kafka.producer.OrderProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping ("/orders")
public class OrderController {

    private final OrderProducer producer;

    public OrderController(OrderProducer producer) {
        this.producer = producer;
    }

    @PostMapping ("/publish")
    public ResponseEntity<OrderRequestEventDTO> publish(@RequestParam String orderId,
                                                        @RequestParam String productCode,
                                                        @RequestParam int quantity,
                                                        @RequestParam (required = false) String eventId) {
        String eid = (eventId == null || eventId.isBlank()) ? UUID.randomUUID().toString() : eventId;
        OrderRequestEventDTO event = new OrderRequestEventDTO(
                eid, orderId, productCode, quantity, Instant.now());
        producer.send(event);
        return ResponseEntity.ok(event);
    }
}
