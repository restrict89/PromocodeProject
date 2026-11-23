package com.example.promocode.application.result;

import java.util.UUID;

public record ActivatePromocodeResult(
        UUID userUuid,
        long promocodeId
) {
}
