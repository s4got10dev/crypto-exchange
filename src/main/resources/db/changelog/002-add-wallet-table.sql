--liquibase formatted sql
--changeset s4got10dev:002-add-wallet-table
--preconditions onFail:MARK_RAN

create table if not exists wallet
(
  id         uuid        not null primary key,
  user_id    uuid        not null references user_account (id),
  name       varchar(30) not null,
  balance    jsonb       not null,
  created_at timestamp   not null,
  updated_at timestamp,
  version    int         not null
);


--rollback drop table if exists wallet;