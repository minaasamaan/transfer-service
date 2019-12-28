package com.mybank.transferservice;

import com.mybank.transferservice.model.JournalEntry;
import com.mybank.transferservice.repository.JournalEntryRepository;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.WebTarget;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DropwizardExtensionsSupport.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {
    private static final String configurations = ResourceHelpers.resourceFilePath("application-test.yml");
    private static final DropwizardAppExtension<TransferAppConfiguration> application = new DropwizardAppExtension<>(
            TransferApplication.class, configurations);
    private static Jdbi jdbi;
    private static JournalEntryRepository journalEntryRepository;

    @BeforeAll
    public void beforeAll() throws Exception {
        application.getApplication().run("db", "migrate", configurations);
        DataSourceFactory dataSourceFactory = application.getConfiguration().getDataSourceFactory();
        jdbi = Jdbi.create(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
        jdbi.installPlugin(new SqlObjectPlugin());
        journalEntryRepository= jdbi.onDemand(JournalEntryRepository .class);

        doBeforeAll(jdbi);
    }

    @BeforeEach
    public void beforeEach() {
        jdbi.withHandle(handle -> handle.createUpdate("delete from accounts"));
        doBeforeEach(jdbi);
    }

    protected static WebTarget newRequest(){
        return application.client().target("http://localhost:" + application.getLocalPort());
    }

    protected static void verifyJournalEntry(UUID trxId, UUID accountId, UUID correlationId, double amount) {
        Optional<JournalEntry> entry = journalEntryRepository.findById(trxId);

        assertTrue(entry.isPresent());

        entry.map(journalEntry -> {
            assertEquals(accountId, journalEntry.getAccountId());
            assertEquals(correlationId, journalEntry.getCorrelationId());
            assertEquals(amount, journalEntry.getAmount());
            return null;//no need to return ..
        });
    }

    protected abstract void doBeforeEach(Jdbi jdbi);

    protected abstract void doBeforeAll(Jdbi jdbi);
}
