package com.gdb.repository;

import com.gdb.domain.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** In-memory transaction storage backed by a list. */
public class InMemoryTransactionRepository implements TransactionRepository {
    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public synchronized void save(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }

    @Override
    public synchronized List<Transaction> findByAccount(int accountNumber) {
        return transactions.stream()
                .filter(transaction -> transaction.getAccountNumber() == accountNumber
                        || transaction.getFromAccount() == accountNumber
                        || transaction.getToAccount() == accountNumber)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<Transaction> findAll() {
        return new ArrayList<>(transactions);
    }

    @Override
    public synchronized void clear() {
        transactions.clear();
    }
}
