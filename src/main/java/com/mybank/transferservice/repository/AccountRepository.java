package com.mybank.transferservice.repository;

import com.mybank.transferservice.model.Account;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    @SqlUpdate("insert into accounts (id, balance) values (:id, :balance)")
    void create(@BindBean Account account);

    @SqlQuery("select id from accounts where id in (<accountList>) for update")
    List<UUID> lockAndGet(@BindList("accountList") UUID... accounts);

    @SqlUpdate("update accounts set balance=balance -:amount where id=:id and balance >= :amount")
    int debit(@Bind("id") UUID id, @Bind("amount") double amount);

    @SqlUpdate("update accounts set balance=balance + :amount where id=:id")
    int credit(@Bind("id") UUID id, @Bind("amount") double amount);

    @SqlQuery("select * from accounts where id=:id")
    @RegisterRowMapper(AccountMapper.class)
    Optional<Account> findById(@Bind("id") UUID id);

    class AccountMapper implements RowMapper<Account> {
        @Override
        public Account map(ResultSet rs, StatementContext ctx) throws SQLException {
            return Account.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .balance(rs.getDouble("balance"))
                    .build();
        }
    }
}
