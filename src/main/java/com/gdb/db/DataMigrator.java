package com.gdb.db;

import com.gdb.command.TransactionCommand;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.logging.LogDestination;
import com.gdb.repository.AccountRepository;
import com.gdb.repository.TransactionRepository;
import java.util.Objects;

/** Copies legacy data, propagating failures rather than reporting partial copies as success. */
public final class DataMigrator {
    private DataMigrator() { }

    public record MigrationSummary(int accounts, int transactions) { }

    public static MigrationSummary migrate(AccountRepository sourceAccounts, AccountRepository targetAccounts,
                                            TransactionRepository sourceTransactions, TransactionRepository targetTransactions) {
        Objects.requireNonNull(sourceAccounts);
        Objects.requireNonNull(targetAccounts);
        Objects.requireNonNull(sourceTransactions);
        Objects.requireNonNull(targetTransactions);
        int accounts = 0;
        int transactions = 0;
        for (IAccount account : sourceAccounts.findAll()) {
            targetAccounts.save(account);
            accounts++;
        }
        for (Transaction transaction : sourceTransactions.findAll()) {
            targetTransactions.save(transaction);
            transactions++;
        }
        return new MigrationSummary(accounts, transactions);
    }

    public static int migrate(LogDestination source, TransactionRepository target) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        int migrated = 0;
        for (TransactionCommand command : source.readAll()) {
            if (command != null && command.getTransaction() != null) {
                target.save(command.getTransaction());
                migrated++;
            }
        }
        return migrated;
    }
}
