package com.gdb.repository;

import com.gdb.domain.*;
import com.gdb.exceptions.DataAccessException;
import com.gdb.file.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/** CSV ledger with journaled two-file transfers. */
public class FileTransactionRepository implements TransactionRepository {
    private final String filePath;
    private final String accountPath;
    private final boolean autosave;
    private final int backups;
    private List<String> rows = new ArrayList<>();

    public FileTransactionRepository(String path) { this(path, true, 1); }
    public FileTransactionRepository(String path, int startId) { this(path, true, startId); }
    public FileTransactionRepository(String path, boolean autosave, int startId) {
        this(path, Path.of(path).resolveSibling("accounts.csv").toString(), autosave, 5);
    }
    public FileTransactionRepository(String path, String accounts, boolean autosave, int backups) {
        this.filePath = path; this.accountPath = accounts; this.autosave = autosave; this.backups = backups;
        load();
    }
    public void load() {
        synchronized (FileStore.LOCK) {
            try {
                FileStore.recover(accountPath);
                List<String> loaded = FileStore.read(filePath, CsvMapper.TRANSACTION_HEADER);
                Set<String> ids = new HashSet<>();
                for (String row : loaded) {
                    if (!ids.add(CsvMapper.fromCsvTransaction(row).getTransactionId())) throw new IllegalArgumentException("Duplicate transaction");
                }
                rows = loaded;
            } catch (Exception exception) { throw failure(exception); }
        }
    }
    public void flush() {
        synchronized (FileStore.LOCK) {
            try { FileStore.write(filePath, CsvMapper.TRANSACTION_HEADER, rows, backups); }
            catch (Exception exception) { load(); throw failure(exception); }
        }
    }
    private void refresh() { if (autosave) load(); }
    @Override public void save(Transaction transaction) {
        Objects.requireNonNull(transaction);
        synchronized (FileStore.LOCK) {
            refresh();
            if (findAll().stream().anyMatch(existing -> Objects.equals(existing.getTransactionId(), transaction.getTransactionId()))) {
                throw new IllegalArgumentException("Transaction already exists");
            }
            rows.add(CsvMapper.toCsv(transaction)); if (autosave) flush();
        }
    }
    @Override public List<Transaction> findAll() {
        synchronized (FileStore.LOCK) {
            refresh(); return rows.stream().map(CsvMapper::fromCsvTransaction).toList();
        }
    }
    @Override public List<Transaction> findByAccount(int number) {
        return findAll().stream().filter(transaction -> transaction.getAccountNumber() == number
                || transaction.getFromAccount() == number || transaction.getToAccount() == number).toList();
    }
    @Override public void clear() {
        synchronized (FileStore.LOCK) { refresh(); rows.clear(); if (autosave) flush(); }
    }
    @Override public void transfer(int from, int to, double amount) { transferAndRecord(from, to, amount); }

    public Transaction transferAndRecord(int from, int to, double amount) {
        if (!autosave) throw new IllegalStateException("Transfers require automatic persistence");
        if (from == to || !Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Invalid transfer");
        synchronized (FileStore.LOCK) {
            load();
            try {
                List<IAccount> accounts = FileStore.read(accountPath, CsvMapper.ACCOUNT_HEADER).stream().map(CsvMapper::fromCsv).toList();
                Account sender = (Account) accounts.stream().filter(account -> account.getAccountNumber() == from).findFirst().orElseThrow();
                Account receiver = (Account) accounts.stream().filter(account -> account.getAccountNumber() == to).findFirst().orElseThrow();
                if (!sender.isActive() || !receiver.isActive() || sender.getBalance() < amount) throw new IllegalArgumentException("Inactive account or insufficient balance");
                sender.restoreBalance(sender.getBalance() - amount);
                receiver.restoreBalance(receiver.getBalance() + amount);
                Transaction transaction = new Transaction(Transaction.generateId(), LocalDateTime.now(), from,
                        TransactionType.TRANSFER, amount, sender.getBalance(), "SUCCESS", "Transfer to account #" + to, from, to);
                List<String> ledger = new ArrayList<>(rows); ledger.add(CsvMapper.toCsv(transaction));
                FileStore.commitPair(accountPath, accounts.stream().map(CsvMapper::toCsv).toList(), filePath, ledger, backups);
                rows = ledger;
                return transaction;
            } catch (Exception exception) { throw failure(exception); }
        }
    }
    private DataAccessException failure(Exception cause) { return new DataAccessException("Unable to access transaction file " + filePath, cause); }
}
