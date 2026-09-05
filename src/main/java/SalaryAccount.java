public class SalaryAccount extends Account {

    private String employerName;
    private int inactiveMonths;

    public SalaryAccount(
            int accountNumber,
            String name,
            int age,
            double initialBalance,
            String employerName
    ) {
        super(accountNumber, name, age, initialBalance, "SALARY");

        setEmployerName(employerName);
        this.inactiveMonths = 0;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        if (employerName == null || employerName.isBlank()) {
            throw new IllegalArgumentException(
                    "Employer name cannot be empty."
            );
        }

        this.employerName = employerName;
    }

    public int getInactiveMonths() {
        return inactiveMonths;
    }

    public void setInactiveMonths(int inactiveMonths) {
        if (inactiveMonths < 0) {
            throw new IllegalArgumentException(
                    "Inactive months cannot be negative."
            );
        }

        this.inactiveMonths = inactiveMonths;
    }
}