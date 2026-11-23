package com.example.promocode.application;

import com.example.promocode.api.exception.PromocodeActivationLimitExceededException;
import com.example.promocode.api.exception.PromocodeAlreadyActivatedException;
import com.example.promocode.application.command.ActivatePromocodeCommand;
import com.example.promocode.application.port.out.PromocodeActivationRepository;
import com.example.promocode.application.port.out.OutboxEventRepository;
import com.example.promocode.application.port.out.PromocodeRepository;

import com.example.promocode.application.result.ActivatePromocodeResult;
import com.example.promocode.domain.event.PromocodeActivatedEvent;
import com.example.promocode.infrastructure.persistence.outbox.OutboxEventFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromocodeActivationManager {
    private final PromocodeRepository promocodeRepository;
    private final PromocodeActivationRepository activationPromocodeRepository;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public ActivatePromocodeResult activate(ActivatePromocodeCommand activateCommand) {
        boolean updatedActivationCount = promocodeRepository.tryIncrementActivationCount(
                activateCommand.promocodeId(),
                activateCommand.productCategory()
        );

        if (!updatedActivationCount) {
            throw new PromocodeActivationLimitExceededException();
        }

        boolean insertedPromocodeActivation = activationPromocodeRepository.tryInsert(
                activateCommand.userUuid(),
                activateCommand.promocodeId(),
                activateCommand.productCategory()
        );

        if (!insertedPromocodeActivation) {
            throw new PromocodeAlreadyActivatedException();
        }

        outboxEventRepository.save(outboxEventFactory.fromPromocodeActivatedEvent(
                        new PromocodeActivatedEvent(
                                UUID.randomUUID(),
                                activateCommand.userUuid(),
                                activateCommand.promocodeId(),
                                activateCommand.productCategory(),
                                Instant.now()
                        )
                )
        );


        return new ActivatePromocodeResult(
                activateCommand.userUuid(),
                activateCommand.promocodeId()
        );
    }
}
