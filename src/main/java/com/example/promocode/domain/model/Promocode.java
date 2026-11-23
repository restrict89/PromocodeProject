package com.example.promocode.domain.model;

import java.time.Instant;

public record Promocode(
        long id,
        String code,
        String category,
        int maxActivations,
        int activationCount,
        Instant createdAt
) {
}
