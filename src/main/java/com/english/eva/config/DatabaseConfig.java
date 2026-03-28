package com.english.eva.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private final DataSource dataSource;
    private final DSLContext dsl;

    public DatabaseConfig(String dbFilePath) {
        this.dataSource = createDataSource(dbFilePath);
        runMigrations();
        this.dsl = DSL.using(dataSource, SQLDialect.H2);
        log.info("Database initialized: {}", dbFilePath);
    }

    private DataSource createDataSource(String dbFilePath) {
        var ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:file:" + dbFilePath);
        ds.setUser("sa");
        ds.setPassword("password");
        return ds;
    }

    private void runMigrations() {
        Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .locations("classpath:db/migration")
                .load()
                .migrate();
        log.info("Flyway migrations complete");
    }

    public DSLContext getDsl() {
        return dsl;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
