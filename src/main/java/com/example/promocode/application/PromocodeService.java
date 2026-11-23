package com.example.promocode.application;

import com.example.promocode.application.command.ActivatePromocodeCommand;
import com.example.promocode.application.result.ActivatePromocodeResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface PromocodeService {
   ActivatePromocodeResult activate(@Valid @NotNull ActivatePromocodeCommand activatePromocodeCommand);
}
