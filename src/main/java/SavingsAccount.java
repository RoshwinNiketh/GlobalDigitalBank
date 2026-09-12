public class SavingsAccount extends Account {

    private final double minBalance;
    private final double interestRate;

    public SavingsAccount(
            int accountNumber,
            String name,
            int age,
            double initialBalance,
            double minBalance,
            double interestRate
    ) {
        super(accountNumber, name, age, initialBalance, "SAVINGS");

        if (minBalance < 0) {
            throw new IllegalArgumentException(
                    "Minimum balance cannot be negative."
            );
        }

        if (initialBalance < minBalance) {
            throw new IllegalArgumentException(
                    "Initial balance must be at least Rs." + minBalance
            );
        }

        if (interestRate < 0) {
            throw new IllegalArgumentException(
                    "Interest rate cannot be negative."
            );
        }

        this.minBalance = minBalance;
        this.interestRate = interestRate;
    }

    public void applyInterest() {
        double interest = getBalance() * interestRate / 100.0;

        if (interest > 0) {
            creditBalance(interest);
        }
    }

    @Override
    protected double getMinimumBalance() {
        return minBalance;
    }

    public double getMinBalance() {
        return minBalance;
    }

    @Override
    public void withdraw(double amount)
            throws InvalidAmountException,
            InsufficientBalanceException,
            MinimumBalanceViolationException,
            InactiveAccountException,
            InvalidPinException {

        if (getBalance() - amount < minBalance) {
            throw new MinimumBalanceViolationException(
                    "Withdrawal would violate the minimum balance of Rs."
                            + minBalance
            );
        }

        super.withdraw(amount);
    }

    public double getInterestRate() {
        return interestRate;
    }
}