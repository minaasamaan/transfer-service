package com.mybank.transferservice;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.mybank.transferservice.resource.TransferResource;
import com.mybank.transferservice.service.TransferService;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.jdbi.v3.core.Jdbi;

public class TransferApplication extends Application<TransferAppConfiguration> {

    private static final String[] db_migration_arguments = new String[]{"db", "migrate", "src/main/resources/application.yml"};
    private static final String app_name = "transfer-service";

    public static void main(String[] args) throws Exception {
        new TransferApplication().run(args);
    }

    @Override
    public String getName() {
        return app_name;
    }

    @Override
    public void initialize(Bootstrap<TransferAppConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<>() {
            @Override
            public DataSourceFactory getDataSourceFactory(TransferAppConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });

        bootstrap.addBundle(new SwaggerBundle<>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(TransferAppConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    public void run(TransferAppConfiguration configuration, Environment environment) throws Exception {

        run(db_migration_arguments);

        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), app_name);

        environment.getObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        environment.jersey().register(new TransferResource(new TransferService(jdbi)));
    }
}
