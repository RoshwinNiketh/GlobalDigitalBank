package com.gdb.tests;

import com.gdb.domain.*;
import com.gdb.exceptions.*;

public class TestAbstractAccount {
    public static void main(String[] args) {
        System.out.println("=== Activity 10: Banking Operations Suite ===");

        AbstractAccount[] accounts = createPortfolio();
        SavingsAccount savings = (SavingsAccount) accounts[0];
        CurrentAccount current = (CurrentAccount) accounts[1];

        testSuccessfulTransfer(savings, current);
        testFailedTransfer(savings, current);
        processMonthlyCycle(accounts);

        System.out.println("All banking operations passed!");
    }

    private static AbstractAccount[] createPortfolio() {
        SavingsAccount savings = new SavingsAccount(
                "SAV1001", "Roshwin Niketh", 19, 10000.0, "ACTIVE", "1234",4);
        CurrentAccount current = new CurrentAccount(
                "CUR1001", "Ashwanth", 25, 5000.0, "ACTIVE", "5678", 10000.0);
        SalaryAccount salary = new SalaryAccount(
                "SAL1001", "Vijay Kumar", 30, 2000.0, "ACTIVE", "1111", "Infosys");

        return new AbstractAccount[] {savings, current, salary};
    }

    private static void transfer(AbstractAccount source, AbstractAccount destination,
                                 double amount, String pin) throws AccountException {
        source.withdraw(amount, pin);
        destination.deposit(amount);
    }

    private static void testSuccessfulTransfer(SavingsAccount savings, CurrentAccount current) {
        try {
            transfer(savings, current, 3000.0, "1234");
        } catch (AccountException e) {
            throw new AssertionError("Valid transfer failed", e);
        }

        if (savings.getBalance() != 7000.0 || current.getBalance() != 8000.0) {
            throw new AssertionError("Transfer produced incorrect balances");
        }

        System.out.println("Transfer Rs 3000 from Savings to Current: SUCCESS");
        System.out.println("Savings Balance: Rs " + savings.getBalance()
                + " | Current Balance: Rs " + current.getBalance());
    }

    private static void testFailedTransfer(SavingsAccount savings, CurrentAccount current) {
        double savingsBefore = savings.getBalance();
        double currentBefore = current.getBalance();
        boolean wrongPinCaught = false;

        try {
            transfer(savings, current, 1000.0, "9999");
        } catch (InvalidPinException e) {
            wrongPinCaught = true;
        } catch (AccountException e) {
            throw new AssertionError("Unexpected transfer exception", e);
        }

        if (!wrongPinCaught
                || savings.getBalance() != savingsBefore
                || current.getBalance() != currentBefore) {
            throw new AssertionError("Wrong-PIN transfer changed a balance");
        }

        System.out.println("Failed Transfer (Wrong PIN): Exception caught, no balance changed [PASS]");
    }

    private static void processMonthlyCycle(AbstractAccount[] accounts) {
        for (AbstractAccount account : accounts) {
            if (account instanceof SavingsAccount savings) {
                savings.applyInterest();
            } else if (account instanceof SalaryAccount salary) {
                checkSalaryAccount(salary);
            }
        }

        System.out.println("Monthly Interest Cycle processed for all qualifying accounts.");
    }

    private static void checkSalaryAccount(SalaryAccount salary) {
        if (salary.getInactiveMonths() != 0) {
            throw new AssertionError("Salary account has missed a salary-credit month");
        }
    }
}
