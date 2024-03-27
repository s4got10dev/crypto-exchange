--liquibase formatted sql
--changeset s4got10dev:003-add-trade-order-table
--preconditions onFail:MARK_RAN

create table if not exists trade_order
(
  id             uuid            not null primary key,
  user_id        uuid            not null references user_account (id),
  wallet_id      uuid            not null references wallet (id),
  type           varchar(10)     not null,
  amount         numeric(34, 18) not null,
  base_currency  varchar(10)     not null,
  quote_currency varchar(10)     not null,
  status         varchar(10)     not null,
  created_at     timestamp       not null,
  updated_at     timestamp,
  version        int             not null
);


--rollback drop table if exists trade_order;