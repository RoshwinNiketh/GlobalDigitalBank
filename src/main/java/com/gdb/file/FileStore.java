package com.gdb.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Shared JVM lock and redo journal for a pair of CSV files. One JVM owns each data directory. */
public final class FileStore {
    public static final Object LOCK = new Object();
    private FileStore() { }

    public static List<String> read(String path, String header) throws IOException {
        Path file = Path.of(path);
        if (!Files.exists(file)) return new ArrayList<>();
        List<String> records = new ArrayList<>(CsvMapper.records(Files.readString(file)));
        if (!records.isEmpty()) {
            String actual = records.remove(0);
            if (!actual.equals(header) && !(header.equals(CsvMapper.ACCOUNT_HEADER)
                    && actual.equals(header.substring(0, header.lastIndexOf(','))))) {
                throw new IOException("Unexpected CSV header in " + path);
            }
        }
        return records;
    }

    public static void write(String path, String header, List<String> rows, int backups) throws IOException {
        if (backups > 0) BackupManager.backup(path, backups);
        List<String> lines = new ArrayList<>();
        lines.add(header); lines.addAll(rows);
        AtomicFileWriter.write(path, lines);
    }

    public static String journalPath(String accounts) {
        return accounts + ".transfer-journal";
    }

    public static void recover(String accounts) throws IOException {
        Path journal = Path.of(journalPath(accounts));
        if (!Files.exists(journal)) return;
        List<String> entries = Files.readAllLines(journal);
        if (entries.size() != 4) throw new IOException("Invalid transfer recovery journal");
        String accountPath = decode(entries.get(0));
        if (!Path.of(accountPath).toAbsolutePath().normalize().equals(Path.of(accounts).toAbsolutePath().normalize())) {
            throw new IOException("Transfer journal account path mismatch");
        }
        AtomicFileWriter.write(accountPath, CsvMapper.records(decode(entries.get(2))));
        AtomicFileWriter.write(decode(entries.get(1)), CsvMapper.records(decode(entries.get(3))));
        Files.delete(journal);
    }

    public static void commitPair(String accounts, List<String> accountRows, String transactions,
                                   List<String> transactionRows, int backups) throws IOException {
        if (Path.of(accounts).toAbsolutePath().normalize().equals(Path.of(transactions).toAbsolutePath().normalize())) {
            throw new IOException("Accounts and transactions require different files");
        }
        if (backups > 0) {
            BackupManager.backup(accounts, backups);
            BackupManager.backup(transactions, backups);
        }
        List<String> entries = List.of(encode(Path.of(accounts).toAbsolutePath().normalize().toString()),
                encode(Path.of(transactions).toAbsolutePath().normalize().toString()),
                encode(CsvMapper.ACCOUNT_HEADER + "\n" + String.join("\n", accountRows)),
                encode(CsvMapper.TRANSACTION_HEADER + "\n" + String.join("\n", transactionRows)));
        AtomicFileWriter.write(journalPath(accounts), entries);
        // The durable journal is the commit point. Recovery completes both replacements after interruption.
        recover(accounts);
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
