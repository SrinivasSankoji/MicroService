package com.idempotency.kafka.repository;

import com.idempotency.kafka.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @Modifying
    @Transactional
    @Query (value = """
            INSERT INTO orders(event_id, order_id, product_code, quantity, created_at)
            VALUES (?1, ?2, ?3, ?4, ?5)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(String eventId, String orderId, String productCode, int quantity, Instant createdAt);
}
