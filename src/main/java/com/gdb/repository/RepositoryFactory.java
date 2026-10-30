package com.gdb.repository;

import com.gdb.db.ConnectionProvider;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.HikariConnectionProvider;
import com.gdb.db.SchemaInitializer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/** Creates shared repositories and the configured JDBC connection provider. */
public final class RepositoryFactory {
    private static AccountRepository accountRepositoryInstance;
    private static TransactionRepository transactionRepositoryInstance;
    private static ConnectionProvider connectionProviderInstance;

    private RepositoryFactory() {
    }

    public static String getPersistenceMode() {
        return loadPersistenceProperties().getProperty("persistence.mode", "memory")
                .trim().toLowerCase(Locale.ROOT);
    }

    public static Properties loadPersistenceProperties() {
        Properties properties = new Properties();
        try (InputStream input = RepositoryFactory.class.getClassLoader()
                .getResourceAsStream("config/persistence.properties")) {
            if (input != null) {
                properties.load(input);
                return properties;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load persistence configuration", exception);
        }

        Path sourcePath = Path.of("src", "main", "resources", "config", "persistence.properties");
        if (Files.isRegularFile(sourcePath)) {
            try (InputStream input = new FileInputStream(sourcePath.toFile())) {
                properties.load(input);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to load persistence configuration", exception);
            }
        }
        return properties;
    }

    public static synchronized ConnectionProvider getConnectionProvider() {
        if (connectionProviderInstance == null) {
            connectionProviderInstance = createConnectionProvider(loadPersistenceProperties());
        }
        return connectionProviderInstance;
    }

    /** Creates an initialized provider; the caller owns its shutdown. */
    public static ConnectionProvider createConnectionProvider(Properties properties) {
        boolean pooled = Boolean.parseBoolean(properties.getProperty("persistence.pool.enabled",
                properties.getProperty("db.pool.enabled", "false")).trim());
        ConnectionProvider provider = pooled ? new HikariConnectionProvider(properties)
                : new JdbcConnectionProvider(
                        properties.getProperty("persistence.db.url", "jdbc:sqlite:gdb.db").trim(),
                        properties.getProperty("persistence.db.driver", "org.sqlite.JDBC").trim());
        try {
            SchemaInitializer.initialize(provider);
            return provider;
        } catch (RuntimeException exception) {
            provider.shutdown();
            throw exception;
        }
    }

    public static synchronized AccountRepository getAccountRepository() {
        if (accountRepositoryInstance == null) {
            String mode = getPersistenceMode();
            if ("jdbc".equals(mode)) {
                accountRepositoryInstance = new JdbcAccountRepository(getConnectionProvider());
            }
            if ("file".equals(mode)) {
                throw new UnsupportedOperationException("File repositories are not implemented yet");
            }
            if (accountRepositoryInstance == null) accountRepositoryInstance = new InMemoryAccountRepository();
        }
        return accountRepositoryInstance;
    }

    public static synchronized TransactionRepository getTransactionRepository() {
        if (transactionRepositoryInstance == null) {
            String mode = getPersistenceMode();
            if ("jdbc".equals(mode)) {
                transactionRepositoryInstance = new JdbcTransactionRepository(getConnectionProvider());
            }
            if ("file".equals(mode)) {
                throw new UnsupportedOperationException("File repositories are not implemented yet");
            }
            if (transactionRepositoryInstance == null) transactionRepositoryInstance = new InMemoryTransactionRepository();
        }
        return transactionRepositoryInstance;
    }

    public static synchronized void reset() {
        if (connectionProviderInstance != null) {
            connectionProviderInstance.shutdown();
            connectionProviderInstance = null;
        }
        accountRepositoryInstance = null;
        transactionRepositoryInstance = null;
    }
}
