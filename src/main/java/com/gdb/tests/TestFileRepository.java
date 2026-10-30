package com.gdb.tests;

import com.gdb.domain.*;
import com.gdb.file.*;
import com.gdb.file.FileStore;
import com.gdb.repository.*;
import com.gdb.exceptions.DataAccessException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/** Activity 26 checks use an isolated temporary directory and keep main as orchestration. */
public final class TestFileRepository {
    private TestFileRepository() { }

    public static void main(String[] args) throws Exception {
        System.out.println("ACTIVITY 26 — FILE REPOSITORY (CSV + ATOMIC WRITES + BACKUPS)");
        Path directory = Files.createTempDirectory("gdb-files-");
        try {
            runChecks(directory);
        } finally {
            cleanDirectory(directory);
        }
        System.out.println("ALL ACTIVITY 26 FILE REPOSITORY TESTS PASSED");
    }

    private static void runChecks(Path directory) throws Exception {
        String accountPath = directory.resolve("accounts.csv").toString();
        String ledgerPath = directory.resolve("transactions.csv").toString();
        FileAccountRepository accounts = new FileAccountRepository(accountPath);
        FileTransactionRepository ledger = new FileTransactionRepository(ledgerPath);
        testAccounts(accountPath, accounts);
        testTransactions(ledgerPath, ledger);
        testTransfers(accounts, ledger);
        testBackups(directory, accounts, accountPath);
        testRecovery(directory, accounts, accountPath, ledgerPath);
        testManualFlush(directory);
        testService(accounts, ledger);
    }

    private static void testAccounts(String path, FileAccountRepository accounts) throws Exception {
        IAccount account = AccountFactory.createAccount("SAVINGS", accounts.nextAccountNumber(), "Doe, \"Jane\"\nCustomer", 30, 50000, 3);
        account.setPin(1234);
        accounts.save(account);
        FileAccountRepository reopened = new FileAccountRepository(path);
        IAccount found = reopened.findById(1001);
        check(found.getAccountHolderName().equals(account.getAccountHolderName()), "Quoted multiline name round trip");
        check(found.verifyPin(1234) && found.getTenureYears() == 3, "PIN and tenure round trip");
        found.deposit(10000); reopened.update(found);
        check(accounts.findById(1001).getBalance() == 60000, "Updates visible across repository instances");
        found.deposit(1);
        check(accounts.findById(1001).getBalance() == 60000, "Reads return detached accounts");
        accounts.save(AccountFactory.createAccount("CURRENT", 1002, "Receiver", 30, 40000));
        accounts.save(AccountFactory.createAccount("SALARY", 1003, "Salary Customer", 30, 0));
        check(accounts.findById(1003) instanceof SalaryAccount, "Salary mapping");
        accounts.delete(1003);
        check(!accounts.exists(1003), "Delete persistence");
        check(reopened.nextAccountNumber() >= 1003, "Account number follows stored IDs");
        System.out.println("Account CRUD, CSV escaping, reload, and defensive reads: PASSED");
    }

    private static void testTransactions(String path, FileTransactionRepository ledger) {
        Transaction original = new Transaction(Transaction.generateId(), LocalDateTime.now(), 1001,
                TransactionType.TRANSFER, 100, 60000, "SUCCESS", "Comma, quote \" and\nnewline", 1001, 1002);
        ledger.save(original);
        Transaction found = new FileTransactionRepository(path).findByAccount(1002).get(0);
        check(found.getDescription().equals(original.getDescription()), "Description round trip");
        check(found.getTimestamp().equals(original.getTimestamp()), "Timestamp round trip");
        found.setAmount(999);
        check(ledger.findAll().get(0).getAmount() == 100, "Detached ledger read");
        ledger.clear();
        check(new FileTransactionRepository(path).findAll().isEmpty(), "Clear persistence");
        System.out.println("Transaction save, queries, escaping, and clear: PASSED");
    }

