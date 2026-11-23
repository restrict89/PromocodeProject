package com.example.promocode.infrastructure.persistence.jdbc;

import com.example.promocode.application.port.out.PromocodeRepository;
import com.example.promocode.domain.model.Promocode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class JdbcPromocodeRepository implements PromocodeRepository {
    private static final String UPDATE_PROMOCODE_COUNT = """
            UPDATE promocodes
            SET activation_count = activation_count + 1
            WHERE id = :id AND category = :category
                AND activation_count < max_activations
            """;
    private static final String FIND_BY_ID = """
            SELECT id, code, category,
                   max_activations, activation_count, created_at
            FROM promocodes
            WHERE id = :id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<Promocode> findById(long id) {
        return jdbcTemplate.query(
                FIND_BY_ID,
                Map.of("id", id),
                (rs, rowNum) -> new Promocode(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("category"),
                        rs.getInt("max_activations"),
                        rs.getInt("activation_count"),
                        rs.getObject("created_at", OffsetDateTime.class)
                                .toInstant()
                )
        ).stream().findFirst();
    }

    @Override
    public boolean tryIncrementActivationCount(long promocodeId, String productCategory) {
        int updated = jdbcTemplate.update(
                UPDATE_PROMOCODE_COUNT,
                Map.of(
                        "id", promocodeId,
                        "category", productCategory
                )
        );
        return updated == 1;
    }
}
