package com.gdb.file;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Retains the newest timestamped copies of a file. */
public final class BackupManager {
    private BackupManager() { }

    public static synchronized void backup(String path, int maxBackups) throws IOException {
        if (maxBackups < 1) throw new IllegalArgumentException("Backup retention must be positive");
        Path source = Path.of(path).toAbsolutePath().normalize();
        if (!Files.exists(source)) return;
        String prefix = source.getFileName() + ".bak.";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSSSSSSSS"));
        Files.copy(source, source.resolveSibling(prefix + timestamp + "-" + UUID.randomUUID()));
        List<Path> backups;
        try (var files = Files.list(source.getParent())) {
            backups = files.filter(file -> file.getFileName().toString().startsWith(prefix))
                    .sorted(Comparator.comparing(file -> file.getFileName().toString())).toList();
        }
        for (int index = 0; index < backups.size() - maxBackups; index++) Files.delete(backups.get(index));
    }
}
