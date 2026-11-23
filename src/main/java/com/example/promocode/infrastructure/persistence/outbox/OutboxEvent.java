package com.example.promocode.infrastructure.persistence.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        long aggregateId,
        String eventType,
        String payload,
        Instant createdAt,
        Instant processedAt
) {
}