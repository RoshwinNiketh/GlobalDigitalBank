package com.gdb.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/** Creates shared repository instances according to config/persistence.properties. */
public final class RepositoryFactory {
    private static AccountRepository accountRepositoryInstance;
    private static TransactionRepository transactionRepositoryInstance;

    private RepositoryFactory() {
    }

    public static String getPersistenceMode() {
        Properties properties = new Properties();
        try (InputStream input = RepositoryFactory.class.getClassLoader()
                .getResourceAsStream("config/persistence.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load persistence configuration", exception);
        }
        return properties.getProperty("persistence.mode", "memory")
                .trim().toLowerCase(Locale.ROOT);
    }

    public static synchronized AccountRepository getAccountRepository() {
        if (accountRepositoryInstance == null) {
            ensureMemoryMode();
            accountRepositoryInstance = new InMemoryAccountRepository();
        }
        return accountRepositoryInstance;
    }

    public static synchronized TransactionRepository getTransactionRepository() {
        if (transactionRepositoryInstance == null) {
            ensureMemoryMode();
            transactionRepositoryInstance = new InMemoryTransactionRepository();
        }
        return transactionRepositoryInstance;
    }

    private static void ensureMemoryMode() {
        String mode = getPersistenceMode();
        if ("jdbc".equals(mode)) {
            throw new UnsupportedOperationException(
                    "JDBC repositories are not implemented yet - coming in Activity 22");
        }
        if ("file".equals(mode)) {
            throw new UnsupportedOperationException("File repositories are not implemented yet");
        }
    }
}
