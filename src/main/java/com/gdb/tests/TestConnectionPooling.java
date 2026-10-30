package com.gdb.tests;

import com.gdb.db.*;
import com.gdb.domain.AccountFactory;
import com.gdb.exceptions.DataAccessException;
import com.gdb.repository.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.*;

/** Pool lifecycle, repository correctness, and connection acquisition measurements. */
public final class TestConnectionPooling {
    private TestConnectionPooling() { }

    public static void main(String[] args) throws Exception {
        System.out.println("ACTIVITY 25 — CONNECTION POOLING WITH HIKARICP");
        Path database = Files.createTempFile("gdb-pool-", ".db");
        try {
            runChecks(configuration(database));
        } finally {
            Files.deleteIfExists(database);
        }
        System.out.println("ALL ACTIVITY 25 CONNECTION POOLING FUNCTIONAL TESTS PASSED");
    }

    private static Properties configuration(Path database) {
        Properties properties = new Properties();
        properties.setProperty("persistence.db.url", "jdbc:sqlite:" + database);
        properties.setProperty("persistence.pool.enabled", "true");
        properties.setProperty("persistence.pool.size", "1");
        properties.setProperty("persistence.pool.timeout", "1000");
        return properties;
    }

    private static void runChecks(Properties properties) throws Exception {
        ConnectionProvider selected = RepositoryFactory.createConnectionProvider(properties);
        check(selected instanceof HikariConnectionProvider, "Factory should select pooled provider");
        HikariConnectionProvider pooled = (HikariConnectionProvider) selected;
        try {
            testBorrowReturnAndLimits(pooled);
            testRepositoryOperations(pooled);
            testRollbackReset(pooled);
            testDirectSelection(properties);
            benchmark(properties, pooled);
        } finally {
            pooled.shutdown();
        }
        testShutdown(pooled);
    }

    private static void testBorrowReturnAndLimits(HikariConnectionProvider pooled) throws Exception {
        check(pooled.getDataSource().getMaximumPoolSize() == 1, "Configured pool size");
        check(pooled.getDataSource().getConnectionTimeout() == 1000, "Configured timeout");
        try (Connection borrowed = pooled.getConnection()) {
            executeQuery(borrowed);
            try {
                Connection unexpected = pooled.getConnection();
                unexpected.close();
                throw new AssertionError("Exhausted pool should time out");
            } catch (SQLTransientConnectionException expected) {
                // The sole connection is still held by this test.
            }
        }
        try (ExecutorService workers = Executors.newFixedThreadPool(4)) {
            java.util.List<Future<?>> results = new java.util.ArrayList<>();
            for (int index = 0; index < 8; index++) {
                results.add(workers.submit(() -> {
                    try (Connection connection = pooled.getConnection()) {
                        executeQuery(connection);
                    } catch (SQLException exception) {
                        throw new RuntimeException(exception);
                    }
                }));
            }
            for (Future<?> result : results) result.get(10, TimeUnit.SECONDS);
        }
        check(pooled.getDataSource().getHikariPoolMXBean().getTotalConnections() == 1,
                "Borrowed connections should be returned and reused within pool limit");
        System.out.println("Pool configuration, exhaustion, return, and concurrent borrowing: PASSED");
    }

    private static void testRepositoryOperations(ConnectionProvider provider) throws Exception {
        JdbcAccountRepository accounts = new JdbcAccountRepository(provider);
        JdbcTransactionRepository transactions = new JdbcTransactionRepository(provider);
        accounts.save(AccountFactory.createAccount("SAVINGS", 1001, "Sender", 30, 50000));
        accounts.save(AccountFactory.createAccount("SAVINGS", 1002, "Receiver", 30, 30000));
        transactions.transfer(1001, 1002, 10000);
        check(accounts.findById(1001).getBalance() == 40000, "Pooled sender debit");
        check(accounts.findById(1002).getBalance() == 40000, "Pooled recipient credit");
        check(transactions.findAll().size() == 1, "Pooled audit insert");
        try {
            transactions.transfer(1001, 9999, 1000);
            throw new AssertionError("Missing recipient should fail");
        } catch (DataAccessException expected) {
            check(accounts.findById(1001).getBalance() == 40000, "Pooled rollback");
        }
        System.out.println("Repository operations and transfer rollback over pool: PASSED");
    }

    private static void testRollbackReset(ConnectionProvider provider) throws Exception {
        try (Connection connection = provider.getConnection(); Statement statement = connection.createStatement()) {
            check(connection.getAutoCommit(), "Borrowed connection should start in auto-commit mode");
            connection.setAutoCommit(false);
            statement.executeUpdate("UPDATE accounts SET balance = 1 WHERE account_number = 1001");
            // Closing without commit must roll back before the connection is reused.
        }
        try (Connection connection = provider.getConnection()) {
            check(connection.getAutoCommit(), "Pool should reset auto-commit");
        }
        check(new JdbcAccountRepository(provider).findById(1001).getBalance() == 40000,
                "Pool should roll back uncommitted changes");
        System.out.println("Connection state reset and uncommitted rollback: PASSED");
    }

    private static void testDirectSelection(Properties properties) throws Exception {
        Properties directSettings = new Properties();
        directSettings.putAll(properties);
        directSettings.setProperty("persistence.pool.enabled", "false");
        ConnectionProvider direct = RepositoryFactory.createConnectionProvider(directSettings);
        try {
            check(direct instanceof JdbcConnectionProvider, "Disabled pooling selects direct JDBC");
            try (Connection connection = direct.getConnection()) { executeQuery(connection); }
        } finally {
            direct.shutdown();
        }
        System.out.println("Direct JDBC configuration switch: PASSED");
    }

    private static void benchmark(Properties properties, ConnectionProvider pooled) throws Exception {
        ConnectionProvider direct = new JdbcConnectionProvider(properties.getProperty("persistence.db.url"));
        try {
            measure(direct, 20);
            measure(pooled, 20);
            int cycles = 200;
            long directNanos = measure(direct, cycles);
            long pooledNanos = measure(pooled, cycles);
            System.out.printf("Connection + SELECT 1 benchmark (%d cycles): direct %.3f ms, pooled %.3f ms, ratio %.2fx%n",
                    cycles, directNanos / 1e6, pooledNanos / 1e6, (double) directNanos / pooledNanos);
            System.out.println("Timing is observational; no fixed speedup is guaranteed for local SQLite.");
        } finally {
            direct.shutdown();
        }
    }

    private static long measure(ConnectionProvider provider, int cycles) throws SQLException {
        long start = System.nanoTime();
        for (int index = 0; index < cycles; index++) {
            try (Connection connection = provider.getConnection()) { executeQuery(connection); }
        }
        return System.nanoTime() - start;
    }

    private static void executeQuery(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SELECT 1")) {
            check(result.next() && result.getInt(1) == 1, "Working connection");
        }
    }

    private static void testShutdown(HikariConnectionProvider pooled) throws SQLException {
        pooled.shutdown();
        check(pooled.getDataSource().isClosed(), "Shutdown should close pool");
        try {
            Connection unexpected = pooled.getConnection();
            unexpected.close();
            throw new AssertionError("Closed pool should reject borrowing");
        } catch (SQLException expected) {
            System.out.println("Pool shutdown: PASSED");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
