package com.example.promocode.infrastructure.http;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserStatusHttpClientImpl implements UserStatusHttpClient {
    @Override
    public boolean getStatusById(UUID userUuid) {
        return true;
    }
}
