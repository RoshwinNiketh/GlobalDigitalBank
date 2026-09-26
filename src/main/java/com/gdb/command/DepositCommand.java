package com.gdb.command;

import com.gdb.domain.Account;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;

/** Command that deposits funds into an account. */
public class DepositCommand implements TransactionCommand {
    private static final long serialVersionUID = 1L;

    private final IAccount account;
    private final double amount;
    private Transaction transaction;

    public DepositCommand(IAccount account, double amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void execute() throws Exception {
        transaction = ((Account) account).depositWithTransaction(amount);
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
