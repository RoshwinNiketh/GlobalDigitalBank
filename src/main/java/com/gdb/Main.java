package com.gdb;

import com.gdb.logging.FileLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;
import com.gdb.ui.AccountUI;

/** Application entry point for the interactive banking console. */
public class Main {
    public static void main(String[] args) {
        System.out.println("Booting Global Digital Bank...");
        AccountService service = new AccountService(
                new TransactionLogger(new FileLogDestination()));
        new AccountUI(service).start();
    }
}
