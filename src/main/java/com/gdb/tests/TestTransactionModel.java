package com.gdb.tests;

import com.gdb.domain.*;
import com.gdb.service.TransferService;
import com.gdb.exceptions.*;

public class TestTransactionModel {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 16 — TRANSACTION MODEL TEST");
        System.out.println("=".repeat(60));

        TransferService svc = new TransferService();

        Account acc1 = (Account) AccountFactory.createAccount(
                "SAVINGS", 1001, "Rajesh Sharma", 30, 50000.0);

        acc1.setPin(1234);

        Transaction depositTransaction =
                acc1.depositWithTransaction(5000.0);

        System.out.println(
                "[STEP 10] Deposit Transaction: "
                        + depositTransaction);

        Transaction withdrawalTransaction =
                acc1.withdrawWithTransaction(2000.0, 1234);

        System.out.println(
                "[STEP 11] Withdrawal Transaction: "
                        + withdrawalTransaction);

        Account acc2 = (Account) AccountFactory.createAccount(
                "SAVINGS", 1002, "Priya Patel", 28, 20000.0);

        Transaction transferTransaction =
                svc.transferWithTransaction(
                        acc1, acc2, 1000.0, 1234);

        System.out.println(
                "[STEP 12] Transfer Transaction: "
                        + transferTransaction);

        acc1.deposit(1000.0);

        System.out.println(
                "[STEP 13] Legacy Deposit +1000: "
                        + acc1.getAccountInfo());
    }
}
