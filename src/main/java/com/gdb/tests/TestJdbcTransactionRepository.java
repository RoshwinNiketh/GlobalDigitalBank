package com.gdb.tests;

import com.gdb.db.*;
import com.gdb.domain.*;
import com.gdb.repository.*;
import com.gdb.exceptions.DataAccessException;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;

/** Activity 24 acceptance checks, isolated from application databases. */
public final class TestJdbcTransactionRepository {
    public static void main(String[] args) throws Exception {
        System.out.println("ACTIVITY 24 — JDBC TRANSACTION REPOSITORY & MIGRATION");
        Path database = Files.createTempFile("gdb-activity24-", ".db");
        ConnectionProvider provider = new JdbcConnectionProvider("jdbc:sqlite:" + database);
        try {
            runChecks(provider);
        } finally {
            provider.shutdown();
            Files.deleteIfExists(database);
        }
        System.out.println("ALL ACTIVITY 24 JDBC TRANSACTION TESTS PASSED");
    }

    private static void runChecks(ConnectionProvider provider) throws Exception {
        SchemaInitializer.initialize(provider);
        JdbcAccountRepository accounts = new JdbcAccountRepository(provider);
        JdbcTransactionRepository transactions = new JdbcTransactionRepository(provider);
        accounts.save(AccountFactory.createAccount("SAVINGS", 1001, "Sender", 30, 50000));
        accounts.save(AccountFactory.createAccount("SAVINGS", 1002, "Receiver", 30, 30000));
        testQueries(transactions);
        testTransfer(accounts, transactions);
        testRollback(provider, accounts, transactions);
        testMigration(accounts, transactions);
        testService(accounts, transactions);
    }

    private static Transaction record(int account) {
        return new Transaction(Transaction.generateId(), LocalDateTime.now(), account,
                TransactionType.DEPOSIT, 100, 50000, "SUCCESS", "Migration record", 0, 0);
    }

    private static void testQueries(JdbcTransactionRepository transactions) {
        Transaction original = record(1001);
        transactions.save(original);
        Transaction restored = transactions.findByAccount(1001).get(0);
        check(restored.getTransactionId().equals(original.getTransactionId()), "ID round trip");
        check(restored.getTimestamp().equals(original.getTimestamp()), "Timestamp round trip");
        check(restored.getDescription().equals(original.getDescription()), "Description round trip");
        check(transactions.findByAccount(9999).isEmpty(), "Unknown account history");
        transactions.clear();
        check(transactions.findAll().isEmpty(), "Clear ledger");
        System.out.println("Transaction save, query, and clear: PASSED");
    }

    private static void testTransfer(JdbcAccountRepository accounts, JdbcTransactionRepository transactions) {
        transactions.transfer(1001, 1002, 10000);
        check(accounts.findById(1001).getBalance() == 40000, "Sender debit");
        check(accounts.findById(1002).getBalance() == 40000, "Receiver credit");
        check(transactions.findByAccount(1002).size() == 1, "Recipient audit query");
        check(transactions.findAll().get(0).getBalanceAfter() == 40000, "Audit balance");
        System.out.println("Atomic transfer: PASSED");
    }

    private static void testRollback(ConnectionProvider provider, JdbcAccountRepository accounts,
                                     JdbcTransactionRepository transactions) throws Exception {
        expectFailure(() -> transactions.transfer(1001, 1002, 999999));
        expectFailure(() -> transactions.transfer(1001, 9999, 100));
        try (Connection connection = provider.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER reject_transfer BEFORE INSERT ON transactions BEGIN SELECT RAISE(ABORT, 'Injected ledger failure'); END");
            expectFailure(() -> transactions.transfer(1001, 1002, 100));
            statement.execute("DROP TRIGGER reject_transfer");
        }
        check(accounts.findById(1001).getBalance() == 40000, "Rollback sender debit");
        check(accounts.findById(1002).getBalance() == 40000, "Rollback receiver credit");
        check(transactions.findAll().size() == 1, "No audit after failed transfer");
        System.out.println("Insufficient funds, missing receiver, and ledger failure rollback: PASSED");
    }

    private static void testMigration(JdbcAccountRepository accounts, JdbcTransactionRepository transactions) throws Exception {
        InMemoryAccountRepository sourceAccounts = new InMemoryAccountRepository();
        InMemoryTransactionRepository sourceTransactions = new InMemoryTransactionRepository();
        sourceAccounts.save(AccountFactory.createAccount("SAVINGS", 2001, "Migrated", 30, 50000));
        sourceTransactions.save(record(2001));
        DataMigrator.MigrationSummary summary = DataMigrator.migrate(
                sourceAccounts, accounts, sourceTransactions, transactions);
        check(summary.accounts() == 1 && summary.transactions() == 1, "Migration counts");
        check(accounts.exists(2001), "Migrated account");
        check(transactions.findByAccount(2001).size() == 1, "Migrated transaction");
        System.out.println("Memory to JDBC migration: PASSED");
    }

    private static void expectFailure(Runnable action) {
        try { action.run(); }
        catch (DataAccessException expected) { return; }
        throw new AssertionError("Expected database operation to fail");
    }

    private static void testService(JdbcAccountRepository accounts, JdbcTransactionRepository transactions) throws Exception {
        com.gdb.logging.MemoryLogDestination logs = new com.gdb.logging.MemoryLogDestination();
        com.gdb.service.AccountService service = new com.gdb.service.AccountService(
                accounts, transactions, new com.gdb.logging.TransactionLogger(logs));
        service.setPin(1001, 1234);
        service.deposit(1001, 5000);
        service.withdraw(1001, 1000, 1234);
        service.transfer(1001, 1002, 2000, 1234);
        check(accounts.findById(1001).getBalance() == 42000, "Service persists sender changes");
        check(accounts.findById(1002).getBalance() == 42000, "Service persists receiver changes");
        InMemoryTransactionRepository copiedLogs = new InMemoryTransactionRepository();
        check(DataMigrator.migrate(logs, copiedLogs) == 3, "Legacy log migration count");
        check(copiedLogs.findAll().size() == 3, "Legacy log migration records");
        System.out.println("JDBC service operations and legacy log migration: PASSED");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
