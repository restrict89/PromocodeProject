package com.example.promocode.infrastructure.persistence.jdbc;

import com.example.promocode.application.port.out.OutboxEventRepository;
import com.example.promocode.infrastructure.persistence.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcOutboxEventRepository implements OutboxEventRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void save(OutboxEvent event) {
        String INSERT_OUTBOX_EVENT = """
                INSERT INTO outbox_events (
                    id, aggregate_id, event_type,
                    payload, created_at
                )
                VALUES (
                    :id, :aggregateId, :eventType,
                    CAST(:payload AS jsonb), :createdAt
                )
                """;
        jdbc.update(INSERT_OUTBOX_EVENT, new MapSqlParameterSource()
                .addValue("id", event.id())
                .addValue("aggregateId", event.aggregateId())
                .addValue("eventType", event.eventType())
                .addValue("payload", event.payload())
                .addValue(
                        "createdAt",
                        OffsetDateTime.ofInstant(
                                event.createdAt(),
                                ZoneOffset.UTC
                        )
                ));
    }

    @Override
    public List<OutboxEvent> findUnpublished(int limit) {
        String sql = """
                SELECT id, aggregate_id, event_type,
                       payload, created_at, processed_at
                FROM outbox_events
                WHERE processed_at IS NULL
                ORDER BY created_at, id
                LIMIT :limit
                """;

        return jdbc.query(
                sql,
                Map.of("limit", limit),
                (rs, rowNum) -> new OutboxEvent(
                        rs.getObject("id", UUID.class),
                        rs.getLong("aggregate_id"),
                        rs.getString("event_type"),
                        rs.getString("payload"),
                        rs.getObject(
                                "created_at", OffsetDateTime.class
                        ).toInstant(),
                        rs.getObject("processed_at") == null
                                ? null
                                : rs.getObject(
                                "processed_at",
                                OffsetDateTime.class
                        ).toInstant()
                )
        );
    }

    @Override
    public void markProcessed(UUID eventId) {
        String sql = """
                UPDATE outbox_events
                SET processed_at = NOW()
                WHERE id = :id
                  AND processed_at IS NULL
                """;

        jdbc.update(sql, Map.of("id", eventId));
    }
}

