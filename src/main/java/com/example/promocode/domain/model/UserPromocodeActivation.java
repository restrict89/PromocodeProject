package com.example.promocode.domain.model;

import java.time.Instant;
import java.util.UUID;

record UserPromocodeActivation(
        long id,
        UUID userUuid,
        long promocodeId,
        String productCategory,
        Instant activatedAt
) {
}
