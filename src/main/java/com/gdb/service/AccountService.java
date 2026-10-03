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
import com.gdb.repository.AccountRepository;
import com.gdb.repository.RepositoryFactory;
import com.gdb.repository.TransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Coordinates account lifecycle, money movement, and transaction logging. */
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionLogger logger;
    private final TransferService transferService;

    public AccountService(TransactionLogger logger) {
        this(RepositoryFactory.getAccountRepository(),
                RepositoryFactory.getTransactionRepository(), logger);
    }

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          TransactionLogger logger) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "Account repository is required");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "Transaction repository is required");
        this.logger = Objects.requireNonNull(logger, "Transaction logger is required");
        this.transferService = new TransferService();
    }

    public IAccount openAccount(String type, String name, int age, double initialBalance)
            throws AccountException {
        int accountNumber = accountRepository.nextAccountNumber();
        IAccount account = AccountFactory.createAccount(
                type, accountNumber, name, age, initialBalance);
        accountRepository.save(account);
        return account;
    }

    public void closeAccount(int accountNumber, int pin) throws AccountException {
        IAccount account = requireAccount(accountNumber);
        if (!account.verifyPin(pin)) {
            throw new InvalidPinException("Incorrect PIN");
        }
        account.closeAccount();
        accountRepository.update(account);
    }

    public Transaction deposit(int accountNumber, double amount) throws Exception {
        DepositCommand command = new DepositCommand(requireAccount(accountNumber), amount);
        command.execute();
        accountRepository.update(requireAccount(accountNumber));
        transactionRepository.save(command.getTransaction());
        logger.log(command);
        return command.getTransaction();
    }

    public Transaction withdraw(int accountNumber, double amount, int pin) throws Exception {
        WithdrawCommand command = new WithdrawCommand(
                requireAccount(accountNumber), amount, pin);
        command.execute();
        accountRepository.update(requireAccount(accountNumber));
        transactionRepository.save(command.getTransaction());
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
        accountRepository.update(from);
        accountRepository.update(to);
        transactionRepository.save(transaction);
        logger.log(command);
        return transaction;
    }

    public IAccount getAccount(int accountNumber) {
        return accountRepository.findById(accountNumber);
    }

    public List<IAccount> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<TransactionCommand> getTransactionHistory() {
        return logger.readAll();
    }

    public int getNextAccountNumber() {
        return accountRepository.getNextAccountNumber();
    }

    public List<Transaction> getTransactionRecords() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionRecords(int accountNumber) {
        return transactionRepository.findByAccount(accountNumber);
    }

    private IAccount requireAccount(int accountNumber) throws AccountException {
        IAccount account = accountRepository.findById(accountNumber);
        if (account == null) {
            throw new AccountException("Account not found: " + accountNumber);
        }
        return account;
    }
}
