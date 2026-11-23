TRUNCATE TABLE user_promocode_activations, outbox_events, promocodes RESTART IDENTITY;

INSERT INTO promocodes (code, category, max_activations, activation_count)
VALUES ('LAST_ACTIVATION', 'BOOKS', 10, 9),
       ('SINGLE_ACTIVATION', 'BOOKS', 10, 0),
       ('CATEGORY_CHECK', 'BOOKS', 10, 0),
       ('USER_STATUS_CHECK', 'BOOKS', 10, 0);

INSERT INTO user_promocode_activations (user_uuid, promocode_id, product_category)
SELECT CAST('00000000-0000-0000-0000-' || lpad(activation_number::text, 12, '0') AS uuid),
       id,
       category
FROM promocodes
CROSS JOIN generate_series(1, 9) AS existing(activation_number)
WHERE code = 'LAST_ACTIVATION';
