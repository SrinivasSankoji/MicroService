package com.idempotency.kafka.dto;

import java.time.Instant;

public record OrderProcessedEventDTO(String eventId, String orderId, String status, Instant processedAt) {
}
