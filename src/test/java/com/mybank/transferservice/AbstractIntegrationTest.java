package com.mybank.transferservice;

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

@ExtendWith(DropwizardExtensionsSupport.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {
    private static final String configurations = ResourceHelpers.resourceFilePath("application-test.yml");

    public static final DropwizardAppExtension<TransferAppConfiguration> application = new DropwizardAppExtension<>(
            TransferApplication.class, configurations);

    private static Jdbi jdbi;

    @BeforeAll
    public void beforeAll() throws Exception {
        application.getApplication().run("db", "migrate", configurations);
        DataSourceFactory dataSourceFactory = application.getConfiguration().getDataSourceFactory();
        jdbi = Jdbi.create(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
        jdbi.installPlugin(new SqlObjectPlugin());
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

    protected abstract void doBeforeEach(Jdbi jdbi);

    protected abstract void doBeforeAll(Jdbi jdbi);
}
