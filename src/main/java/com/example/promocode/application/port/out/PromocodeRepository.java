package com.example.promocode.application.port.out;

import com.example.promocode.domain.model.Promocode;

import java.util.Optional;

public interface PromocodeRepository {

    Optional<Promocode> findById(long id);

    boolean tryIncrementActivationCount(
            long promocodeId,
            String productCategory
    );
}
