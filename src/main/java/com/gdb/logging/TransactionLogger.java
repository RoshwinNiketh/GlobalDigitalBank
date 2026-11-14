package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.util.List;
import java.util.Objects;

/** Bridge abstraction that delegates logging to a selectable destination. */
public class TransactionLogger {
    protected LogDestination destination;

    public TransactionLogger(LogDestination destination) {
        this.destination = Objects.requireNonNull(destination, "Destination is required");
    }

    public void setDestination(LogDestination destination) {
        this.destination = Objects.requireNonNull(destination, "Destination is required");
    }

    public void log(TransactionCommand command) {
        destination.write(command);
    }

    public List<TransactionCommand> readAll() {
        return destination.readAll();
    }

    public void clear() {
        destination.clear();
    }

    public String getDestinationName() {
        return destination.getDestinationName();
    }
}
