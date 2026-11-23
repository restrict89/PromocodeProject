package com.example.promocode.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PromocodeActivationLimitExceededException extends ResponseStatusException {
    public PromocodeActivationLimitExceededException() {
        super(
                HttpStatus.CONFLICT,
                "Promocode activation limit exceeded:"
        );
    }
}
