package com.gdb.repository;

import com.gdb.db.ConnectionProvider;
import com.gdb.domain.Transaction;
import com.gdb.domain.TransactionType;
import com.gdb.exceptions.DataAccessException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Stores ledger records and commits both transfer balances and audit rows together. */
public class JdbcTransactionRepository implements TransactionRepository {
    private final ConnectionProvider provider;

    public JdbcTransactionRepository(ConnectionProvider provider) {
        this.provider = Objects.requireNonNull(provider, "Connection provider is required");
    }

    @Override
    public void save(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction is required");
        try (Connection connection = provider.getConnection()) {
            insert(connection, transaction);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to save transaction", exception);
        }
    }

    private void insert(Connection connection, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_id, timestamp, account_number, type, amount, balance_after, status, description, from_account, to_account) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getTransactionId());
            statement.setString(2, transaction.getTimestamp().toString());
            statement.setInt(3, transaction.getAccountNumber());
            statement.setString(4, transaction.getType().name());
            statement.setDouble(5, transaction.getAmount());
            statement.setDouble(6, transaction.getBalanceAfter());
            statement.setString(7, transaction.getStatus());
            statement.setString(8, transaction.getDescription());
            statement.setInt(9, transaction.getFromAccount());
            statement.setInt(10, transaction.getToAccount());
            statement.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findByAccount(int accountNumber) {
        return query("SELECT * FROM transactions WHERE account_number = ? OR from_account = ? OR to_account = ? ORDER BY timestamp, transaction_id", accountNumber);
    }

    @Override
    public List<Transaction> findAll() {
        return query("SELECT * FROM transactions ORDER BY timestamp, transaction_id", null);
    }

    private List<Transaction> query(String sql, Integer accountNumber) {
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (accountNumber != null) {
                for (int index = 1; index <= 3; index++) statement.setInt(index, accountNumber);
            }
            List<Transaction> transactions = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) transactions.add(mapRow(result));
            }
            return transactions;
        } catch (SQLException | IllegalArgumentException exception) {
            throw new DataAccessException("Unable to read transactions", exception);
        }
    }

    private Transaction mapRow(ResultSet result) throws SQLException {
        return new Transaction(result.getString("transaction_id"),
                LocalDateTime.parse(result.getString("timestamp")), result.getInt("account_number"),
                TransactionType.valueOf(result.getString("type")), result.getDouble("amount"),
                result.getDouble("balance_after"), result.getString("status"),
                result.getString("description"), result.getInt("from_account"), result.getInt("to_account"));
    }

    @Override
    public void clear() {
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM transactions")) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to clear transactions", exception);
        }
    }

    @Override
    public void transfer(int from, int to, double amount) {
        transferAndRecord(from, to, amount);
    }

    public Transaction transferAndRecord(int from, int to, double amount) {
        if (from == to || !Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Transfer requires distinct accounts and a finite positive amount");
        }
        try (Connection connection = provider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                changeBalance(connection, from, -amount);
                changeBalance(connection, to, amount);
                Transaction transaction = new Transaction(Transaction.generateId(), LocalDateTime.now(),
                        from, TransactionType.TRANSFER, amount, readBalance(connection, from),
                        "SUCCESS", "Transfer to account #" + to, from, to);
                insert(connection, transaction);
                connection.commit();
                return transaction;
            } catch (SQLException | RuntimeException exception) {
                try { connection.rollback(); }
                catch (SQLException rollbackFailure) { exception.addSuppressed(rollbackFailure); }
                throw new DataAccessException("Transfer failed; balances and audit record rolled back", exception);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to execute transfer", exception);
        }
    }

    private void changeBalance(Connection connection, int account, double delta) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ? AND status = 'Active' AND balance + ? >= 0";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, delta);
            statement.setInt(2, account);
            statement.setDouble(3, delta);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Account missing, inactive, or insufficient balance: " + account);
            }
        }
    }

    private double readBalance(Connection connection, int account) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?")) {
            statement.setInt(1, account);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Account not found: " + account);
                return result.getDouble(1);
            }
        }
    }
}
