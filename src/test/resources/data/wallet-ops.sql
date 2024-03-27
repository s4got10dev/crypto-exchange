INSERT INTO user_account (id, username, password, email, first_name, last_name, created_at, updated_at, version)
VALUES ('cd91b02e-bc8b-4bec-9404-f861eeba59b8',
        'test-wallet-ops-1',
        '$2a$10$p8sNDVO3bXeAqWpQsSGCUuvPnNHA2u9XxnO280vzmEcToRIPVNMV6',
        'test-1@mail.com',
        'Test',
        '1',
        '2024-03-26 11:04:53.526029',
        '2024-03-26 11:04:53.526029',
        0);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('0a18a128-4ae2-4c73-afaa-9d9e5f88235c',
        'cd91b02e-bc8b-4bec-9404-f861eeba59b8',
        'test-1-wallet-1',
        '{
          "BTC": 6,
          "EUR": 4740600,
          "USD": 43011.6
        }',
        '2024-03-26 21:11:40.616851',
        '2024-03-26 21:57:53.248393',
        11);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('0bfee18c-6fe5-4850-a07d-c2c3b824c479',
        'cd91b02e-bc8b-4bec-9404-f861eeba59b8',
        'test-1-wallet-2',
        '{
          "BTC": 1.05,
          "USD": 123468.6
        }',
        '2024-03-26 21:11:40.616851',
        '2024-03-26 21:57:53.248393',
        20);
