import java.util.Scanner;
import java.lang.String;

public class TestAccountEnhanced {
    private static int accNo,age;
    private static String name,accType;
    private static double balance;
    private static int pin;

    TestAccountEnhanced(){
        accNo=0;
        age=0;
        name="";
        accType="";
        balance=0;
        pin=0;
    }

    public void input(){
        Scanner sc=new Scanner(System.in);
        System.out.print("Enter Account Number: ");
        accNo=sc.nextInt();
        sc.nextLine();
        System.out.print("Enter Name: ");
        name=sc.nextLine();
        System.out.print("Enter age: ");
        age=sc.nextInt();
        System.out.print("Enter Account type (Savings/Current): ");
        accType=sc.next();
        System.out.print("Enter balance: ");
        balance=sc.nextDouble();
        System.out.print("Enter pin: ");
        pin=sc.nextInt();
    }

    public void display(int accNo, String name, int age, String accType, double balance, String status,boolean hasPin){
        System.out.printf("Account #%d | %s (%d yrs) | %s | Rs.%.2f | %s | PIN: %s\n",accNo,name,age,accType,balance,status,((hasPin)?"Yes":"No"));
    }

    public static void main(String[] args){
        TestAccountEnhanced ob=new TestAccountEnhanced();
        System.out.println("=".repeat(50));
        System.out.println("   ENHANCED BANK-ACCOUNT TEST (BOOLEAN RETURNS)");
        System.out.println("=".repeat(50));

        System.out.println("\n>>>Test 1.Valid Account Creation (pin=0)");
        ob.input();
        Account acc1=new Account(accNo,name,age,balance,accType);
        System.out.println("Account Created!");
        ob.display(acc1.getAccountNumber(),acc1.getName(),acc1.getAge(),acc1.getAccountType(),acc1.getBalance(),acc1.getStatus(),acc1.hasPin());

        System.out.println("\n>>>2.Test 2: Invalid Age (under 18) (pin=0)");
        ob.input();
        Account acc2=new Account(accNo,name,age,balance,accType);
        System.out.println("Creating account with age "+age);
        System.out.println("Age auto-corrected to: "+acc2.getAge());
        ob.display(acc2.getAccountNumber(),acc2.getName(),acc2.getAge(),acc2.getAccountType(),acc2.getBalance(),acc2.getStatus(),acc2.hasPin());


        System.out.println("\n>>>Test 3: Invalid Account Type (pin=0)");
        ob.input();
        Account acc3=new Account(accNo,name,age,balance,accType);
        System.out.printf("Creating account with type \"%s\"\n",accType);
        System.out.println("Account type defaulted to: "+acc3.getAccountType());
        ob.display(acc3.getAccountNumber(),acc3.getName(),acc3.getAge(),acc3.getAccountType(),acc3.getBalance(),acc3.getStatus(),acc3.hasPin());

        System.out.println("\n>>>Test 4: Minimum Balance Enforcement on Creation (pin=0)");
        ob.input();
        Account acc4=new Account(accNo,name,age,balance,accType);
        System.out.printf("Creating Savings account with ₹%.2f (below minimum)\n",balance);
        System.out.println("Balance auto-corrected to minimum: "+acc4.getBalance());
        ob.display(acc4.getAccountNumber(),acc4.getName(),acc4.getAge(),acc4.getAccountType(),acc4.getBalance(),acc4.getStatus(),acc4.hasPin());

        System.out.println("\n>>>Test 5: Withdrawal with Minimum Balance (enter any pin)");
        ob.input();
        Account acc5=new Account(accNo,name,age,balance,accType);
        acc5.setPin(pin);
        System.out.print("Initial: "); ob.display(acc5.getAccountNumber(),acc5.getName(),acc5.getAge(),acc5.getAccountType(),acc5.getBalance(),acc5.getStatus(),acc5.hasPin());
        System.out.println("Withdrawing Rs.200.0: "+((acc5.withdraw(200.0))?"SUCCESS":"FAILED"));
        System.out.println("New balance: "+acc5.getBalance());
        System.out.print("After withdrawal: "); ob.display(acc5.getAccountNumber(),acc5.getName(),acc5.getAge(),acc5.getAccountType(),acc5.getBalance(),acc5.getStatus(),acc5.hasPin());
        System.out.println("Withdrawing Rs.900.0: "+((acc5.withdraw(900.0))?"SUCCESS":"FAILED"));
        System.out.println("Current balance: "+acc5.getBalance());

        System.out.println("\n>>>Test 6.Account Status Management (pin=0)");
        ob.input();
        Account acc6=new Account(accNo,name,age,balance,accType);
        System.out.print("Initial: "); ob.display(acc6.getAccountNumber(),acc6.getName(),acc6.getAge(),acc6.getAccountType(),acc6.getBalance(),acc6.getStatus(),acc6.hasPin());
        System.out.println("CLOSING ACCOUNT: "+((acc6.closeAccount())?"SUCCESS":"FAILED"));
        System.out.print("After Close: "); ob.display(acc6.getAccountNumber(),acc6.getName(),acc6.getAge(),acc6.getAccountType(),acc6.getBalance(),acc6.getStatus(),acc6.hasPin());
        System.out.println("Depositing Rs.500.0 to closed account: "+((acc6.deposit(500.0))?"SUCCESS":"FAILED"));
        System.out.println("Reopening account: "+((acc6.reopenAccount())?"SUCCESS":"FAILED"));
        System.out.println("After reopen: "); ob.display(acc6.getAccountNumber(),acc6.getName(),acc6.getAge(),acc6.getAccountType(),acc6.getBalance(),acc6.getStatus(),acc6.hasPin());

        System.out.println("\n>>>Test 7.PIN Protection (do not enter pin=9999 for the test)");
        ob.input();
        Account acc7=new Account(accNo,name,age,balance,accType);
        acc7.setPin(pin);
        System.out.printf("Setting PIN %d: %s\n",pin,((acc7.hasPin())?"SUCCESS":"FAILED"));
        System.out.printf("Withdrawing Rs.200.0 with correct PIN (%d): %s\n",pin,((acc7.verifyPin(pin) && acc7.withdraw(200.0)?"SUCCESS":"FAILED")));
        System.out.println("New balance: "+acc7.getBalance());
        System.out.printf("Withdrawing Rs.100.0 with incorrect PIN (9999): %s\n",acc7.verifyPin(9999)?"SUCCESS":"FAILED (incorrect PIN)");
        acc7.setPin(0);
        System.out.printf("Withdrawing Rs.100.0 with PIN not set: %s\n",acc7.verifyPin(pin)?"SUCCESS":"FAILED (PIN not set)");

        System.out.println("\n>>>8.All Accounts Summary");
        ob.display(acc1.getAccountNumber(),acc1.getName(),acc1.getAge(),acc1.getAccountType(),acc1.getBalance(),acc1.getStatus(),acc1.hasPin());
        ob.display(acc2.getAccountNumber(),acc2.getName(),acc2.getAge(),acc2.getAccountType(),acc2.getBalance(),acc2.getStatus(),acc2.hasPin());
        ob.display(acc3.getAccountNumber(),acc3.getName(),acc3.getAge(),acc3.getAccountType(),acc3.getBalance(),acc3.getStatus(),acc3.hasPin());
        ob.display(acc4.getAccountNumber(),acc4.getName(),acc4.getAge(),acc4.getAccountType(),acc4.getBalance(),acc4.getStatus(),acc4.hasPin());
        ob.display(acc5.getAccountNumber(),acc5.getName(),acc5.getAge(),acc5.getAccountType(),acc5.getBalance(),acc5.getStatus(),acc5.hasPin());
        ob.display(acc6.getAccountNumber(),acc6.getName(),acc6.getAge(),acc6.getAccountType(),acc6.getBalance(),acc6.getStatus(),acc6.hasPin());
        ob.display(acc7.getAccountNumber(),acc7.getName(),acc7.getAge(),acc7.getAccountType(),acc7.getBalance(),acc7.getStatus(),acc7.hasPin());

        System.out.println("=".repeat(50));
        System.out.println("           ENHANCED TEST COMPLETED");
        System.out.println("=".repeat(50));
    }
}
