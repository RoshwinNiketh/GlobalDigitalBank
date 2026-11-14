package com.gdb.file;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/** Replaces one file after writing and flushing a sibling temporary file. */
public final class AtomicFileWriter {
    private AtomicFileWriter() { }

    public static void write(String path, List<String> lines) throws IOException {
        java.util.Objects.requireNonNull(lines, "File lines are required");
        for (String line : lines) java.util.Objects.requireNonNull(line, "File lines cannot be null");
        Path target = Path.of(path).toAbsolutePath().normalize();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), target.getFileName() + ".", ".tmp");
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
