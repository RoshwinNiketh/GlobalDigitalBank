import java.util.Scanner;

public class TestAccount {
    public static void main(String[] args){
        Scanner sc=new Scanner(System.in);
        int accNo=0,age=0;
        String name="",accType="";
        double balance=0.0;
        System.out.println("=================================================");
        System.out.println("         GLOBAL DIGITAL BANK-ACCOUNT TEST        ");
        System.out.println("=================================================");

        System.out.println(">>> 1.Creating Account #1");
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

        Account acc1=new Account(accNo,name,age,balance,accType);
        System.out.println("Account Created!");
        System.out.printf("Account #%d | %s (%d yrs) | %s | Rs.%.2f | %s | %s",acc1.getAccountNumber(),acc1.getName(),acc1.getAge(),acc1.getAccountType(),acc1.getBalance(),acc1.getStatus(), acc1.getStatus());

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

        Account acc2=new Account(accNo,name,age,balance,accType);
        System.out.printf("Account #%d | %s (%d yrs) | %s | Rs.%.2f | %s",acc2.getAccountNumber(),acc2.getName(),acc2.getAge(),acc2.getAccountType(),acc2.getBalance(),acc2.getStatus());

        System.out.println("\n>>>5.All Accounts");
        System.out.printf("Account #%d | %s (%d yrs) | %s | Rs.%.2f | %s",acc1.getAccountNumber(),acc1.getName(),acc1.getAge(),acc1.getAccountType(),acc1.getBalance(),acc1.getStatus());
        System.out.printf("\nAccount #%d | %s (%d yrs) | %s | Rs.%.2f | %s",acc2.getAccountNumber(),acc2.getName(),acc2.getAge(),acc2.getAccountType(),acc2.getBalance(),acc2.getStatus());
    }
}
