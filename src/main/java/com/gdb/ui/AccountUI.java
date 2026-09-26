package com.gdb.ui;

import com.gdb.command.TransactionCommand;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.service.AccountService;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/** Menu-driven console interface that delegates all banking operations to AccountService. */
public class AccountUI {
    private final AccountService service;
    private final Scanner scanner;

    public AccountUI(AccountService service) {
        this.service = Objects.requireNonNull(service, "Account service is required");
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            displayMainMenu();
            String input = readString("Enter your choice: ");
            if (input == null) {
                System.out.println("\nInput closed. Goodbye.");
                return;
            }

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number from 1 to 8.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        handleOpenAccount();
                        break;
                    case 2:
                        handleDeposit();
                        break;
                    case 3:
                        handleWithdraw();
                        break;
                    case 4:
                        handleTransfer();
                        break;
                    case 5:
                        handleCloseAccount();
                        break;
                    case 6:
                        handleViewAccount();
                        break;
                    case 7:
                        handleViewTransactions();
                        break;
                    case 8:
                        System.out.println("Thank you! Goodbye.");
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter 1-8.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n========================================");
        System.out.println("   GLOBAL DIGITAL BANK");
        System.out.println("========================================");
        System.out.println("1. Open Account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Close Account");
        System.out.println("6. View Account Details");
        System.out.println("7. View Transaction History");
        System.out.println("8. Exit");
        System.out.println("========================================");
    }

    private void handleOpenAccount() throws Exception {
        System.out.println("\n--- Open Account ---");
        String type = readString("Account Type (Savings/Current/FixedDeposit/Salary): ");
        if (type == null) return;
        String name = readString("Name: ");
        if (name == null) return;
        int age = readInt("Age: ");
        double initialBalance = readDouble("Initial Balance: ");
        int pin = readInt("Set 4-digit PIN: ");
        IAccount account = service.openAccount(type, name, age, initialBalance);
        account.setPin(pin);
        System.out.println("SUCCESS: " + account.getAccountInfo());
        System.out.println("PIN set successfully.");
    }

    private void handleDeposit() throws Exception {
        System.out.println("\n--- Deposit ---");
        int accountNumber = readInt("Account Number: ");
        double amount = readDouble("Amount to deposit: ");
        Transaction transaction = service.deposit(accountNumber, amount);
        IAccount account = service.getAccount(accountNumber);
        System.out.println("SUCCESS: Deposited Rs. " + transaction.getAmount()
                + " to #" + accountNumber + ". New balance: Rs. "
                + account.getBalance());
    }

    private void handleWithdraw() throws Exception {
        System.out.println("\n--- Withdraw ---");
        int accountNumber = readInt("Account Number: ");
        double amount = readDouble("Amount to withdraw: ");
        int pin = readInt("PIN: ");
        Transaction transaction = service.withdraw(accountNumber, amount, pin);
        IAccount account = service.getAccount(accountNumber);
        System.out.println("SUCCESS: Withdrew Rs. " + transaction.getAmount()
                + " from #" + accountNumber + ". New balance: Rs. "
                + account.getBalance());
    }

    private void handleTransfer() throws Exception {
        System.out.println("\n--- Transfer ---");
        int fromAccount = readInt("From Account: ");
        int toAccount = readInt("To Account: ");
        double amount = readDouble("Amount: ");
        int pin = readInt("PIN: ");
        Transaction transaction = service.transfer(fromAccount, toAccount, amount, pin);
        System.out.println("SUCCESS: Transferred Rs. " + transaction.getAmount()
                + " from #" + fromAccount + " to #" + toAccount + ". New balance: Rs. "
                + service.getAccount(fromAccount).getBalance());
    }

    private void handleCloseAccount() throws Exception {
        System.out.println("\n--- Close Account ---");
        int accountNumber = readInt("Account Number: ");
        int pin = readInt("PIN: ");
        service.closeAccount(accountNumber, pin);
        System.out.println("SUCCESS: Account #" + accountNumber + " closed.");
    }

    private void handleViewAccount() {
        System.out.println("\n--- View Account Details ---");
        int accountNumber = readInt("Account Number: ");
        IAccount account = service.getAccount(accountNumber);
        if (account == null) {
            System.out.println("Account not found: " + accountNumber);
        } else {
            System.out.println(account.getAccountInfo());
        }
    }

    private void handleViewTransactions() {
        List<TransactionCommand> history = service.getTransactionHistory();
        if (history.isEmpty()) {
            System.out.println("No transactions logged.");
            return;
        }
        System.out.println("\n--- Transaction History ---");
        for (int index = 0; index < history.size(); index++) {
            System.out.println("[" + (index + 1) + "] "
                    + history.get(index).getTransaction());
        }
    }

    private int readInt(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input == null) {
                throw new IllegalStateException("Input closed.");
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input == null) {
                throw new IllegalStateException("Input closed.");
            }
            try {
                double value = Double.parseDouble(input);
                if (!Double.isFinite(value)) {
                    System.out.println("Please enter a finite numeric amount.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric amount.");
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }
}
