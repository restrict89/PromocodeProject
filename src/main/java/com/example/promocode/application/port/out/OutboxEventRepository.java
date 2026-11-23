package com.example.promocode.application.port.out;

import com.example.promocode.infrastructure.persistence.outbox.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {
    void save(OutboxEvent event);

    List<OutboxEvent> findUnpublished(int limit);

    void markProcessed(UUID eventId);
}
