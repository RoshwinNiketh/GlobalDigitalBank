package com.gdb.tests;

import com.gdb.db.ConnectionProvider;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.SchemaInitializer;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.CurrentAccount;
import com.gdb.domain.FixedDepositAccount;
import com.gdb.domain.IAccount;
import com.gdb.repository.JdbcAccountRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** CRUD checks for Activity 23's JDBC account repository. */
public final class TestJdbcAccountRepository {
    private static final Path TEST_DATABASE = Path.of("test_acc_gdb.db");
    private static final String TEST_DATABASE_URL = "jdbc:sqlite:" + TEST_DATABASE;

    private TestJdbcAccountRepository() {
    }

    public static void main(String[] args) throws Exception {
        printHeader();
        Files.deleteIfExists(TEST_DATABASE);
        try {
            runRepositoryChecks();
        } finally {
            Files.deleteIfExists(TEST_DATABASE);
        }
        System.out.println("\nALL ACTIVITY 23 JDBC ACCOUNT REPOSITORY TESTS PASSED");
    }

    private static void printHeader() {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 23 — JDBC ACCOUNT REPOSITORY CRUD TESTS");
        System.out.println("=".repeat(60));
    }

    private static void runRepositoryChecks() throws Exception {
        ConnectionProvider provider = new JdbcConnectionProvider(TEST_DATABASE_URL);
        try {
            SchemaInitializer.initialize(provider);
            JdbcAccountRepository repository = new JdbcAccountRepository(provider);
            testSaveAndFind(repository);
            testUpdateAndFindAll(repository);
            testDeleteAndExists(repository);
            testNextAccountNumber(repository);
            testSubtypeState(repository);
        } finally {
            provider.shutdown();
        }
    }

    private static void testSaveAndFind(JdbcAccountRepository repository) throws Exception {
        System.out.println("\n[TEST 1] Save and find account:");
        IAccount account = AccountFactory.createAccount("SAVINGS", 1001,
                "Rajesh Sharma", 30, 50000, 3);
        account.setPin(1234);
        repository.save(account);
        IAccount found = repository.findById(1001);
        check(found != null, "Saved account should be found");
        check(found.getAccountNumber() == 1001, "Account number should be preserved");
        check(found.getBalance() == 50000, "Balance should be preserved");
        check(found.verifyPin(1234), "PIN should be preserved");
        check(found.getTenureYears() == 3, "Tenure should be preserved");
        System.out.println("  -> PASSED");
    }

    private static void testUpdateAndFindAll(JdbcAccountRepository repository) throws Exception {
        System.out.println("\n[TEST 2] Update and find all:");
        IAccount first = repository.findById(1001);
        first.deposit(15000);
        repository.update(first);
        check(repository.findById(1001).getBalance() == 65000,
                "Updated balance should be persisted");
        repository.save(AccountFactory.createAccount("CURRENT", 1002,
                "Priya Patel", 28, 40000));
        List<IAccount> accounts = repository.findAll();
        check(accounts.size() == 2, "findAll should return both accounts");
        check(accounts.get(1).getAccountNumber() == 1002,
                "findAll should be ordered by account number");
        System.out.println("  -> PASSED");
    }

    private static void testDeleteAndExists(JdbcAccountRepository repository) {
        System.out.println("\n[TEST 3] Delete and exists:");
        check(repository.exists(1002), "Saved account should exist");
        repository.delete(1002);
        check(!repository.exists(1002), "Deleted account should not exist");
        check(repository.findById(1002) == null, "Deleted lookup should return null");
        System.out.println("  -> PASSED");
    }

    private static void testNextAccountNumber(JdbcAccountRepository repository) {
        System.out.println("\n[TEST 4] Next account number:");
        check(repository.nextAccountNumber() == 1002,
                "Next account number should follow the highest stored number");
        repository.delete(1001);
        check(repository.nextAccountNumber() == 1001,
                "Empty table should start at account number 1001");
        System.out.println("  -> PASSED");
    }

    private static void testSubtypeState(JdbcAccountRepository repository) throws Exception {
        System.out.println("\n[TEST 5] Account subtype state:");
        CurrentAccount current = (CurrentAccount) AccountFactory.createAccount(
                "CURRENT", 2001, "Current Customer", 35, 30000, 0);
        current.setPin(2468);
        current.withdraw(6000, 2468);
        current.closeAccount();
        repository.save(current);
        CurrentAccount restoredCurrent = (CurrentAccount) repository.findById(2001);
        check(restoredCurrent.getOverdraftUsed() == 1000,
                "Overdraft usage should be persisted");
        check(!restoredCurrent.isActive(), "Inactive status should be persisted");
        check(restoredCurrent.verifyPin(2468), "PIN should survive account state updates");

        FixedDepositAccount fixedDeposit = (FixedDepositAccount) AccountFactory.createAccount(
                "FIXEDDEPOSIT", 2002, "FD Customer", 35, 30000, 0);
        fixedDeposit.applyMonthlyInterest();
        repository.save(fixedDeposit);
        FixedDepositAccount restoredDeposit = (FixedDepositAccount) repository.findById(2002);
        check(restoredDeposit.getInterestEarned() > 0,
                "Interest earned should be persisted");
        System.out.println("  -> PASSED");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
