package com.gdb.repository;

import com.gdb.domain.IAccount;
import java.util.List;

/** Persistence contract for bank accounts. */
public interface AccountRepository {
    void save(IAccount account);
    IAccount findById(int accountNumber);
    List<IAccount> findAll();
    void update(IAccount account);
    void delete(int accountNumber);
    boolean exists(int accountNumber);
    int nextAccountNumber();
    int getNextAccountNumber();
}
