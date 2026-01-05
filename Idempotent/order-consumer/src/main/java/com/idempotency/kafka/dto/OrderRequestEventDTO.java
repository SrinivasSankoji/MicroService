package com.idempotency.kafka.dto;

import java.time.Instant;

public record OrderRequestEventDTO(String eventId, String orderId, String productCode, int quantity, Instant createdAt) {
}
