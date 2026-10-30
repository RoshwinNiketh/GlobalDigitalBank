package com.gdb.repository;

import com.gdb.domain.IAccount;
import com.gdb.exceptions.DataAccessException;
import com.gdb.file.*;
import java.util.*;

/** CSV account storage; automatic mode reloads before each operation to avoid stale instance caches. */
public class FileAccountRepository implements AccountRepository {
    private final String filePath;
    private final boolean autosave;
    private final int backups;
    private int nextNumber;
    private Map<Integer, String> rows = new TreeMap<>();

    public FileAccountRepository(String path) { this(path, true, 1001); }
    public FileAccountRepository(String path, int start) { this(path, true, start); }
    public FileAccountRepository(String path, boolean autosave, int start) { this(path, autosave, start, 5); }
    public FileAccountRepository(String path, boolean autosave, int start, int backups) {
        this.filePath = path; this.autosave = autosave; this.nextNumber = start; this.backups = backups;
        load();
    }

    public void load() {
        synchronized (FileStore.LOCK) {
            try {
                FileStore.recover(filePath);
                Map<Integer, String> loaded = new TreeMap<>();
                for (String row : FileStore.read(filePath, CsvMapper.ACCOUNT_HEADER)) {
                    IAccount account = CsvMapper.fromCsv(row);
                    if (loaded.put(account.getAccountNumber(), row) != null) throw new IllegalArgumentException("Duplicate account");
                    nextNumber = Math.max(nextNumber, account.getAccountNumber() + 1);
                }
                rows = loaded;
            } catch (Exception exception) { throw failure(exception); }
        }
    }

    public void flush() {
        synchronized (FileStore.LOCK) {
            try { FileStore.write(filePath, CsvMapper.ACCOUNT_HEADER, new ArrayList<>(rows.values()), backups); }
            catch (Exception exception) { load(); throw failure(exception); }
        }
    }

    private void refresh() { if (autosave) load(); }
    private void persist() { if (autosave) flush(); }

    @Override public void save(IAccount account) {
        Objects.requireNonNull(account);
        synchronized (FileStore.LOCK) {
            refresh();
            if (rows.containsKey(account.getAccountNumber())) throw new IllegalArgumentException("Account already exists");
            rows.put(account.getAccountNumber(), CsvMapper.toCsv(account)); persist();
        }
    }
    @Override public IAccount findById(int number) {
        synchronized (FileStore.LOCK) { refresh(); return rows.containsKey(number) ? CsvMapper.fromCsv(rows.get(number)) : null; }
    }
    @Override public List<IAccount> findAll() {
        synchronized (FileStore.LOCK) { refresh(); return rows.values().stream().map(CsvMapper::fromCsv).toList(); }
    }
    @Override public void update(IAccount account) {
        Objects.requireNonNull(account);
        synchronized (FileStore.LOCK) {
            refresh();
            if (rows.containsKey(account.getAccountNumber())) { rows.put(account.getAccountNumber(), CsvMapper.toCsv(account)); persist(); }
        }
    }
    @Override public void delete(int number) {
        synchronized (FileStore.LOCK) { refresh(); if (rows.remove(number) != null) persist(); }
    }
    @Override public boolean exists(int number) {
        synchronized (FileStore.LOCK) { refresh(); return rows.containsKey(number); }
    }
    @Override public int nextAccountNumber() {
        synchronized (FileStore.LOCK) { refresh(); return nextNumber++; }
    }
    @Override public int getNextAccountNumber() {
        synchronized (FileStore.LOCK) { refresh(); return nextNumber; }
    }
    private DataAccessException failure(Exception cause) { return new DataAccessException("Unable to access account file " + filePath, cause); }
}
