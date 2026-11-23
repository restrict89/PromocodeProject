package com.example.promocode.application.port.out;

import java.util.UUID;

public interface PromocodeActivationRepository {
    boolean tryInsert(UUID userUuid, long promocodeId, String productCategory);
}
