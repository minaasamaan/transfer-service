package com.mybank.transferservice;

import com.mybank.transferservice.model.Account;
import com.mybank.transferservice.model.JournalEntry;
import com.mybank.transferservice.repository.AccountRepository;
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
    private static AccountRepository accountRepository;

    protected static WebTarget newRequest() {
        return application.client().target("http://localhost:" + application.getLocalPort());
    }

    protected static Account createAccount(UUID id, double initialBalance) {
        Account account = Account.builder().id(UUID.randomUUID()).balance(initialBalance).build();
        accountRepository.create(account);
        return account;
    }

    protected static void verifyAccountBalance(UUID id, double expectedBalance) {
        assertEquals(accountRepository.findById(id).get().getBalance(), expectedBalance);
    }

    protected static void verifyJournalEntry(UUID trxId, UUID accountId, UUID correlationId, double amount) {
        Optional<JournalEntry> entry = journalEntryRepository.findById(trxId);

        assertTrue(entry.isPresent());

        entry.map(journalEntry -> {
            assertEquals(accountId, journalEntry.getAccountId());
            assertEquals(correlationId, journalEntry.getCorrelationId());
            assertEquals(amount, journalEntry.getAmount());
            return null;//no need to return value..
        });
    }

    @BeforeAll
    public void beforeAll() throws Exception {

        //execute migrations
        application.getApplication().run("db", "migrate", configurations);

        //initialize Jdbi instance
        DataSourceFactory dataSourceFactory = application.getConfiguration().getDataSourceFactory();
        jdbi = Jdbi.create(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
        jdbi.installPlugin(new SqlObjectPlugin());

        //initialize dao
        accountRepository = jdbi.onDemand(AccountRepository.class);
        journalEntryRepository = jdbi.onDemand(JournalEntryRepository.class);

        //do specific stuff here...
        doBeforeAll(jdbi);
    }

    @BeforeEach
    public void beforeEach() {
        jdbi.withHandle(handle -> handle.createUpdate("delete from journal_entries"));
        jdbi.withHandle(handle -> handle.createUpdate("delete from accounts"));
    }

    protected void doBeforeAll(Jdbi jdbi) {
    }
}
