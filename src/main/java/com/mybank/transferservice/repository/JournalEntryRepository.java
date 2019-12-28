package com.mybank.transferservice.repository;

import com.mybank.transferservice.model.JournalEntry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository {

    @SqlUpdate("insert into journal_entries values (:id, :accountId, :correlationId, :amount, :description)")
    void create(@BindBean JournalEntry journalEntry);

    @SqlQuery("select * from journal_entries where id=:id")
    @RegisterRowMapper(JournalEntryMapper.class)
    Optional<JournalEntry> findById(@Bind("id") UUID id);

    class JournalEntryMapper implements RowMapper<JournalEntry> {
        @Override
        public JournalEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
            return JournalEntry.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .accountId(UUID.fromString(rs.getString("account_id")))
                    .correlationId(UUID.fromString(rs.getString("correlation_id")))
                    .amount(rs.getDouble("amount"))
                    .description(rs.getString("description"))
                    .build();
        }
    }
}
