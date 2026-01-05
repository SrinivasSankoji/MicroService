package com.idempotency.kafka.service;

import com.idempotency.kafka.dto.OrderProcessedEventDTO;
import com.idempotency.kafka.dto.OrderRequestEventDTO;
import com.idempotency.kafka.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public boolean saveOrIgnoreOrder(OrderRequestEventDTO event) {
        int inserted = orderRepository.insertIfAbsent(
                event.eventId(), event.orderId(), event.productCode(), event.quantity(), event.createdAt());
        return inserted == 1;
    }

    public OrderProcessedEventDTO processedEvent(OrderRequestEventDTO req) {
        return new OrderProcessedEventDTO(req.eventId(), req.orderId(), "PROCESSED", Instant.now());
    }
}
