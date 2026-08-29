public class Account{

    private static final double MIN_BALANCE_SAVINGS=500.0;
    private static final double MIN_BALANCE_CURRENT=1000.0;
    private static final int MIN_AGE=18;
    private static final int MIN_PIN=1000;
    private static final int MAX_PIN=9999;

    private int accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    private Integer pin;

    public Account(int accountNumber, String name, int age, double initialBalance, String accountType){
        if (age < MIN_AGE) {
            throw new IllegalArgumentException("Age must be at least " + MIN_AGE);
        }

        if (accountType == null || (!accountType.equalsIgnoreCase("Savings") && !accountType.equalsIgnoreCase("Current"))) {
            throw new IllegalArgumentException("Account type must be 'Savings' or 'Current'");
        }

        double minBal = accountType.equalsIgnoreCase("Current") ? MIN_BALANCE_CURRENT : MIN_BALANCE_SAVINGS;
        if (initialBalance < minBal) {
            throw new IllegalArgumentException("Initial balance for " + accountType + " account must be at least Rs." + minBal);
        }

        this.accountNumber = accountNumber;
        this.name = name;
        this.age = age;
        this.balance = initialBalance;
        this.accountType = accountType.equalsIgnoreCase("Current") ? "Current" : "Savings";
        this.status = "Active";
        this.pin =null;
    }
    public void deposit(double amount) throws InvalidAmountException, InactiveAccountException{
        validateActive();
        if(amount<=0){
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }
        this.balance+=amount;
    }

    public void withdraw(double amount) throws InvalidAmountException,
            InsufficientBalanceException, MinimumBalanceViolationException,
            InactiveAccountException, InvalidPinException {

        validateActive();
        if (!hasPin()) {
            throw new InvalidPinException("PIN has not been set for this account.");
        }
        if (!verifyPin(pin)) {
            throw new InvalidPinException("Incorrect PIN entered.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        if (amount > this.balance) {
            throw new InsufficientBalanceException("Insufficient funds. Available balance: Rs." + this.balance);
        }
        if ((this.balance - amount) < getMinimumBalance()) {
            throw new MinimumBalanceViolationException("Transaction declined. Account must maintain a minimum balance of Rs." + getMinimumBalance());
        }

        this.balance -= amount;
    }
    public void setPin(int pin) throws IllegalArgumentException{
        if (pin < MIN_PIN || pin > MAX_PIN) {
            throw new IllegalArgumentException("PIN must be a valid 4-digit number (1000-9999).");
        }
        this.pin = pin;
    }
    public boolean verifyPin(int pin) {
        return hasPin() && this.pin == pin;
    }

    public boolean hasPin() {
        return this.pin != null;
    }

    private double getMinimumBalance() {
        return this.accountType.equalsIgnoreCase("Current") ? MIN_BALANCE_CURRENT : MIN_BALANCE_SAVINGS;
    }

    boolean isActive() {
        return "Active".equalsIgnoreCase(this.status);
    }
    public void closeAccount() throws IllegalStateException{
        if (!isActive()) {
            throw new IllegalStateException("Account is already closed/inactive.");
        }
        this.status = "Inactive";
    }
    public void reopenAccount() throws IllegalStateException{
        if (isActive()) {
            throw new IllegalStateException("Account is already active.");
        }
        this.status = "Active";
    }
    private void validateActive() throws InactiveAccountException {
        if (!isActive()) {
            throw new InactiveAccountException("Cannot perform operation. Account is currently inactive.");
        }
    }

    //Getters and Setters
    public int getAccountNumber(){
        return accountNumber;
    }

    public String getName(){
        return name;
    }

    public int getAge(){
        return age;
    }

    public double getBalance(){
        return balance;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getStatus(){
        return status;
    }

    public void setName(String name){
        this.name=name;
    }

    public void setAge(int age){
        this.age=age;
    }
}