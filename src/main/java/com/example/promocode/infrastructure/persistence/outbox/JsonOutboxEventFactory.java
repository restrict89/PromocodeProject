package com.example.promocode.infrastructure.persistence.outbox;

import com.example.promocode.domain.event.PromocodeActivatedEvent;
import tools.jackson.core.JacksonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JsonOutboxEventFactory implements OutboxEventFactory {
    private static final String ACTIVATE_EVENT_TYPE = "PROMOCODE_ACTIVATED";
    private final ObjectMapper objectMapper;

    @Override
    public OutboxEvent fromPromocodeActivatedEvent(PromocodeActivatedEvent event) {
        return new OutboxEvent(
                event.eventId(),
                event.promocodeId(),
                ACTIVATE_EVENT_TYPE,
                objectMapper.writeValueAsString(event),
                Instant.now(),
                null
        );
    }

    @Override
    public <T> T convertPayload(String payload, Class<T> payloadType) {
        try {
            return objectMapper.readValue(payload, payloadType);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Could not deserialize JSON to "
                            + payloadType.getName(),
                    exception
            );
        }
    }
}