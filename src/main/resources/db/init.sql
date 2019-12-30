----------- DDL for the application tables-------------

create table accounts(
  id         UUID not null,
  balance    float not null,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp on
update current_timestamp,
primary key(id)
);

create table journal_entries(
  id UUID not null,
account_id UUID not null,
correlation_id UUID not null,
amount float not null,
description varchar,
created_at timestamp not null default current_timestamp,
primary key(id),
foreign key(account_id)references accounts(id)
);

----------- Test data to be used in smoke testing -------------

insert into accounts(id, balance)
values ('ad52e2dc-2a8e-11ea-978f-2e728ce88125', 2000.0);
insert into accounts(id, balance)
values ('ad52e5a2-2a8e-11ea-978f-2e728ce88125', 2000.0);

