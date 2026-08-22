public class Account{
    private int accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;

    public Account(int accountNumber, String name, int age, double initialBalance, String accountType){
        this.accountNumber=accountNumber;
        this.name=name;
        this.age= (age>=18) ? age : 18;
        if(accountType.equalsIgnoreCase("current")) {
            this.accountType = accountType;
            this.balance=(initialBalance<1000.0) ? 1000.0 : initialBalance;
        }
        else {
            this.accountType = "Savings";
            this.balance=(initialBalance<1000.0) ? 1000.0 : initialBalance;
        }
        this.status="Active";
    }
    public boolean deposit(double amount){
        if(amount<=0){
            return false;
        }
        balance+=amount;
        return true;
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
}