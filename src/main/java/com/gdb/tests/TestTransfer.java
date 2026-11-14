package com.gdb.tests;

import com.gdb.domain.*;
import com.gdb.service.TransferService;
import com.gdb.exceptions.*;

public class TestTransfer {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 15 — TRANSFER WITH DAILY LIMITS");
        System.out.println("=".repeat(60));

        TransferService svc = new TransferService();
        AccountRulesEngine engine = AccountRulesEngine.getInstance();

        Account acc1 = (Account) AccountFactory.createAccount(
                "SAVINGS", 1001, "Rajesh Sharma", 30, 100000.0);

        Account acc2 = (Account) AccountFactory.createAccount(
                "SAVINGS", 1002, "Priya Patel", 28, 20000.0);

        acc1.setPin(1234);

        System.out.println("[STEP 9] " + acc1.getAccountInfo());
        System.out.println("[STEP 9] " + acc2.getAccountInfo());

        try {
            svc.transfer(acc1, acc2, 5000.0, 1234);

            System.out.println(
                    "[STEP 10] Transfer Rs. 5,000: SUCCESS | acc1 = Rs. "
                            + acc1.getBalance()
                            + " | acc2 = Rs. "
                            + acc2.getBalance());
        } catch (AccountException e) {
            System.out.println("[STEP 10] Transfer failed: " + e.getMessage());
        }

        try {
            svc.transfer(acc1, acc2, 100000.0, 1234);
        } catch (InsufficientBalanceException e) {
            System.out.println(
                    "[STEP 11] Caught InsufficientBalanceException: "
                            + e.getMessage());
        }

        System.out.println(
                "[STEP 12] Daily limit for acc1: Rs. "
                        + acc1.getDailyTransferLimit());

        int transferNumber = 1;

        try {
            while (true) {
                svc.transfer(acc1, acc2, 20000.0, 1234);

                System.out.println(
                        "  Transfer #" + transferNumber
                                + " of Rs. 20,000: SUCCESS | used today = Rs. "
                                + acc1.getDailyTransferTotal());

                transferNumber++;
            }
        } catch (AccountException e) {
            System.out.println(
                    "[STEP 12] Caught "
                            + e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage());
        }

        System.out.println(
                "[STEP 13] Used today: Rs. "
                        + acc1.getDailyTransferTotal()
                        + " | Remaining: Rs. "
                        + acc1.getRemainingDailyTransferLimit());
    }
}
