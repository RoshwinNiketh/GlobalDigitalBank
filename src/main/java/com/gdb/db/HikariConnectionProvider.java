package com.gdb.db;

import com.gdb.exceptions.DataAccessException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/** Adapts a HikariCP pool to the application's connection provider contract. */
public final class HikariConnectionProvider implements ConnectionProvider, AutoCloseable {
    private final HikariDataSource dataSource;

    public HikariConnectionProvider(Properties properties) {
        Objects.requireNonNull(properties, "Persistence configuration is required");
        try {
            dataSource = new HikariDataSource(createConfig(properties));
        } catch (RuntimeException exception) {
            throw new DataAccessException("Unable to initialize JDBC connection pool", exception);
        }
    }

    private static HikariConfig createConfig(Properties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("persistence.db.url", "jdbc:sqlite:gdb.db").trim());
        config.setDriverClassName(properties.getProperty("persistence.db.driver", "org.sqlite.JDBC").trim());
        config.setMaximumPoolSize(Integer.parseInt(setting(properties, "size", "10")));
        config.setMinimumIdle(1);
        config.setConnectionTimeout(Long.parseLong(setting(properties, "timeout", "30000")));
        config.setIdleTimeout(Long.parseLong(setting(properties, "idleTimeout", "600000")));
        return config;
    }

    private static String setting(Properties properties, String key, String fallback) {
        return properties.getProperty("persistence.pool." + key,
                properties.getProperty("db.pool." + key, fallback)).trim();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() {
        dataSource.close();
    }

    @Override
    public void close() {
        shutdown();
    }

    @Override
    public String getProviderName() {
        return "HikariConnectionProvider [Pool size: " + dataSource.getMaximumPoolSize() + "]";
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
