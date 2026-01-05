package com.idempotency.kafka.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table (name = "orders")
public class OrderEntity {
    @Id
    @Column (name = "event_id")
    private String eventId;

    @Column (name = "order_id", nullable = false)
    private String orderId;

    @Column (name = "product_code", nullable = false)
    private String productCode;

    @Column (name = "quantity", nullable = false)
    private int quantity;

    @Column (name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrderEntity() {
    }
}
