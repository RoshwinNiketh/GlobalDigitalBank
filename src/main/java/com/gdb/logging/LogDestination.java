package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.util.List;

/** Storage contract implemented by transaction log destinations. */
public interface LogDestination {
    void write(TransactionCommand command);

    List<TransactionCommand> readAll();

    void clear();

    String getDestinationName();
}
