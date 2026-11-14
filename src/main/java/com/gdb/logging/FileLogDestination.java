package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.io.IOException;
import java.util.List;

/** File-backed destination that adapts the Activity 17 transaction log. */
public class FileLogDestination implements LogDestination {
    private final TransactionLog log;

    public FileLogDestination() {
        this.log = new TransactionLog();
    }

    @Override
    public void write(TransactionCommand command) {
        try {
            log.log(command);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write transaction log", e);
        }
    }

    @Override
    public List<TransactionCommand> readAll() {
        try {
            return log.readAll();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Could not read transaction log", e);
        }
    }

    @Override
    public void clear() {
        log.clear();
    }

    @Override
    public String getDestinationName() {
        return "FILE";
    }
}
