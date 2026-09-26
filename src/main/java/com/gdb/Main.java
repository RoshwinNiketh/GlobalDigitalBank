package com.gdb;

import com.gdb.command.TransactionCommand;
import com.gdb.domain.IAccount;
import com.gdb.logging.FileLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;
import java.util.List;

/** Application entry point demonstrating the service layer with file logging. */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Global Digital Bank — Service Demo");
        System.out.println("=".repeat(60));

        AccountService service = new AccountService(
                new TransactionLogger(new FileLogDestination()));
        IAccount john = service.openAccount("SAVINGS", "John Doe", 25, 15000.0);
        john.setPin(1234);
        IAccount jane = service.openAccount("SAVINGS", "Jane Smith", 30, 10000.0);
        jane.setPin(5678);

        System.out.println("Opened: " + john.getAccountInfo());
        System.out.println("Opened: " + jane.getAccountInfo());
        System.out.println("Deposit: "
                + service.deposit(john.getAccountNumber(), 5000.0));
        System.out.println("Withdrawal: "
                + service.withdraw(john.getAccountNumber(), 2000.0, 1234));
        System.out.println("Transfer: " + service.transfer(
                john.getAccountNumber(), jane.getAccountNumber(), 1000.0, 1234));

        System.out.println("\nFinal balances:");
        System.out.println("  John (Account #" + john.getAccountNumber()
                + "): Rs. " + john.getBalance());
        System.out.println("  Jane (Account #" + jane.getAccountNumber()
                + "): Rs. " + jane.getBalance());

        List<TransactionCommand> history = service.getTransactionHistory();
        System.out.println("\nTransaction history (" + history.size() + " records):");
        for (int index = 0; index < history.size(); index++) {
            System.out.println("  [" + (index + 1) + "] "
                    + history.get(index).getTransaction());
        }
    }
}
