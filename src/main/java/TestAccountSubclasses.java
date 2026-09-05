public class TestAccountSubclasses {

    public static void main(String[] args) {

        System.out.println(
                "=== Activity 7: Account Subclasses Test ==="
        );

        SavingsAccount savings = new SavingsAccount(
                2001,
                "Aarav Sharma",
                25,
                10000.0,
                1000.0,
                4.0
        );

        CurrentAccount current = new CurrentAccount(
                2002,
                "Meera Patel",
                30,
                10000.0,
                25000.0
        );

        FixedDepositAccount fixedDeposit =
                new FixedDepositAccount(
                        2003,
                        "Rohan Singh",
                        35,
                        10000.0,
                        12,
                        6.5
                );

        SalaryAccount salary = new SalaryAccount(
                2004,
                "Priya Nair",
                28,
                5000.0,
                "Infosys"
        );

        System.out.printf(
                "Savings Account Created: Balance Rs %.1f"
                        + " | Min Balance: Rs %.1f%n",
                savings.getBalance(),
                savings.getMinBalance()
        );

        System.out.printf(
                "Current Account Created: Overdraft Limit Rs %.1f%n",
                current.getOverdraftLimit()
        );

        System.out.printf(
                "Fixed Deposit Created: Tenure %d months"
                        + " | Interest: %.1f%%%n",
                fixedDeposit.getTenureMonths(),
                fixedDeposit.getInterestRate()
        );

        System.out.printf(
                "Salary Account Created: Employer %s%n",
                salary.getEmployerName()
        );

        System.out.println(
                "All subclasses instantiated successfully!"
        );
    }
}