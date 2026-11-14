package com.gdb.db;

import java.sql.Connection;
import java.sql.SQLException;

/** Abstracts database connection acquisition and provider lifecycle. */
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
    void shutdown();
    String getProviderName();
}
