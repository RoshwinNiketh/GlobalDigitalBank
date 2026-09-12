public class TestAccountSubclasses {

    public static void main(String[] args) {

        System.out.println(
                "=== Activity 8: Polymorphism Test ==="
        );

        testSavingsWithdrawal();
        testCurrentOverdraft();
        testCurrentOverdraftExceeded();
        testFixedDepositWithdrawal();

        System.out.println(
                "All polymorphic behaviors verified!"
        );
    }

    private static void testSavingsWithdrawal() {

        Account account = new SavingsAccount(
                2001,
                "Aarav Sharma",
                25,
                10000.0,
                1000.0,
                4.0
        );

        account.setPin(1234);

        try {
            account.withdraw(9500.0);

            System.out.println(
                    "[Savings] Withdraw 9500 "
                            + "(breaches min balance 1000): "
                            + "FAILED"
            );

        } catch (MinimumBalanceViolationException e) {
            System.out.println(
                    "[Savings] Withdraw 9500 "
                            + "(breaches min balance 1000): "
                            + "Caught MinimumBalanceViolationException [PASS]"
            );

        } catch (AccountException e) {
            System.out.println(
                    "[Savings] Unexpected exception: "
                            + e.getClass().getSimpleName()
                            + " [FAILED]"
            );
        }
    }

    private static void testCurrentOverdraft() {

        Account account = new CurrentAccount(
                2002,
                "Meera Patel",
                30,
                10000.0,
                25000.0
        );

        account.setPin(1234);

        try {
            account.withdraw(15000.0);

            if (account.getBalance() == -5000.0) {
                System.out.println(
                        "[Current] Withdraw with Overdraft "
                                + "(Balance goes to -5000): "
                                + "SUCCESS [PASS]"
                );
            } else {
                System.out.println(
                        "[Current] Incorrect balance: Rs."
                                + account.getBalance()
                                + " [FAILED]"
                );
            }

        } catch (AccountException e) {
            System.out.println(
                    "[Current] Overdraft withdrawal failed: "
                            + e.getMessage()
                            + " [FAILED]"
            );
        }
    }

    private static void testCurrentOverdraftExceeded() {

        Account account = new CurrentAccount(
                2003,
                "Riya Shah",
                28,
                10000.0,
                25000.0
        );

        account.setPin(1234);

        try {
            account.withdraw(36000.0);

            System.out.println(
                    "[Current] Withdraw exceeding Overdraft "
                            + "(exceeds -25000): FAILED"
            );

        } catch (InsufficientBalanceException e) {
            System.out.println(
                    "[Current] Withdraw exceeding Overdraft "
                            + "(exceeds -25000): "
                            + "Caught InsufficientBalanceException [PASS]"
            );

        } catch (AccountException e) {
            System.out.println(
                    "[Current] Unexpected exception: "
                            + e.getClass().getSimpleName()
                            + " [FAILED]"
            );
        }
    }

    private static void testFixedDepositWithdrawal() {

        Account account = new FixedDepositAccount(
                2004,
                "Rohan Singh",
                35,
                10000.0,
                12,
                6.5
        );

        try {
            account.withdraw(1000.0);

            System.out.println(
                    "[FixedDeposit] Withdraw attempt: FAILED"
            );

        } catch (AccountException e) {
            System.out.println(
                    "[FixedDeposit] Withdraw attempt: "
                            + "Caught AccountException [PASS]"
            );
        }
    }
}