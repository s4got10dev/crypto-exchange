INSERT INTO user_account (id, username, password, email, first_name, last_name, created_at, updated_at, version)
VALUES ('d7096b98-ad8d-4ec7-ad4b-5341cdf6a1fb',
        'test-wallet-1',
        '$2a$10$p8sNDVO3bXeAqWpQsSGCUuvPnNHA2u9XxnO280vzmEcToRIPVNMV6',
        'test-wallet-1@mail.com',
        'Test',
        '1',
        '2024-03-26 11:04:53.526029',
        '2024-03-26 11:04:53.526029',
        0);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('480dbb7b-c7fe-4131-b1bd-632faa199aca',
        'd7096b98-ad8d-4ec7-ad4b-5341cdf6a1fb',
        'test-wallet-existing',
        '{
          "BTC": 6,
          "EUR": 4740600,
          "USD": 43011.6
        }',
        '2024-03-26 21:11:40.616851',
        '2024-03-26 21:57:53.248393',
        11);


INSERT INTO user_account (id, username, password, email, first_name, last_name, created_at, updated_at, version)
VALUES ('ec038f0f-b2d4-4142-993c-7ca2da2970b1',
        'test-wallet-2',
        '$2a$10$/gEZRG.T.rrAwnbT7hGCpOE.RiGhh.joN7HS2wY4mKBw6xmGfYXZ.',
        'test-wallet-2@mail.com',
        'Test',
        '2',
        '2024-03-27 08:18:58.034347',
        '2024-03-27 08:18:58.034347', 0);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('ff690a30-6944-4b8f-bc02-090daf133072',
        'ec038f0f-b2d4-4142-993c-7ca2da2970b1',
        'test-2-wallet-1',
        '{
          "BTC": 1.05,
          "USD": 123468.6
        }',
        '2024-03-26 21:11:40.616851',
        '2024-03-26 21:57:53.248393',
        20);

INSERT INTO wallet (id, user_id, name, balance, created_at, updated_at, version)
VALUES ('c21eba09-fd9c-4a0b-88a2-c228c507dd48',
        'ec038f0f-b2d4-4142-993c-7ca2da2970b1',
        'test-2-wallet-2',
        '{
          "EUR": 178657
        }',
        '2024-03-26 21:11:40.616851',
        '2024-03-26 21:57:53.248393',
        15);
