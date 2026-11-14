package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/** Persists transaction commands in an append-only Java serialization stream. */
public class TransactionLog {
    private static final String FILE_PATH = "data/transactions.ser";

    private static class AppendableObjectOutputStream extends ObjectOutputStream {
        AppendableObjectOutputStream(OutputStream output) throws IOException {
            super(output);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }
    }

    public synchronized void log(TransactionCommand command) throws IOException {
        if (command == null) {
            throw new IllegalArgumentException("Command is required");
        }

        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create log directory: " + parent);
        }

        boolean append = file.exists() && file.length() > 0;
        try (ObjectOutputStream output = append
                ? new AppendableObjectOutputStream(new FileOutputStream(file, true))
                : new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(command);
            output.flush();
        }
    }

    public synchronized List<TransactionCommand> readAll()
            throws IOException, ClassNotFoundException {
        File file = new File(FILE_PATH);
        List<TransactionCommand> commands = new ArrayList<>();
        if (!file.exists() || file.length() == 0) {
            return commands;
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                Object value = input.readObject();
                if (!(value instanceof TransactionCommand)) {
                    throw new IOException("Unexpected object in transaction log: "
                            + value.getClass().getName());
                }
                commands.add((TransactionCommand) value);
            }
        } catch (EOFException endOfLog) {
            return commands;
        }
    }

    public synchronized void clear() {
        File file = new File(FILE_PATH);
        if (file.exists() && !file.delete()) {
            throw new IllegalStateException("Could not delete transaction log: " + file);
        }
    }
}
