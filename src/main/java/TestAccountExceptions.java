import java.util.Scanner;

public class TestAccountExceptions {

    private static int accNo, age, pin;
    private static String name, accType;
    private static double balance;

    public void input() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Account Number: ");
        accNo = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Name: ");
        name = sc.nextLine();

        System.out.print("Enter Age: ");
        age = sc.nextInt();

        System.out.print("Enter Account Type (Savings/Current): ");
        accType = sc.next();

        System.out.print("Enter Initial Balance: ");
        balance = sc.nextDouble();

        System.out.print("Enter PIN (1000-9999): ");
        pin = sc.nextInt();
    }

    public void display(Account acc) {
        System.out.printf(
                "Account #%d | %s (%d yrs) | %s | Rs.%.2f | %s | PIN: %s%n",
                acc.getAccountNumber(),
                acc.getName(),
                acc.getAge(),
                acc.getAccountType(),
                acc.getBalance(),
                acc.getStatus(),
                acc.hasPin() ? "Yes" : "No"
        );
    }

    public static void main(String[] args) {

        TestAccountExceptions test = new TestAccountExceptions();

        System.out.println("=".repeat(50));
        System.out.println("          ACCOUNT TEST WITH EXCEPTIONS");
        System.out.println("=".repeat(50));

        // ---------------------------------------------------------
        // TEST 1: Valid Account Creation
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 1: Valid Account Creation");

        try {
            test.input();

            Account acc1 = new Account(
                    accNo, name, age, balance, accType
            );

            System.out.println("Account created successfully.");
            test.display(acc1);

        } catch (IllegalArgumentException e) {
            System.out.println("Account creation failed: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 2: Invalid Age
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 2: Invalid Age");

        try {
            test.input();

            Account acc2 = new Account(
                    accNo, name, age, balance, accType
            );

            System.out.println("Account created successfully.");
            test.display(acc2);

        } catch (IllegalArgumentException e) {
            System.out.println("Expected exception caught: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 3: Invalid Account Type
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 3: Invalid Account Type");

        try {
            test.input();

            Account acc3 = new Account(
                    accNo, name, age, balance, accType
            );

            System.out.println("Account created successfully.");
            test.display(acc3);

        } catch (IllegalArgumentException e) {
            System.out.println("Expected exception caught: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 4: Minimum Balance Violation During Creation
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 4: Minimum Balance During Creation");

        try {
            test.input();

            Account acc4 = new Account(
                    accNo, name, age, balance, accType
            );

            System.out.println("Account created successfully.");
            test.display(acc4);

        } catch (IllegalArgumentException e) {
            System.out.println("Expected exception caught: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 5: Invalid PIN
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 5: Invalid PIN");

        try {
            test.input();

            Account acc5 = new Account(
                    accNo, name, age, balance, accType
            );

            System.out.println("Account created successfully.");

            try {
                acc5.setPin(pin);
                System.out.println("PIN set successfully.");
            } catch (IllegalArgumentException e) {
                System.out.println("PIN error: " + e.getMessage());
            }

            test.display(acc5);

        } catch (IllegalArgumentException e) {
            System.out.println("Account creation failed: " + e.getMessage());
        }


        // ---------------------------------------------------------
// TEST 6: Invalid Deposit (Negative Amount)
// ---------------------------------------------------------
        System.out.println("\n>>> TEST 6: Invalid Deposit (Negative Amount)");

        try {
            Account acc6 = new Account(
                    1006,
                    "Test User",
                    25,
                    1000.0,
                    "Savings"
            );

            System.out.println("Attempting to deposit ₹-100.0");

            try {
                acc6.deposit(-100.0);

                System.out.println("Invalid deposit accepted.");

            } catch (InvalidAmountException e) {
                System.out.println(
                        "EXCEPTION: Deposit amount must be positive. " +
                                "Provided: ₹-100.0"
                );

            } catch (InactiveAccountException e) {
                System.out.println(
                        "EXCEPTION: " + e.getMessage()
                );
            }

        } catch (IllegalArgumentException e) {
            System.out.println(
                    "EXCEPTION: " + e.getMessage()
            );
        }

        // ---------------------------------------------------------
        // TEST 7: Withdrawal Exceptions
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 7: Withdrawal");

        try {
            test.input();

            Account acc7 = new Account(
                    accNo, name, age, balance, accType
            );

            acc7.setPin(pin);

            System.out.println("Initial account:");
            test.display(acc7);

            try {
                acc7.withdraw(200.0);
                System.out.println("Withdrawal successful.");
                System.out.println("New balance: Rs." + acc7.getBalance());

            } catch (InvalidAmountException |
                     InsufficientBalanceException |
                     MinimumBalanceViolationException |
                     InactiveAccountException |
                     InvalidPinException e) {

                System.out.println("Withdrawal failed: " + e.getMessage());
            }

            try {
                acc7.withdraw(-100.0);
                System.out.println("Invalid withdrawal accepted.");

            } catch (InvalidAmountException e) {
                System.out.println("Expected exception caught: " + e.getMessage());

            } catch (InsufficientBalanceException |
                     MinimumBalanceViolationException |
                     InactiveAccountException |
                     InvalidPinException e) {

                System.out.println("Withdrawal failed: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Account creation/PIN error: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 8: Insufficient Balance
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 8: Insufficient Balance");

        try {
            Account acc8 = new Account(
                    1008, "Test User", 25, 2000.0, "Savings"
            );

            acc8.setPin(1234);

            try {
                acc8.withdraw(3000.0);
                System.out.println("Withdrawal successful.");

            } catch (InsufficientBalanceException e) {
                System.out.println("Expected exception caught: " + e.getMessage());

            } catch (InvalidAmountException |
                     MinimumBalanceViolationException |
                     InactiveAccountException |
                     InvalidPinException e) {

                System.out.println("Withdrawal failed: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Account creation failed: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 9: Minimum Balance Violation
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 9: Minimum Balance Violation");

        try {
            Account acc9 = new Account(
                    1009, "Test User", 25, 1000.0, "Savings"
            );

            acc9.setPin(1234);

            try {
                acc9.withdraw(600.0);

                System.out.println("Withdrawal successful.");

            } catch (MinimumBalanceViolationException e) {
                System.out.println("Expected exception caught: " + e.getMessage());

            } catch (InvalidAmountException |
                     InsufficientBalanceException |
                     InactiveAccountException |
                     InvalidPinException e) {

                System.out.println("Withdrawal failed: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Account creation failed: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 10: Account Close / Reopen
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 10: Account Status Management");

        try {
            Account acc10 = new Account(
                    1010, "Test User", 25, 2000.0, "Savings"
            );

            System.out.println("Initial status: " + acc10.getStatus());

            acc10.closeAccount();
            System.out.println("After closing: " + acc10.getStatus());

            try {
                acc10.deposit(500.0);

            } catch (InactiveAccountException e) {
                System.out.println(
                        "Expected exception caught: " + e.getMessage()
                );

            } catch (InvalidAmountException e) {
                System.out.println(
                        "Deposit failed: " + e.getMessage()
                );
            }

            acc10.reopenAccount();
            System.out.println("After reopening: " + acc10.getStatus());

        } catch (IllegalStateException e) {
            System.out.println("Status error: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Account creation failed: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 11: Reopen Active Account
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 11: Reopen Already Active Account");

        try {
            Account acc11 = new Account(
                    1011, "Test User", 25, 2000.0, "Savings"
            );

            try {
                acc11.reopenAccount();

            } catch (IllegalStateException e) {
                System.out.println(
                        "Expected exception caught: " + e.getMessage()
                );
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Account creation failed: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // TEST 12: PIN Verification
        // ---------------------------------------------------------
        System.out.println("\n>>> TEST 12: PIN Verification");

        try {
            Account acc12 = new Account(
                    1012, "Test User", 25, 2000.0, "Savings"
            );

            System.out.println("Has PIN before setting: " + acc12.hasPin());

            acc12.setPin(1234);

            System.out.println("Has PIN after setting: " + acc12.hasPin());
            System.out.println(
                    "Correct PIN (1234): " + acc12.verifyPin(1234)
            );
            System.out.println(
                    "Incorrect PIN (9999): " + acc12.verifyPin(9999)
            );

        } catch (IllegalArgumentException e) {
            System.out.println("PIN/account error: " + e.getMessage());
        }


        // ---------------------------------------------------------
        // COMPLETED
        // ---------------------------------------------------------
        System.out.println("\n" + "=".repeat(60));
        System.out.println("             TESTING COMPLETED");
        System.out.println("=".repeat(60));
    }
}