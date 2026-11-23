CREATE TABLE promocodes
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code             VARCHAR(100) NOT NULL UNIQUE,
    category         VARCHAR(100) NOT NULL,
    max_activations  INT          NOT NULL DEFAULT 10,
    activation_count INT          NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_activation_limit CHECK (
        max_activations BETWEEN 1 AND 10
            AND activation_count >= 0 AND activation_count <= max_activations
        )
);