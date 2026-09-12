public class CurrentAccount extends Account {

    private double overdraftLimit;

    public CurrentAccount(
            int accountNumber,
            String name,
            int age,
            double initialBalance,
            double overdraftLimit
    ) {
        super(accountNumber, name, age, initialBalance, "CURRENT");
        setOverdraftLimit(overdraftLimit);
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        if (overdraftLimit < 0) {
            throw new IllegalArgumentException(
                    "Overdraft limit cannot be negative."
            );
        }

        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(double amount)
            throws InvalidAmountException,
            InsufficientBalanceException,
            MinimumBalanceViolationException,
            InactiveAccountException,
            InvalidPinException {

        validateWithdrawalRequest(amount);

        if (amount > getBalance() + overdraftLimit) {
            throw new InsufficientBalanceException(
                    "Withdrawal exceeds the available balance and overdraft limit."
            );
        }

        debitBalance(amount);
    }
}