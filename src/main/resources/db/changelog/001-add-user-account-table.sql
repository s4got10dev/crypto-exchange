--liquibase formatted sql
--changeset s4got10dev:001-add-user-account-table
--preconditions onFail:MARK_RAN

create table if not exists user_account
(
  id         uuid         not null primary key,
  username   varchar(32)  not null,
  password   char(60),
  email      varchar(128) not null,
  first_name varchar(32)  not null,
  last_name  varchar(32)  not null,
  created_at timestamp    not null,
  updated_at timestamp,
  version    int          not null
);

create unique index user_account_username ON user_account (username);
create unique index user_account_email ON user_account (email);

--rollback drop table if exists user_account;