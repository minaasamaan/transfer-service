create table accounts(
  id UUID not null,
  balance float not null,
  primary key(id)
);

create table journal_entries(
  id UUID not null,
  account_id UUID not null,
  correlation_id UUID not null,
  amount float not null,
  description varchar,
  primary key(id),
  foreign key(account_id)references accounts(id)
);

