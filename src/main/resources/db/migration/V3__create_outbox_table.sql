CREATE TABLE outbox_events
(
    id           UUID PRIMARY KEY,
    aggregate_id BIGINT NOT NULL,
    event_type   VARCHAR(255)                NOT NULL,
    payload      JSONB                       NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE INDEX idx_outbox_unpublished
    ON outbox_events(created_at, id)
    WHERE processed_at IS NULL;