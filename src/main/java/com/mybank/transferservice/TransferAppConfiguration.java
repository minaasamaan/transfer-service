package com.mybank.transferservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class TransferAppConfiguration extends Configuration implements DatabaseConfiguration<TransferAppConfiguration> {

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    @Override
    public PooledDataSourceFactory getDataSourceFactory(TransferAppConfiguration transferAppConfiguration) {
        return getDataSourceFactory();
    }
}
