package com.gdb.tests;

import com.gdb.domain.Account;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.Transaction;
import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InvalidAmountException;
import com.gdb.exceptions.InvalidPinException;
import com.gdb.service.TransferService;

public class TestTransactionModel {
    public static void main(String[] args) throws Exception {
        printHeader();
        testSuccessfulTransactions();
        testFailedTransactions();
        testLegacyDeposit();
    }

    private static void printHeader() {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 16 - TRANSACTION MODEL TEST");
        System.out.println("=".repeat(60));
    }

    private static Account createSavingsAccount(
            int accountNumber, String name, double initialBalance) throws AccountException {
        Account account = (Account) AccountFactory.createAccount(
                "SAVINGS", accountNumber, name, 30, initialBalance);
        account.setPin(1234);
        return account;
    }

    private static void testSuccessfulTransactions() throws AccountException {
        Account source = createSavingsAccount(1001, "Rajesh Sharma", 50000.0);
        Account destination = createSavingsAccount(1002, "Priya Patel", 20000.0);
        TransferService transferService = new TransferService();

        Transaction deposit = source.depositWithTransaction(5000.0);
        requireSuccess(deposit, "deposit");
        System.out.println("[SUCCESS] Deposit Transaction: " + deposit);

        Transaction withdrawal = source.withdrawWithTransaction(2000.0, 1234);
        requireSuccess(withdrawal, "withdrawal");
        System.out.println("[SUCCESS] Withdrawal Transaction: " + withdrawal);

        Transaction transfer = transferService.transferWithTransaction(
                source, destination, 1000.0, 1234);
        requireSuccess(transfer, "transfer");
        System.out.println("[SUCCESS] Transfer Transaction: " + transfer);
    }

    private static void testFailedTransactions() throws AccountException {
        Account source = createSavingsAccount(2001, "Failure Test Source", 50000.0);
        Account destination = createSavingsAccount(2002, "Failure Test Destination", 20000.0);
        TransferService transferService = new TransferService();

        double balanceBeforeDeposit = source.getBalance();
        try {
            source.depositWithTransaction(0.0);
            throw new AssertionError("Zero-value deposit should fail");
        } catch (InvalidAmountException e) {
            requireUnchanged(source, balanceBeforeDeposit, "failed deposit");
            System.out.println("[FAILURE] Deposit rejected: " + e.getMessage());
        }

        double balanceBeforeWithdrawal = source.getBalance();
        try {
            source.withdrawWithTransaction(1000.0, 9999);
            throw new AssertionError("Withdrawal with incorrect PIN should fail");
        } catch (InvalidPinException e) {
            requireUnchanged(source, balanceBeforeWithdrawal, "failed withdrawal");
            System.out.println("[FAILURE] Withdrawal rejected: " + e.getMessage());
        }

        double sourceBeforeTransfer = source.getBalance();
        double destinationBeforeTransfer = destination.getBalance();
        try {
            transferService.transferWithTransaction(
                    source, destination, 1000.0, 9999);
            throw new AssertionError("Transfer with incorrect PIN should fail");
        } catch (InvalidPinException e) {
            requireUnchanged(source, sourceBeforeTransfer, "failed transfer source");
            requireUnchanged(destination, destinationBeforeTransfer,
                    "failed transfer destination");
            System.out.println("[FAILURE] Transfer rejected: " + e.getMessage());
        }
    }

    private static void testLegacyDeposit() throws AccountException {
        Account account = createSavingsAccount(3001, "Legacy Deposit Test", 50000.0);
        account.deposit(1000.0);

        if (account.getBalance() != 51000.0) {
            throw new AssertionError("Legacy deposit did not update the balance");
        }

        System.out.println("[LEGACY] Deposit +1000: " + account.getAccountInfo());
    }

    private static void requireSuccess(Transaction transaction, String operation) {
        if (transaction == null || !"SUCCESS".equals(transaction.getStatus())) {
            throw new AssertionError("Successful " + operation + " has no SUCCESS transaction");
        }
    }

    private static void requireUnchanged(Account account, double expected, String operation) {
        if (account.getBalance() != expected) {
            throw new AssertionError(operation + " changed the account balance");
        }
    }
}
