CREATE TABLE user_promocode_activations
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_uuid        UUID         NOT NULL,
    promocode_id     BIGINT       NOT NULL,
    product_category VARCHAR(100) NOT NULL,
    activated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_activation_promocode FOREIGN KEY (promocode_id) REFERENCES promocodes (id),
    CONSTRAINT uq_user_promocode UNIQUE (user_uuid, promocode_id)
);