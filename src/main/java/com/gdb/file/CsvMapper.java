package com.gdb.file;

import com.gdb.domain.*;
import java.time.LocalDateTime;
import java.util.*;

/** CSV escaping and domain mapping; quoted fields may contain commas, quotes, and newlines. */
public final class CsvMapper {
    public static final String ACCOUNT_HEADER = "account_number,account_holder_name,age,balance,account_type,status,pin,opening_date,overdraft_used,interest_earned,tenure_years";
    public static final String TRANSACTION_HEADER = "transaction_id,timestamp,account_number,type,amount,balance_after,status,from_account,to_account,description";
    private CsvMapper() { }

    public static String toCsv(IAccount account) {
        Account details = (Account) account;
        return row(account.getAccountNumber(), account.getAccountHolderName(), details.getAge(),
                account.getBalance(), account.getAccountType(), account.isActive() ? "Active" : "Inactive",
                details.getPin(), account.getOpeningDate(),
                account instanceof CurrentAccount current ? current.getOverdraftUsed() : 0,
                account instanceof FixedDepositAccount deposit ? deposit.getInterestEarned() : 0,
                account.getTenureYears());
    }

    public static IAccount fromCsv(String line) {
        List<String> fields = fields(line);
        if (fields.size() != 10 && fields.size() != 11) throw new IllegalArgumentException("Invalid account CSV field count");
        try {
            String type = fields.get(4);
            int tenure = fields.size() == 11 ? Integer.parseInt(fields.get(10)) : 0;
            double balance = Double.parseDouble(fields.get(3));
            double openingBalance = Math.max(balance, AccountRulesEngine.getInstance().getMinimumBalance(type, tenure));
            Account account = (Account) AccountFactory.createAccount(type, Integer.parseInt(fields.get(0)),
                    fields.get(1), Integer.parseInt(fields.get(2)), openingBalance, tenure);
            account.restoreBalance(balance);
            account.restoreOpeningDate(fields.get(7));
            if (!fields.get(6).isEmpty()) account.setPin(Integer.parseInt(fields.get(6)));
            if (account instanceof CurrentAccount current) current.setOverdraftUsed(Double.parseDouble(fields.get(8)));
            if (account instanceof FixedDepositAccount deposit) deposit.setInterestEarned(Double.parseDouble(fields.get(9)));
            if ("Inactive".equals(fields.get(5))) account.closeAccount();
            else if (!"Active".equals(fields.get(5))) throw new IllegalArgumentException("Invalid account status");
            return account;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid account CSV record", exception);
        }
    }

    public static String toCsv(Transaction transaction) {
        return row(transaction.getTransactionId(), transaction.getTimestamp(), transaction.getAccountNumber(),
                transaction.getType(), transaction.getAmount(), transaction.getBalanceAfter(), transaction.getStatus(),
                transaction.getFromAccount(), transaction.getToAccount(), transaction.getDescription());
    }

    public static Transaction fromCsvTransaction(String line) {
        List<String> fields = fields(line);
        if (fields.size() != 10) throw new IllegalArgumentException("Invalid transaction CSV field count");
        return new Transaction(fields.get(0), LocalDateTime.parse(fields.get(1)), Integer.parseInt(fields.get(2)),
                TransactionType.valueOf(fields.get(3)), Double.parseDouble(fields.get(4)), Double.parseDouble(fields.get(5)),
                fields.get(6), fields.get(9), Integer.parseInt(fields.get(7)), Integer.parseInt(fields.get(8)));
    }

    private static String row(Object... values) {
        return Arrays.stream(values).map(value -> value == null ? "" : value.toString())
                .map(value -> "\"" + value.replace("\"", "\"\"") + "\"")
                .collect(java.util.stream.Collectors.joining(","));
    }

    public static List<String> fields(String record) {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < record.length(); index++) {
            char character = record.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < record.length() && record.charAt(index + 1) == '"') {
                    field.append('"'); index++;
                } else quoted = !quoted;
            } else if (character == ',' && !quoted) {
                result.add(field.toString()); field.setLength(0);
            } else field.append(character);
        }
        if (quoted) throw new IllegalArgumentException("Unterminated quoted CSV field");
        result.add(field.toString());
        return result;
    }

    public static List<String> records(String text) {
        List<String> records = new ArrayList<>();
        StringBuilder record = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '"') quoted = !quoted;
            if ((character == '\n' || character == '\r') && !quoted) {
                if (!record.isEmpty()) { records.add(record.toString()); record.setLength(0); }
            } else record.append(character);
        }
        if (quoted) throw new IllegalArgumentException("Unterminated CSV record");
        if (!record.isEmpty()) records.add(record.toString());
        return records;
    }
}
