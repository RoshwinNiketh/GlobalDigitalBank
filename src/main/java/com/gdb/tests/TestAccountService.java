package com.gdb.tests;

import com.gdb.command.TransactionCommand;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InvalidPinException;
import com.gdb.logging.MemoryLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;
import java.util.List;

/** Activity 19 integration driver using an in-memory transaction destination. */
public class TestAccountService {
    public static void main(String[] args) throws Exception {
        printHeader();
        AccountService service = createService();
        IAccount[] accounts = openAccounts(service);
        IAccount john = accounts[0];
        IAccount jane = accounts[1];

        runMoneyMovementDemo(service, john, jane);
        printFinalBalances(john, jane);
        printTransactionHistory(service);
        verifyErrorHandling(service, john);
    }

    private static void printHeader() {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 19 — ACCOUNT SERVICE DEMO");
        System.out.println("=".repeat(60));
    }

    private static AccountService createService() {
        return new AccountService(new TransactionLogger(new MemoryLogDestination()));
    }

    private static IAccount[] openAccounts(AccountService service) throws Exception {
        IAccount john = service.openAccount("SAVINGS", "John Doe", 25, 15000.0);
        john.setPin(1234);
        IAccount jane = service.openAccount("SAVINGS", "Jane Smith", 30, 10000.0);
        jane.setPin(5678);

        System.out.println("\n[STEP 13] Opened: " + john.getAccountInfo());
        System.out.println("[STEP 13] Opened: " + jane.getAccountInfo());
        return new IAccount[] { john, jane };
    }

    private static void runMoneyMovementDemo(AccountService service,
                                             IAccount john,
                                             IAccount jane) throws Exception {
        Transaction deposit = service.deposit(john.getAccountNumber(), 5000.0);
        System.out.println("\n[STEP 14] Deposit: " + deposit);

        Transaction withdrawal = service.withdraw(
                john.getAccountNumber(), 2000.0, 1234);
        System.out.println("[STEP 15] Withdrawal: " + withdrawal);

        Transaction transfer = service.transfer(
                john.getAccountNumber(), jane.getAccountNumber(), 1000.0, 1234);
        System.out.println("[STEP 16] Transfer: " + transfer);
    }

    private static void printFinalBalances(IAccount john, IAccount jane) {
        System.out.println("\n[STEP 17] Final Balances:");
        System.out.println("  John (Account #" + john.getAccountNumber()
                + "): Rs. " + john.getBalance());
        System.out.println("  Jane (Account #" + jane.getAccountNumber()
                + "): Rs. " + jane.getBalance());
    }

    private static void printTransactionHistory(AccountService service) {
        List<TransactionCommand> history = service.getTransactionHistory();
        System.out.println("\n[STEP 18] Transaction History ("
                + history.size() + " records):");
        for (int index = 0; index < history.size(); index++) {
            System.out.println("  [" + (index + 1) + "] "
                    + history.get(index).getTransaction());
        }
    }

    private static void verifyErrorHandling(AccountService service,
                                            IAccount john) throws Exception {
        System.out.println("\n[STEP 19] Error Handling Checks:");
        try {
            service.deposit(9999, 100.0);
            throw new AssertionError("Deposit to missing account should fail");
        } catch (AccountException e) {
            System.out.println("  Deposit to missing account caught: "
                    + e.getMessage() + " [PASS]");
        }

        try {
            service.withdraw(john.getAccountNumber(), 100.0, 9999);
            throw new AssertionError("Withdrawal with wrong PIN should fail");
        } catch (InvalidPinException e) {
            System.out.println("  Withdrawal with wrong PIN caught: "
                    + e.getMessage() + " [PASS]");
        }
    }
}
