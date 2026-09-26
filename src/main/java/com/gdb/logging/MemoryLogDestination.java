package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** In-memory transaction log destination. */
public class MemoryLogDestination implements LogDestination {
    private final List<TransactionCommand> commands = new ArrayList<>();

    @Override
    public void write(TransactionCommand command) {
        commands.add(Objects.requireNonNull(command, "Command is required"));
    }

    @Override
    public List<TransactionCommand> readAll() {
        return new ArrayList<>(commands);
    }

    @Override
    public void clear() {
        commands.clear();
    }

    @Override
    public String getDestinationName() {
        return "MEMORY";
    }
}
