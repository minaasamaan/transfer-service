package com.mybank.transferservice;

import com.mybank.transferservice.resource.TransferResource;
import com.mybank.transferservice.service.TransferService;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;

public class TransferApplication extends Application<TransferAppConfiguration> {
    public static void main(String[] args) throws Exception {
        new TransferApplication().run(args);
    }

    @Override
    public String getName() {
        return "transfer-service";
    }

    @Override
    public void initialize(Bootstrap<TransferAppConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<TransferAppConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(TransferAppConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(TransferAppConfiguration configuration, Environment environment) {
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "transfer-service");
        environment.jersey().register(new TransferResource(new TransferService(jdbi)));
    }
}
