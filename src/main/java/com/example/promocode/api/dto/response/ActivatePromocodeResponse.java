package com.example.promocode.api.dto.response;

import java.util.UUID;

public record ActivatePromocodeResponse(
        UUID userUuid,
        long promocodeId
) {
}
