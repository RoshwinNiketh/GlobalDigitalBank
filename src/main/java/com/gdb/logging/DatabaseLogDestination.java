package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import com.gdb.db.SimulatedDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Simulated database-backed transaction log destination. */
public class DatabaseLogDestination implements LogDestination {
    private static final String TABLE = "transaction_log";

    private final SimulatedDatabase db;

    public DatabaseLogDestination(SimulatedDatabase db) {
        this.db = Objects.requireNonNull(db, "Database is required");
    }

    @Override
    public void write(TransactionCommand command) {
        db.insert(TABLE, Objects.requireNonNull(command, "Command is required"));
    }

    @Override
    public List<TransactionCommand> readAll() {
        List<TransactionCommand> commands = new ArrayList<>();
        for (Object value : db.selectAll(TABLE)) {
            if (!(value instanceof TransactionCommand)) {
                throw new IllegalStateException("Unexpected value in transaction log: "
                        + value.getClass().getName());
            }
            commands.add((TransactionCommand) value);
        }
        return commands;
    }

    @Override
    public void clear() {
        db.deleteAll(TABLE);
    }

    @Override
    public String getDestinationName() {
        return "DATABASE";
    }
}
