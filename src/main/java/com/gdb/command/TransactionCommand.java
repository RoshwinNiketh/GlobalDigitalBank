package com.gdb.command;

import com.gdb.domain.Transaction;
import java.io.Serializable;

/** A serializable operation that can produce a transaction record. */
public interface TransactionCommand extends Serializable {
    void execute() throws Exception;

    Transaction getTransaction();
}
