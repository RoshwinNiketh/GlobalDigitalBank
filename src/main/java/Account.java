public class Account{
    private int accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    private int pin;

    private static final double MIN_BALANCE_SAVINGS=500.0;
    private static final double MIN_BALANCE_CURRENT=1000.0;
    private static final int MIN_AGE=18;
    private static final int MIN_PIN=1000;
    private static final int MAX_PIN=9999;

    public Account(int accountNumber, String name, int age, double initialBalance, String accountType){
        this.accountNumber=accountNumber;
        this.name=name;
        this.age= (age>=MIN_AGE) ? age : MIN_AGE;
        if(accountType.equalsIgnoreCase("current")) {
            this.accountType = accountType;
            this.balance=(initialBalance<MIN_BALANCE_CURRENT) ? MIN_BALANCE_CURRENT : initialBalance;
        }
        else {
            this.accountType = "Savings";
            this.balance=(initialBalance<MIN_BALANCE_SAVINGS) ? MIN_BALANCE_SAVINGS : initialBalance;
        }
        this.status="Active";
        this.pin=0;
    }
    public boolean deposit(double amount){
        if(status.equals("Active") && amount>0){
            balance+=amount;
            return true;
        }
        return false;
    }

    public boolean withdraw(double amount) {
        if(amount>0 && amount<=balance){
            balance-=amount;
            return true;
        }
        return false;
    }

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
    boolean setPin(int pin) {
        if (pin >= MIN_PIN && pin <= MAX_PIN) {
            this.pin = pin;
            return true;
        }
        return false;
    }
    boolean verifyPin(int pin) {
        return this.pin != 0 && this.pin == pin;
    }

    boolean hasPin() {
        return pin != 0;
    }

    boolean isActive() {
        return status.equals("Active");
    }
    boolean closeAccount() {
        if (isActive()) {
            status = "Inactive";
            return true;
        }
        return false;
    }
    boolean reopenAccount() {
        if (!isActive()) {
            status = "Active";
            return true;
        }
        return false;
    }
}