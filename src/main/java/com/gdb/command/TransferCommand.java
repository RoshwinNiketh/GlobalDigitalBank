package com.gdb.command;

import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.service.TransferService;

/** Command that transfers funds between two accounts. */
public class TransferCommand implements TransactionCommand {
    private static final long serialVersionUID = 1L;

    private final IAccount fromAccount;
    private final IAccount toAccount;
    private final double amount;
    private final int pin;
    private Transaction transaction;

    public TransferCommand(IAccount from, IAccount to, double amount, int pin) {
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
        this.pin = pin;
    }

    /** Creates a command record for a transfer already executed by a service. */
    public TransferCommand(IAccount from, IAccount to, double amount, int pin,
                           Transaction transaction) {
        this(from, to, amount, pin);
        this.transaction = transaction;
    }

    @Override
    public void execute() throws Exception {
        transaction = new TransferService().transferWithTransaction(
                fromAccount, toAccount, amount, pin);
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
