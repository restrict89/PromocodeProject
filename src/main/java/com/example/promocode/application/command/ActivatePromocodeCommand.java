package com.example.promocode.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ActivatePromocodeCommand(
        @NotNull UUID userUuid,
        @Positive long promocodeId,
        @NotBlank String productCategory
) {
}
