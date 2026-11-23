package com.example.promocode.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PromocodeActivatedEvent(
        UUID eventId,
        UUID userUuid,
        long promocodeId,
        String productCategory,
        Instant activatedAt
) {
}
