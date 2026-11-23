package com.example.promocode.infrastructure.http;

import java.util.UUID;

public interface UserStatusHttpClient {

    /**
     * Requests the neighbouring service for the user's activity status.
     *
     * @param userUuid user identifier
     * @return true if the user is active
     */
    boolean getStatusById(UUID userUuid);
}
