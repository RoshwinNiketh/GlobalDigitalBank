public class FixedDepositAccount extends Account {

    private final int tenureMonths;
    private final double interestRate;

    public FixedDepositAccount(
            int accountNumber,
            String name,
            int age,
            double initialBalance,
            int tenureMonths,
            double interestRate
    ) {
        super(
                accountNumber,
                name,
                age,
                initialBalance,
                "FIXED_DEPOSIT"
        );

        if (tenureMonths <= 0) {
            throw new IllegalArgumentException(
                    "Tenure must be greater than zero months."
            );
        }

        if (interestRate < 0) {
            throw new IllegalArgumentException(
                    "Interest rate cannot be negative."
            );
        }

        this.tenureMonths = tenureMonths;
        this.interestRate = interestRate;
    }

    public double calculateMaturityAmount() {
        double tenureInYears = tenureMonths / 12.0;

        return getBalance()
                * Math.pow(1.0 + interestRate / 100.0, tenureInYears);
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public double getInterestRate() {
        return interestRate;
    }
}