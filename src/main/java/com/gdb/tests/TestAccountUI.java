package com.gdb.tests;

import com.gdb.domain.IAccount;
import com.gdb.logging.MemoryLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;

/** Integration driver confirming the UI's AccountService endpoints. */
public class TestAccountUI {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 20 — ACCOUNT UI & INTEGRATION TEST");
        System.out.println("=".repeat(60));

        AccountService service = new AccountService(
                new TransactionLogger(new MemoryLogDestination()));

        IAccount account = service.openAccount(
                "SAVINGS", "Alice Cooper", 28, 20000.0);
        account.setPin(1234);
        System.out.println("[UI TEST] Opened: " + account.getAccountInfo());

        service.deposit(account.getAccountNumber(), 5000.0);
        System.out.println("[UI TEST] Deposit Rs. 5000 | Balance: Rs. "
                + account.getBalance());

        service.withdraw(account.getAccountNumber(), 3000.0, 1234);
        System.out.println("[UI TEST] Withdraw Rs. 3000 | Balance: Rs. "
                + account.getBalance());

        int historyCount = service.getTransactionHistory().size();
        if (historyCount != 2 || account.getBalance() != 22000.0) {
            throw new AssertionError("Account service integration check failed");
        }
        System.out.println("[UI TEST] Total Logged Transactions: " + historyCount);
        System.out.println("All UI service endpoints validated successfully!");
    }
}
