package com.gdb.tests;

import com.gdb.command.DepositCommand;
import com.gdb.command.TransactionCommand;
import com.gdb.command.TransferCommand;
import com.gdb.command.WithdrawCommand;
import com.gdb.db.SimulatedDatabase;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.logging.DatabaseLogDestination;
import com.gdb.logging.FileLogDestination;
import com.gdb.logging.LogDestination;
import com.gdb.logging.MemoryLogDestination;
import com.gdb.logging.TransactionLogger;
import java.util.List;

/** Activity 18 driver for switching between independent logging backends. */
public class TestBridgeLogging {
    private static final int EXPECTED_COMMANDS = 3;

    public static void main(String[] args) throws Exception {
        printHeader();

        IAccount[] accounts = createAccounts();
        IAccount source = accounts[0];
        IAccount destination = accounts[1];

        FileLogDestination fileDestination = new FileLogDestination();
        fileDestination.clear();
        DatabaseLogDestination databaseDestination =
                new DatabaseLogDestination(new SimulatedDatabase());
        MemoryLogDestination memoryDestination = new MemoryLogDestination();

        TransactionLogger logger = new TransactionLogger(fileDestination);
        logThreeCommands(logger, source, destination);
        printCount(logger, "STEP 25");

        logger.setDestination(databaseDestination);
        logThreeCommands(logger, source, destination);
        printCount(logger, "STEP 26");

        logger.setDestination(memoryDestination);
        logThreeCommands(logger, source, destination);
        printCount(logger, "STEP 27");

        verifyIsolation(logger, fileDestination, databaseDestination, memoryDestination);
        System.out.println("\n[STEP 29] Destinations: "
                + fileDestination.getDestinationName() + ", "
                + databaseDestination.getDestinationName() + ", "
                + memoryDestination.getDestinationName());
        System.out.println("[STEP 29] All Bridge Pattern log backends verified successfully!");
    }

    private static void printHeader() {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 18 — BRIDGE PATTERN (FILE + DB)");
        System.out.println("=".repeat(60));
    }

    private static IAccount[] createAccounts() throws Exception {
        IAccount source = AccountFactory.createAccount(
                "SAVINGS", 1001, "John", 25, 15000.0);
        source.setPin(1234);
        IAccount destination = AccountFactory.createAccount(
                "SAVINGS", 1002, "Jane", 30, 10000.0);
        return new IAccount[] { source, destination };
    }

    private static void logThreeCommands(TransactionLogger logger,
                                         IAccount source,
                                         IAccount destination) throws Exception {
        TransactionCommand deposit = new DepositCommand(source, 5000.0);
        deposit.execute();
        logger.log(deposit);

        TransactionCommand withdrawal = new WithdrawCommand(source, 2000.0, 1234);
        withdrawal.execute();
        logger.log(withdrawal);

        TransactionCommand transfer = new TransferCommand(source, destination, 3000.0, 1234);
        transfer.execute();
        logger.log(transfer);
    }

    private static void printCount(TransactionLogger logger, String step) {
        System.out.println("\n[" + step + "] Logging to "
                + logger.getDestinationName() + " destination...");
        System.out.println("  " + logger.getDestinationName()
                + " log count: " + logger.readAll().size());
    }

    private static void verifyIsolation(TransactionLogger logger,
                                        LogDestination fileDestination,
                                        LogDestination databaseDestination,
                                        LogDestination memoryDestination) {
        System.out.println("\n[STEP 28] Verifying Data Isolation:");
        verifyCount(logger, fileDestination);
        verifyCount(logger, databaseDestination);
        verifyCount(logger, memoryDestination);
    }

    private static void verifyCount(TransactionLogger logger,
                                    LogDestination destination) {
        logger.setDestination(destination);
        List<TransactionCommand> commands = logger.readAll();
        if (commands.size() != EXPECTED_COMMANDS) {
            throw new AssertionError(destination.getDestinationName()
                    + " expected " + EXPECTED_COMMANDS
                    + " commands, found " + commands.size());
        }
        System.out.println("  " + destination.getDestinationName()
                + " count: " + commands.size() + " [EXPECTED: "
                + EXPECTED_COMMANDS + "]");
    }
}
