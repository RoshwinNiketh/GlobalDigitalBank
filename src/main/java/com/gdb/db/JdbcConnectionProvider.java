package com.gdb.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/** Acquires JDBC connections directly through DriverManager. */
public class JdbcConnectionProvider implements ConnectionProvider {
    private final String url;
    private final String driver;

    public JdbcConnectionProvider(String url, String driver) {
        this.url = Objects.requireNonNull(url, "JDBC URL is required");
        this.driver = Objects.requireNonNull(driver, "JDBC driver is required");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("JDBC driver not found: " + driver, exception);
        }
    }

    public JdbcConnectionProvider(String url) {
        this(url, "org.sqlite.JDBC");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public void shutdown() {
        // DriverManager creates independent connections; this provider owns no pool.
    }

    @Override
    public String getProviderName() {
        return "JdbcConnectionProvider [" + url + "]";
    }

    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }
}
