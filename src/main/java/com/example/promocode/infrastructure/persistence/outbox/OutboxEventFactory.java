package com.example.promocode.infrastructure.persistence.outbox;

import com.example.promocode.domain.event.PromocodeActivatedEvent;

public interface OutboxEventFactory {
    OutboxEvent fromPromocodeActivatedEvent(PromocodeActivatedEvent event);

    <T> T convertPayload(String payload, Class<T> payloadType);
}
