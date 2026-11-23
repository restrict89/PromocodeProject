package com.example.promocode.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PromocodeAlreadyActivatedException
        extends ResponseStatusException {

    public PromocodeAlreadyActivatedException() {
        super(
                HttpStatus.CONFLICT,
                "Promocode has already been activated by this user"
        );
    }
}
