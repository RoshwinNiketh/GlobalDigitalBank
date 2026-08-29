import java.util.Scanner;

public class TestAccount {
    private static int accNo,age;
    private static String name,accType;
    private static double balance;

    TestAccount(){
        accNo=0;
        age=0;
        name="";
        accType="";
        balance=0;
    }

    public void input(){
        Scanner sc=new Scanner(System.in);
        System.out.println("Enter Account Number: ");
        accNo=sc.nextInt();
        sc.nextLine();
        System.out.println("Enter Name: ");
        name=sc.nextLine();
        System.out.println("Enter age: ");
        age=sc.nextInt();
        System.out.println("Enter Account type (Savings/Current): ");
        accType=sc.next();
        System.out.println("Enter balance: ");
        balance=sc.nextDouble();
    }

    public void display(int accNo, String name, int age, String accType, double balance, String status){
        System.out.printf("Account #%d | %s (%d yrs) | %s | Rs.%.2f | %s | %s\n",accNo,name,age,accType,balance,status);
    }

    public static void main(String[] args){
        TestAccount ob=new TestAccount();
        System.out.println("=================================================");
        System.out.println("         GLOBAL DIGITAL BANK-ACCOUNT TEST        ");
        System.out.println("=================================================");

        System.out.println(">>> 1.Creating Account #1");
        ob.input();

        Account acc1=new Account(accNo,name,age,balance,accType);
        System.out.println("Account Created!");
        ob.display(acc1.getAccountNumber(),acc1.getName(),acc1.getAge(),acc1.getAccountType(),acc1.getBalance(),acc1.getStatus());

        System.out.println(">>>2.Deposit Money");
        System.out.println("Depositing Rs.500.0: "+acc1.deposit(500.0));
        System.out.println("New Balance: Rs."+acc1.getBalance());
        System.out.println("Depositing Rs.-100.0: "+acc1.deposit(-100.0));

        System.out.println(">>>3.Withdraw Money");
        System.out.println("Withdrawing Rs.200.0: "+acc1.withdraw(200.0));
        System.out.println("New Balance: Rs."+acc1.getBalance());
        System.out.println("Withdrawing Rs.2000.0: "+acc1.withdraw(2000.0));
        System.out.println("Current Balance: "+acc1.getBalance());

        System.out.println(">>>4.Creating Another Account");
        ob.input();

        Account acc2=new Account(accNo,name,age,balance,accType);
        ob.display(acc2.getAccountNumber(),acc2.getName(),acc2.getAge(),acc2.getAccountType(),acc2.getBalance(),acc2.getStatus());

        System.out.println("\n>>>5.All Accounts");
        ob.display(acc1.getAccountNumber(),acc1.getName(),acc1.getAge(),acc1.getAccountType(),acc1.getBalance(),acc1.getStatus());
        ob.display(acc2.getAccountNumber(),acc2.getName(),acc2.getAge(),acc2.getAccountType(),acc2.getBalance(),acc2.getStatus());
    }
}
