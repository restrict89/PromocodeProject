package com.example.promocode.infrastructure.persistence.jdbc;

import com.example.promocode.application.port.out.PromocodeActivationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcPromocodeActivationRepository implements PromocodeActivationRepository {
    private static final String INSERT_ACTIVATION = """
            INSERT INTO user_promocode_activations (
                 user_uuid,
                 promocode_id,
                 product_category,
                 activated_at
             )
             VALUES (
                 :userUuid,
                 :promocodeId,
                 :category,
                 NOW()
             )
             ON CONFLICT (user_uuid, promocode_id)
             DO NOTHING;
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public boolean tryInsert(UUID userUuid, long promocodeId, String productCategory) {
        int inserted = jdbcTemplate.update(INSERT_ACTIVATION,
                Map.of(
                        "userUuid", userUuid,
                        "promocodeId", promocodeId,
                        "category", productCategory
                )
        );
        return inserted == 1;
    }
}
