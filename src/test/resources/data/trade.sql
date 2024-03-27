INSERT INTO user_account (id, username, password, email, first_name, last_name, created_at, updated_at, version)
VALUES ('d8f0f301-c53e-4350-9d65-43fd8ab4ebd5',
        'test-trade-1',
        '$2a$10$p8sNDVO3bXeAqWpQsSGCUuvPnNHA2u9XxnO280vzmEcToRIPVNMV6',
        'test-trade-1@mail.com',
        'Test',
        'Trade 1',
        '2024-03-26 11:04:53.526029',
        '2024-03-26 11:04:53.526029',
        0);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('52e33e9d-7cc2-4482-8643-3f8d162af345',
        'd8f0f301-c53e-4350-9d65-43fd8ab4ebd5',
        'test-1-wallet-1',
        '{
          "BTC": 0
        }',
        '2024-03-26 21:11:40.616851',
        '2024-03-26 21:57:53.248393',
        0);


INSERT INTO user_account (id, username, password, email, first_name, last_name, created_at, updated_at, version)
VALUES ('aede8a3f-b9bf-4df6-93ca-f2d934c860d4',
        'test-trade-2',
        '$2a$10$/gEZRG.T.rrAwnbT7hGCpOE.RiGhh.joN7HS2wY4mKBw6xmGfYXZ.',
        'test-trade-2@mail.com',
        'Test',
        'Trade 2',
        '2024-03-27 08:18:58.034347',
        '2024-03-27 08:18:58.034347',
        0);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('c7efa8c0-8c7b-471d-a9f0-65b4247a57f3',
        'aede8a3f-b9bf-4df6-93ca-f2d934c860d4',
        'test-2-wallet-1',
        '{
          "EUR": 0
        }',
        '2024-03-26 21:11:40.616851',
        '2024-03-26 21:57:53.248393',
        0);
