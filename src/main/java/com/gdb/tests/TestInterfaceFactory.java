package com.gdb.tests;

import com.gdb.domain.*;
import com.gdb.exceptions.*;

public class TestInterfaceFactory {
    public static void main(String[] args) throws AccountException {
        System.out.println("=== Activity 12: Factory-Driven System Suite ===");

        testSavingsAccount();
        testCurrentAccount();
        testFixedDepositAccount();
        testInvalidType();

        System.out.println("Factory-driven architecture successfully verified!");
    }

    private static void testSavingsAccount() throws AccountException {
        IAccount savings = AccountFactory.createAccount(
                "SAVINGS", "SAV1001", "Roshwin Niketh",
                19, 5000.0, "ACTIVE", "1234");

        savings.deposit(1000.0);
        savings.withdraw(4000.0, "1234");

        boolean minimumBalanceEnforced = false;
        try {
            savings.withdraw(1500.0, "1234");
        } catch (MinimumBalanceViolationException e) {
            minimumBalanceEnforced = true;
        }

        if (!minimumBalanceEnforced || savings.getBalance() != 2000.0) {
            throw new AssertionError("Savings minimum balance test failed");
        }

        System.out.println("[Test 1] Savings Account Creation & Deposit: [PASS]");
    }

    private static void testCurrentAccount() throws AccountException {
        IAccount current = AccountFactory.createAccount(
                "CURRENT", "CUR1001", "Priya Patel",
                34, 2000.0, "ACTIVE", "5678");

        current.withdraw(27000.0, "5678");

        boolean overdraftLimitEnforced = false;
        try {
            current.withdraw(1.0, "5678");
        } catch (InsufficientBalanceException e) {
            overdraftLimitEnforced = true;
        }

        if (!overdraftLimitEnforced || current.getBalance() != -25000.0) {
            throw new AssertionError("Current account overdraft test failed");
        }

        System.out.println("[Test 2] Current Account Overdraft Withdrawal: [PASS]");
    }

    private static void testFixedDepositAccount() {
        IAccount fixedDeposit = AccountFactory.createAccount(
                "FIXED_DEPOSIT", "FD1001", "Amit Kumar",
                45, 50000.0, "ACTIVE", "1111");

        boolean withdrawalBlocked = false;
        try {
            fixedDeposit.withdraw(1000.0, "1111");
        } catch (AccountException e) {
            withdrawalBlocked = true;
        }

        if (!withdrawalBlocked || fixedDeposit.getBalance() != 50000.0) {
            throw new AssertionError("Fixed deposit withdrawal test failed");
        }

        System.out.println("[Test 3] Fixed Deposit Premature Withdrawal Block: [PASS]");
    }

    private static void testInvalidType() {
        boolean invalidTypeRejected = false;
        try {
            AccountFactory.createAccount(
                    "UNKNOWN", "BAD1001", "Test Customer",
                    30, 1000.0, "ACTIVE", "1234");
        } catch (IllegalArgumentException e) {
            invalidTypeRejected = true;
        }

        if (!invalidTypeRejected) {
            throw new AssertionError("Unknown account type was accepted");
        }

        System.out.println("[Test 4] Invalid Type Rejection: [PASS]");
    }
}
