INSERT INTO user_account (id, username, password, email, first_name, last_name, created_at, updated_at, version)
VALUES ('271758ff-b48c-47d1-a74f-c530f97bbbcc',
        'test-order-1',
        '$2a$10$p8sNDVO3bXeAqWpQsSGCUuvPnNHA2u9XxnO280vzmEcToRIPVNMV6',
        'test-order-1@mail.com',
        'Test',
        '1',
        '2024-03-26 11:04:53.526029',
        '2024-03-26 11:04:53.526029',
        0);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('ca586f40-cd98-4434-822e-41bdb0dce3e4',
        '271758ff-b48c-47d1-a74f-c530f97bbbcc',
        'test-1-wallet-1',
        '{
          "BTC": 6,
          "EUR": 4740600,
          "USD": 43011.6
        }',
        '2024-03-26 12:11:40.616851',
        '2024-03-26 12:57:53.248393',
        11);

INSERT INTO trade_order (id, user_id, wallet_id, type, amount, base_currency, quote_currency, status, created_at,
                         updated_at, version)
VALUES ('78e4a1b1-135a-441f-ae4b-a1c169fa254f',
        '271758ff-b48c-47d1-a74f-c530f97bbbcc',
        'ca586f40-cd98-4434-822e-41bdb0dce3e4',
        'BUY',
        1.00,
        'BTC',
        'EUR',
        'CANCELED',
        '2024-03-26 15:27:44.273079',
        '2024-03-26 15:47:46.164934',
        0);

INSERT INTO trade_order (id, user_id, wallet_id, type, amount, base_currency, quote_currency, status, created_at,
                         updated_at, version)
VALUES ('8a509b2c-4939-4a81-9fc0-c98b557181f0',
        '271758ff-b48c-47d1-a74f-c530f97bbbcc',
        'ca586f40-cd98-4434-822e-41bdb0dce3e4',
        'BUY',
        1.00,
        'BTC',
        'EUR',
        'OPEN',
        '2024-03-26 21:24:25.456409',
        '2024-03-26 21:24:25.456409',
        0);


INSERT INTO user_account (id, username, password, email, first_name, last_name, created_at, updated_at, version)
VALUES ('1660812a-dad2-4f98-808b-27b91f505b9e',
        'test-order-2',
        '$2a$10$p8sNDVO3bXeAqWpQsSGCUuvPnNHA2u9XxnO280vzmEcToRIPVNMV6',
        'test-order-2@mail.com',
        'Test',
        '1',
        '2024-03-26 11:04:53.526029',
        '2024-03-26 11:04:53.526029',
        0);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('db37cde9-18d4-4450-b8a3-03171109d51e',
        '271758ff-b48c-47d1-a74f-c530f97bbbcc',
        'test-2-wallet-1',
        '{
          "BTC": 1.05,
          "USD": 123468.6
        }',
        '2024-03-26 12:11:40.616851',
        '2024-03-26 12:57:53.248393',
        20);

INSERT INTO trade_order (id, user_id, wallet_id, type, amount, base_currency, quote_currency, status, created_at,
                         updated_at, version)
VALUES ('f299b366-5eba-48d3-a19c-d18907053b66',
        '1660812a-dad2-4f98-808b-27b91f505b9e',
        'db37cde9-18d4-4450-b8a3-03171109d51e',
        'BUY',
        1.00,
        'BTC',
        'USD',
        'CANCELED',
        '2024-03-26 15:27:44.273079',
        '2024-03-26 15:47:46.164934',
        0);

INSERT INTO trade_order (id, user_id, wallet_id, type, amount, base_currency, quote_currency, status, created_at,
                         updated_at, version)
VALUES ('fd828b86-e7e8-4368-9d9c-1b7bbb6427ad',
        '1660812a-dad2-4f98-808b-27b91f505b9e',
        'db37cde9-18d4-4450-b8a3-03171109d51e',
        'BUY',
        1.00,
        'BTC',
        'USD',
        'OPEN',
        '2024-03-26 21:24:25.456409',
        '2024-03-26 21:24:25.456409',
        0);
