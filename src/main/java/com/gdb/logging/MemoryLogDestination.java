package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** In-memory transaction log destination. */
public class MemoryLogDestination implements LogDestination {
    private final List<TransactionCommand> commands = new ArrayList<>();

    @Override
    public synchronized void write(TransactionCommand command) {
        commands.add(Objects.requireNonNull(command, "Command is required"));
    }

    @Override
    public synchronized List<TransactionCommand> readAll() {
        return new ArrayList<>(commands);
    }

    @Override
    public synchronized void clear() {
        commands.clear();
    }

    @Override
    public String getDestinationName() {
        return "MEMORY";
    }
}
