package com.gdb.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Executes the idempotent schema.sql script against a database. */
public final class SchemaInitializer {
    private SchemaInitializer() {
    }

    public static void initialize(ConnectionProvider provider) {
        Objects.requireNonNull(provider, "Connection provider is required");
        String schemaSql = readSchemaSql();

        try (Connection connection = provider.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : schemaSql.split(";")) {
                String command = sql.trim();
                if (!command.isEmpty()) {
                    statement.execute(command);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to initialize database schema", exception);
        }
    }

    public static String readSchemaSql() {
        ClassLoader classLoader = SchemaInitializer.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("schema.sql")) {
            if (input != null) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            Path sourcePath = Path.of("src", "main", "resources", "schema.sql");
            if (Files.isRegularFile(sourcePath)) {
                return Files.readString(sourcePath, StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read schema.sql", exception);
        }
        throw new IllegalStateException("schema.sql was not found on the classpath or in src/main/resources");
    }
}
