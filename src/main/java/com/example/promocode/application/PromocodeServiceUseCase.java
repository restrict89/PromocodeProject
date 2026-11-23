package com.example.promocode.application;

import com.example.promocode.application.command.ActivatePromocodeCommand;
import com.example.promocode.api.exception.PromocodeAlreadyActivatedException;
import com.example.promocode.application.result.ActivatePromocodeResult;
import com.example.promocode.infrastructure.http.UserStatusHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class PromocodeServiceUseCase implements PromocodeService {
    private final UserStatusHttpClient userStatusHttpClient;
    private final PromocodeActivationManager promocodeActivationManager;

    @Override
    public ActivatePromocodeResult activate(ActivatePromocodeCommand activatePromocodeCommand) {
        if (!userStatusHttpClient.getStatusById(activatePromocodeCommand.userUuid())) {
            throw new PromocodeAlreadyActivatedException();
        }

        return promocodeActivationManager.activate(activatePromocodeCommand);
    }
}
