package com.gdb.repository;

import com.gdb.db.ConnectionProvider;
import com.gdb.domain.Account;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.CurrentAccount;
import com.gdb.domain.FixedDepositAccount;
import com.gdb.domain.IAccount;
import com.gdb.exceptions.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** SQLite/JDBC-backed account repository using parameterized SQL. */
public class JdbcAccountRepository implements AccountRepository {
    private static final String COLUMNS = "account_number, account_holder_name, age, balance, account_type, status, pin, opening_date, overdraft_used, interest_earned, tenure_years";
    private final ConnectionProvider provider;

    public JdbcAccountRepository(ConnectionProvider provider) {
        this.provider = Objects.requireNonNull(provider, "Connection provider is required");
    }

    @Override
    public void save(IAccount account) {
        Objects.requireNonNull(account, "Account is required");
        String sql = "INSERT INTO accounts (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAccount(statement, account);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw accessFailure("save account " + account.getAccountNumber(), exception);
        }
    }

    @Override
    public IAccount findById(int accountNumber) {
        String sql = "SELECT " + COLUMNS + " FROM accounts WHERE account_number = ?";
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountNumber);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        } catch (SQLException exception) {
            throw accessFailure("find account " + accountNumber, exception);
        }
    }

    @Override
    public List<IAccount> findAll() {
        List<IAccount> accounts = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM accounts ORDER BY account_number";
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                accounts.add(mapRow(result));
            }
            return accounts;
        } catch (SQLException exception) {
            throw accessFailure("find all accounts", exception);
        }
    }

    @Override
    public void update(IAccount account) {
        Objects.requireNonNull(account, "Account is required");
        String sql = "UPDATE accounts SET balance = ?, status = ?, overdraft_used = ?, interest_earned = ?, pin = ?, tenure_years = ? WHERE account_number = ?";
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, account.getBalance());
            statement.setString(2, account.isActive() ? "Active" : "Inactive");
            statement.setDouble(3, overdraftUsed(account));
            statement.setDouble(4, interestEarned(account));
            setOptionalInt(statement, 5, ((Account) account).getPin());
            statement.setInt(6, account.getTenureYears());
            statement.setInt(7, account.getAccountNumber());
            statement.executeUpdate();
        } catch (SQLException | ClassCastException exception) {
            throw accessFailure("update account " + account.getAccountNumber(), exception);
        }
    }

    @Override
    public void delete(int accountNumber) {
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM accounts WHERE account_number = ?")) {
            statement.setInt(1, accountNumber);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw accessFailure("delete account " + accountNumber, exception);
        }
    }

    @Override
    public boolean exists(int accountNumber) {
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM accounts WHERE account_number = ?")) {
            statement.setInt(1, accountNumber);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException exception) {
            throw accessFailure("check account " + accountNumber, exception);
        }
    }

    @Override
    public int nextAccountNumber() {
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COALESCE(MAX(account_number), 1000) + 1 FROM accounts");
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 1001;
        } catch (SQLException exception) {
            throw accessFailure("generate next account number", exception);
        }
    }

    @Override
    public int getNextAccountNumber() {
        return nextAccountNumber();
    }

    private void bindAccount(PreparedStatement statement, IAccount account) throws SQLException {
        Account details = (Account) account;
        statement.setInt(1, account.getAccountNumber());
        statement.setString(2, account.getAccountHolderName());
        statement.setInt(3, details.getAge());
        statement.setDouble(4, account.getBalance());
        statement.setString(5, accountType(account));
        statement.setString(6, account.isActive() ? "Active" : "Inactive");
        setOptionalInt(statement, 7, details.getPin());
        statement.setString(8, account.getOpeningDate());
        statement.setDouble(9, overdraftUsed(account));
        statement.setDouble(10, interestEarned(account));
        statement.setInt(11, account.getTenureYears());
    }

    private IAccount mapRow(ResultSet result) throws SQLException {
        String type = result.getString("account_type");
        int accountNumber = result.getInt("account_number");
        String name = result.getString("account_holder_name");
        int age = result.getInt("age");
        double balance = result.getDouble("balance");
        int tenureYears = result.getInt("tenure_years");
        try {
            IAccount account = AccountFactory.createAccount(type, accountNumber, name,
                    age, Math.max(balance, com.gdb.domain.AccountRulesEngine.getInstance()
                            .getMinimumBalance(type, tenureYears)), tenureYears);
            Account details = (Account) account;
            details.restoreBalance(balance);
            details.restoreOpeningDate(result.getString("opening_date"));
            int pin = result.getInt("pin");
            if (!result.wasNull()) {
                details.setPin(pin);
            }
            if ("Current".equalsIgnoreCase(type)) {
                ((CurrentAccount) account).setOverdraftUsed(result.getDouble("overdraft_used"));
            }
            if ("FixedDeposit".equalsIgnoreCase(type)) {
                ((FixedDepositAccount) account).setInterestEarned(result.getDouble("interest_earned"));
            }
            if (!"Active".equalsIgnoreCase(result.getString("status"))) {
                account.closeAccount();
            }
            return account;
        } catch (Exception exception) {
            throw new SQLException("Unable to map account row " + accountNumber, exception);
        }
    }

    private String accountType(IAccount account) {
        return account.getAccountType().replaceAll("\\s+", "").toUpperCase(java.util.Locale.ROOT);
    }

    private double overdraftUsed(IAccount account) {
        return account instanceof CurrentAccount ? ((CurrentAccount) account).getOverdraftUsed() : 0.0;
    }

    private double interestEarned(IAccount account) {
        return account instanceof FixedDepositAccount ? ((FixedDepositAccount) account).getInterestEarned() : 0.0;
    }

    private void setOptionalInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private DataAccessException accessFailure(String operation, Exception cause) {
        return new DataAccessException("Unable to " + operation, cause);
    }
}