    private static void testTransfers(FileAccountRepository accounts, FileTransactionRepository ledger) {
        ledger.transfer(1001, 1002, 5000);
        check(accounts.findById(1001).getBalance() == 55000, "Sender debit");
        check(accounts.findById(1002).getBalance() == 45000, "Receiver credit");
        expectFailure(() -> ledger.transfer(1001, 9999, 100));
        expectFailure(() -> ledger.transfer(1001, 1002, 999999));
        check(accounts.findById(1001).getBalance() == 55000, "Failed transfer leaves sender unchanged");
        check(ledger.findAll().size() == 1, "Failed transfer leaves ledger unchanged");
        System.out.println("Journaled transfer and rejected transfer consistency: PASSED");
    }

    private static void testBackups(Path directory, FileAccountRepository accounts, String path) throws Exception {
        for (int index = 0; index < 8; index++) {
            IAccount account = accounts.findById(1001);
            accounts.update(account);
        }
        try (var files = Files.list(directory)) {
            check(files.filter(file -> file.getFileName().toString().startsWith("accounts.csv.bak.")).count() == 5,
                    "Backup retention should be five");
        }
        String before = Files.readString(Path.of(path));
        try {
            AtomicFileWriter.write(path, Arrays.asList("invalid", null));
            throw new AssertionError("Null line should fail writing");
        } catch (NullPointerException expected) {
            check(Files.readString(Path.of(path)).equals(before), "Failed temporary write preserves original");
        }
        System.out.println("Backup rotation and failed-write preservation: PASSED");
    }

    private static void testRecovery(Path directory, FileAccountRepository accounts, String accountPath, String ledgerPath) throws Exception {
        String accountText = Files.readString(Path.of(accountPath));
        String ledgerText = Files.readString(Path.of(ledgerPath));
        List<String> journal = List.of(encode(Path.of(accountPath).toAbsolutePath().toString()),
                encode(Path.of(ledgerPath).toAbsolutePath().toString()), encode(accountText), encode(ledgerText));
        AtomicFileWriter.write(FileStore.journalPath(accountPath), journal);
        AtomicFileWriter.write(ledgerPath, List.of(CsvMapper.TRANSACTION_HEADER));
        check(accounts.findById(1001).getBalance() == 55000, "Recovery preserves committed accounts");
        check(new FileTransactionRepository(ledgerPath).findAll().size() == 1, "Recovery restores committed ledger");
        check(!Files.exists(Path.of(FileStore.journalPath(accountPath))), "Recovery removes completed journal");
        System.out.println("Interrupted two-file commit recovery: PASSED");
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static void testManualFlush(Path directory) throws Exception {
        String path = directory.resolve("manual.csv").toString();
        FileAccountRepository manual = new FileAccountRepository(path, false, 3001);
        manual.save(AccountFactory.createAccount("SAVINGS", manual.nextAccountNumber(), "Manual", 30, 50000));
        check(!Files.exists(Path.of(path)), "Manual mode defers writes");
        manual.flush();
        check(new FileAccountRepository(path).exists(3001), "Explicit flush persists cache");
        System.out.println("Manual flush: PASSED");
    }

    private static void testService(FileAccountRepository accounts, FileTransactionRepository ledger) throws Exception {
        var service = new com.gdb.service.AccountService(accounts, ledger,
                new com.gdb.logging.TransactionLogger(new com.gdb.logging.MemoryLogDestination()));
        service.deposit(1001, 1000);
        service.withdraw(1001, 500, 1234);
        service.transfer(1001, 1002, 500, 1234);
        check(accounts.findById(1001).getBalance() == 55000, "File service sender balance");
        check(accounts.findById(1002).getBalance() == 45500, "File service receiver balance");
        check(ledger.findAll().size() == 4, "File service audit records");
        System.out.println("Service operations over file repositories: PASSED");
    }

    private static void expectFailure(Runnable action) {
        try { action.run(); } catch (DataAccessException expected) { return; }
        throw new AssertionError("Expected transfer failure");
    }

    private static void cleanDirectory(Path directory) throws Exception {
        Path resolved = directory.toAbsolutePath().normalize();
        if (!resolved.getFileName().toString().startsWith("gdb-files-")) throw new IllegalArgumentException("Unexpected cleanup path");
        try (var files = Files.walk(resolved)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
