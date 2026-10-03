package com.gdb.tests;

import com.gdb.db.ConnectionProvider;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.SchemaInitializer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/** Connection and schema acceptance checks for Activity 22. */
public class TestJdbcConnection {
    private static final String TEST_DATABASE_URL = "jdbc:sqlite:test_gdb.db";

    private TestJdbcConnection() {
    }

    public static void main(String[] args) throws Exception {
        printHeader();
        ConnectionProvider provider = new JdbcConnectionProvider(TEST_DATABASE_URL);
        try {
            testConnection(provider);
            testSchemaInitialization(provider);
            testSchemaColumns(provider);
        } finally {
            provider.shutdown();
        }
        System.out.println("\nALL ACTIVITY 22 JDBC CONNECTION TESTS PASSED");
    }

    private static void printHeader() {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 22 — JDBC FOUNDATION (CONNECTION & SCHEMA)");
        System.out.println("=".repeat(60));
    }

    private static void testConnection(ConnectionProvider provider) throws Exception {
        System.out.println("\n[TEST 1] Database Connection Establishment:");
        try (Connection connection = provider.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT 1")) {
            check(result.next() && result.getInt(1) == 1, "SELECT 1 should return 1");
            DatabaseMetaData metadata = connection.getMetaData();
            System.out.println("  Connected to: " + metadata.getURL());
            System.out.println("  Driver Name: " + metadata.getDriverName());
        }
        System.out.println("  -> PASSED");
    }

    private static void testSchemaInitialization(ConnectionProvider provider) throws Exception {
        System.out.println("\n[TEST 2] Schema DDL Execution:");
        SchemaInitializer.initialize(provider);
        SchemaInitializer.initialize(provider);
        try (Connection connection = provider.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name IN ('accounts', 'transactions')")) {
            Set<String> tables = new HashSet<>();
            while (result.next()) {
                tables.add(result.getString("name"));
            }
            check(tables.contains("accounts"), "accounts table should be created");
            check(tables.contains("transactions"), "transactions table should be created");
        }
        System.out.println("  Created tables: accounts, transactions.");
        System.out.println("  -> PASSED");
    }

    private static void testSchemaColumns(ConnectionProvider provider) throws Exception {
        System.out.println("\n[TEST 3] Schema Verification:");
        checkColumns(provider, "accounts", Set.of(
                "account_number", "account_holder_name", "age", "balance",
                "account_type", "status", "pin", "opening_date"));
        checkColumns(provider, "transactions", Set.of(
                "transaction_id", "timestamp", "account_number", "type", "amount",
                "balance_after", "status", "from_account", "to_account", "description"));
        System.out.println("  Required columns found in both tables.");
        System.out.println("  -> PASSED");
    }

    private static void checkColumns(ConnectionProvider provider, String table,
                                     Set<String> expectedColumns) throws Exception {
        Set<String> actualColumns = new HashSet<>();
        try (Connection connection = provider.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (result.next()) {
                actualColumns.add(result.getString("name"));
            }
        }
        check(actualColumns.containsAll(expectedColumns),
                "Table " + table + " is missing columns: " + difference(expectedColumns, actualColumns));
    }

    private static Set<String> difference(Set<String> expected, Set<String> actual) {
        Set<String> missing = new HashSet<>(expected);
        missing.removeAll(actual);
        return missing;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
