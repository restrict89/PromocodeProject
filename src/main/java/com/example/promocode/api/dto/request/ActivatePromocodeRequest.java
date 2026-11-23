package com.example.promocode.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ActivatePromocodeRequest(
        @NotNull UUID userUuid,
        @Positive long promocodeId,
        @NotBlank String productCategory
) {
}
