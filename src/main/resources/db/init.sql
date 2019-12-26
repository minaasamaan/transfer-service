create table accounts(
  id UUID not null,
  balance float not null,
  primary key (id)
);

create table transactions(
  id UUID not null,
  acount_id UUID not null,
  amount float not null,
  type varchar not null,
  primary key (id),
  foreign key (acount_id) references accounts(id)
);

