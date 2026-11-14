package com.gdb.tests;

import com.gdb.command.DepositCommand;
import com.gdb.command.TransactionCommand;
import com.gdb.command.TransferCommand;
import com.gdb.command.WithdrawCommand;
import com.gdb.domain.Account;
import com.gdb.domain.AccountFactory;
import com.gdb.logging.TransactionLog;
import java.util.List;

/** Activity 17 driver for executable commands and persistent file logging. */
public class TestCommandLogging {
    public static void main(String[] args) throws Exception {
        printHeader();

        TransactionLog log = new TransactionLog();
        log.clear();

        Account[] accounts = createTestAccounts();
        Account acc1 = accounts[0];
        Account acc2 = accounts[1];

        logDeposit(acc1, log);
        logWithdrawal(acc1, log);
        logTransfer(acc1, acc2, log);

        List<TransactionCommand> history = displayHistory(log);
        verifyPersistence(history.size());
    }

    private static void printHeader() {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 17 — COMMAND PATTERN + FILE LOGGING");
        System.out.println("=".repeat(60));
    }

    private static Account[] createTestAccounts() throws Exception {
        Account acc1 = (Account) AccountFactory.createAccount(
                "SAVINGS", 1001, "John Doe", 25, 15000.0);
        Account acc2 = (Account) AccountFactory.createAccount(
                "SAVINGS", 1002, "Jane Smith", 30, 10000.0);
        acc1.setPin(1234);
        return new Account[] { acc1, acc2 };
    }

    private static void logDeposit(Account account, TransactionLog log)
            throws Exception {
        DepositCommand deposit = new DepositCommand(account, 5000.0);
        deposit.execute();
        log.log(deposit);
        System.out.println("[STEP 11] Logged: " + deposit.getTransaction());
    }

    private static void logWithdrawal(Account account, TransactionLog log)
            throws Exception {
        WithdrawCommand withdrawal = new WithdrawCommand(account, 2000.0, 1234);
        withdrawal.execute();
        log.log(withdrawal);
        System.out.println("[STEP 12] Logged: " + withdrawal.getTransaction());
    }

    private static void logTransfer(Account from, Account to, TransactionLog log)
            throws Exception {
        TransferCommand transfer = new TransferCommand(from, to, 3000.0, 1234);
        transfer.execute();
        log.log(transfer);
        System.out.println("[STEP 13] Logged: " + transfer.getTransaction());
    }

    private static List<TransactionCommand> displayHistory(TransactionLog log)
            throws Exception {
        List<TransactionCommand> history = log.readAll();
        System.out.println("\n[STEP 14] Read " + history.size()
                + " commands from transaction log:");
        for (int index = 0; index < history.size(); index++) {
            System.out.println("  [" + (index + 1) + "] "
                    + history.get(index).getTransaction());
        }
        return history;
    }

    private static void verifyPersistence(int expectedCount) throws Exception {
        TransactionLog freshLog = new TransactionLog();
        List<TransactionCommand> persisted = freshLog.readAll();
        if (persisted.size() != expectedCount || persisted.size() != 3) {
            throw new AssertionError("Expected 3 persisted commands, found "
                    + persisted.size());
        }
        System.out.println("\n[STEP 15] Fresh reader verified "
                + persisted.size() + " persisted commands [PASS]");
    }
}
