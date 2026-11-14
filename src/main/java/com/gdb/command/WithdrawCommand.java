package com.gdb.command;

import com.gdb.domain.Account;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;

/** Command that withdraws funds from an account. */
public class WithdrawCommand implements TransactionCommand {
    private static final long serialVersionUID = 1L;

    private final IAccount account;
    private final double amount;
    private final int pin;
    private Transaction transaction;

    public WithdrawCommand(IAccount account, double amount, int pin) {
        this.account = account;
        this.amount = amount;
        this.pin = pin;
    }

    @Override
    public void execute() throws Exception {
        transaction = ((Account) account).withdrawWithTransaction(amount, pin);
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
