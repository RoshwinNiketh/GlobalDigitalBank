package com.gdb;

import com.gdb.logging.FileLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;
import com.gdb.ui.AccountUI;

/** Application entry point for the interactive banking console. */
public class Main {
    public static void main(String[] args) {
        printStartupMessage();
        launchAccountUI();
    }

    private static void printStartupMessage() {
        System.out.println("Booting Global Digital Bank...");
    }

    private static void launchAccountUI() {
        AccountUI accountUI = createAccountUI();
        accountUI.start();
    }

    private static AccountUI createAccountUI() {
        AccountService service = new AccountService(
                new TransactionLogger(new FileLogDestination()));
        return new AccountUI(service);
    }
}
