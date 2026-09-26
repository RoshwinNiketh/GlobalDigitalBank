package com.gdb.service;

import com.gdb.command.DepositCommand;
import com.gdb.command.TransactionCommand;
import com.gdb.command.TransferCommand;
import com.gdb.command.WithdrawCommand;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InvalidPinException;
import com.gdb.logging.TransactionLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Coordinates account lifecycle, money movement, and transaction logging. */
public class AccountService {
    private final Map<Integer, IAccount> accounts;
    private final TransactionLogger logger;
    private final TransferService transferService;
    private int nextAccountNumber = 1001;

    public AccountService(TransactionLogger logger) {
        this.logger = Objects.requireNonNull(logger, "Transaction logger is required");
        this.accounts = new HashMap<>();
        this.transferService = new TransferService();
    }

    public IAccount openAccount(String type, String name, int age, double initialBalance)
            throws AccountException {
        int accountNumber = nextAccountNumber++;
        IAccount account = AccountFactory.createAccount(
                type, accountNumber, name, age, initialBalance);
        accounts.put(accountNumber, account);
        return account;
    }

    public void closeAccount(int accountNumber, int pin) throws AccountException {
        IAccount account = requireAccount(accountNumber);
        if (!account.verifyPin(pin)) {
            throw new InvalidPinException("Incorrect PIN");
        }
        account.closeAccount();
    }

    public Transaction deposit(int accountNumber, double amount) throws Exception {
        DepositCommand command = new DepositCommand(requireAccount(accountNumber), amount);
        command.execute();
        logger.log(command);
        return command.getTransaction();
    }

    public Transaction withdraw(int accountNumber, double amount, int pin) throws Exception {
        WithdrawCommand command = new WithdrawCommand(
                requireAccount(accountNumber), amount, pin);
        command.execute();
        logger.log(command);
        return command.getTransaction();
    }

    public Transaction transfer(int fromAccountNumber, int toAccountNumber,
                                double amount, int pin) throws Exception {
        IAccount from = requireAccount(fromAccountNumber);
        IAccount to = requireAccount(toAccountNumber);
        Transaction transaction = transferService.transferWithTransaction(
                from, to, amount, pin);
        TransferCommand command = new TransferCommand(
                from, to, amount, pin, transaction);
        logger.log(command);
        return transaction;
    }

    public IAccount getAccount(int accountNumber) {
        return accounts.get(accountNumber);
    }

    public List<IAccount> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    public List<TransactionCommand> getTransactionHistory() {
        return logger.readAll();
    }

    public int getNextAccountNumber() {
        return nextAccountNumber;
    }

    private IAccount requireAccount(int accountNumber) throws AccountException {
        IAccount account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountException("Account not found: " + accountNumber);
        }
        return account;
    }
}
