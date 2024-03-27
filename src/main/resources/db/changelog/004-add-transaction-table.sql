--liquibase formatted sql
--changeset s4got10dev:004-add-transaction-table
--preconditions onFail:MARK_RAN

create table if not exists transaction
(
  id         uuid        not null primary key,
  user_id    uuid        not null references user_account (id),
  wallet_id  uuid        not null references wallet (id),
  type       varchar(25) not null,
  metadata   jsonb       not null,
  created_at timestamp   not null
);

--rollback drop table if exists wallet_transaction;