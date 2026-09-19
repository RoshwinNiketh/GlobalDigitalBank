package com.gdb.domain;

import com.gdb.exceptions.InvalidAgeException;
import com.gdb.exceptions.MinimumBalanceViolationException;

public class SalaryAccount extends Account {
    public SalaryAccount(int accountNumber, String name, int age, double initialBalance)
            throws InvalidAgeException, MinimumBalanceViolationException {
        this(accountNumber, name, age, initialBalance, 0);
    }

    public SalaryAccount(int accountNumber, String name, int age, double initialBalance, int tenureYears)
            throws InvalidAgeException, MinimumBalanceViolationException {
        super(accountNumber, name, age, initialBalance, tenureYears);
        if (initialBalance < getMinimumBalance()) {
            throw new MinimumBalanceViolationException(
                    "Salary account requires minimum balance of Rs. " + getMinimumBalance());
        }
    }

    @Override
    public double getMinimumBalance() {
        return AccountRulesEngine.getInstance().getMinimumBalance("SALARY", tenureYears);
    }

    @Override
    public String getAccountType() {
        return "Salary";
    }

    @Override
    public double getInterestRate() {
        return AccountRulesEngine.getInstance().getInterestRate("SALARY", tenureYears);
    }

    @Override
    public boolean canWithdraw(double amount) {
        return amount > 0 && amount <= balance;
    }
}
