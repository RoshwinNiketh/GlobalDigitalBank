package com.gdb.domain;

    // TODO: Step 3 - Centralise account creation with a switch on the account type:
    //   - If type is null, return null.
    //   - switch (type.toUpperCase()):
    //       "SAVINGS"             -> a new SavingsAccount      (minBalance 1000.0, interestRate 4.0)
    //       "CURRENT"             -> a new CurrentAccount      (overdraftLimit 25000.0)
    //       "FIXED_DEPOSIT", "FD" -> a new FixedDepositAccount (tenureMonths 12, interestRate 6.5)
    //       "SALARY"              -> a new SalaryAccount       (employerName "TechCorp")
    //       default               -> throw new IllegalArgumentException("Unknown account type: " + type)




public class AccountFactory {
    public static IAccount createAccount(
            String type, String accNum, String name, int age,
            double balance, String status, String pin) {

        if (type == null) {
            throw new IllegalArgumentException("Unknown account type: null");
        }

        return switch (type.toUpperCase()) {
            case "SAVINGS" -> new SavingsAccount(
                    accNum, name, age, balance, status, pin, 1000.0, 4.0);
            case "CURRENT" -> new CurrentAccount(
                    accNum, name, age, balance, status, pin, 25000.0);
            case "FIXED_DEPOSIT", "FD" -> new FixedDepositAccount(
                    accNum, name, age, balance, status, pin, 12, 6.5);
            case "SALARY" -> new SalaryAccount(
                    accNum, name, age, balance, status, pin, "TechCorp");
            default -> throw new IllegalArgumentException(
                    "Unknown account type: " + type);
        };
    }
}