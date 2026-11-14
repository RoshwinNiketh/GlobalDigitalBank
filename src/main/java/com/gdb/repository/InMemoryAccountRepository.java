package com.gdb.repository;

import com.gdb.domain.IAccount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory account storage backed by a map keyed by account number. */
public class InMemoryAccountRepository implements AccountRepository {
    private final Map<Integer, IAccount> accounts = new HashMap<>();
    private int nextAccountNumber = 1001;

    @Override
    public synchronized void save(IAccount account) {
        if (account != null) {
            accounts.put(account.getAccountNumber(), account);
        }
    }

    @Override
    public synchronized IAccount findById(int accountNumber) {
        return accounts.get(accountNumber);
    }

    @Override
    public synchronized List<IAccount> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public synchronized void update(IAccount account) {
        if (account != null && accounts.containsKey(account.getAccountNumber())) {
            accounts.put(account.getAccountNumber(), account);
        }
    }

    @Override
    public synchronized void delete(int accountNumber) {
        accounts.remove(accountNumber);
    }

    @Override
    public synchronized boolean exists(int accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    @Override
    public synchronized int nextAccountNumber() {
        return nextAccountNumber++;
    }

    @Override
    public synchronized int getNextAccountNumber() {
        return nextAccountNumber;
    }
}
