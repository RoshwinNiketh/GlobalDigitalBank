package com.gdb.tests;

import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.domain.TransactionType;
import com.gdb.logging.MemoryLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.repository.AccountRepository;
import com.gdb.repository.InMemoryAccountRepository;
import com.gdb.repository.InMemoryTransactionRepository;
import com.gdb.repository.RepositoryFactory;
import com.gdb.repository.TransactionRepository;
import com.gdb.service.AccountService;
import java.time.LocalDateTime;
import java.util.List;

/** Acceptance checks for Activity 21's in-memory repository implementations. */
public class TestRepositoryInMemory {
    private TestRepositoryInMemory() {
    }

    public static void main(String[] args) throws Exception {
        printHeader();
        testAccountRepositoryCrud();
        testTransactionRepositoryOperations();
        testServiceIntegration();
        testRepositoryFactory();
        System.out.println("\nALL ACTIVITY 21 IN-MEMORY REPOSITORY TESTS PASSED");
    }

    private static void printHeader() {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 21 — REPOSITORY PATTERN (IN-MEMORY)");
        System.out.println("=".repeat(60));
    }

    private static void testAccountRepositoryCrud() throws Exception {
        System.out.println("\n[TEST 1] Account Repository CRUD:");
        AccountRepository repository = new InMemoryAccountRepository();
        int accountNumber = repository.nextAccountNumber();
        IAccount account = AccountFactory.createAccount(
                "SAVINGS", accountNumber, "Rajesh Sharma", 30, 50000);

        repository.save(account);
        check(repository.exists(accountNumber), "Saved account should exist");
        check(repository.findById(accountNumber) == account, "Account lookup should return saved account");
        System.out.println("  Saved: " + account.getAccountInfo());
        System.out.println("  Found: " + repository.findById(accountNumber).getAccountInfo());

        account.deposit(15000);
        repository.update(account);
        check(repository.findById(accountNumber).getBalance() == 65000,
                "Updated account balance should be available");
        System.out.println("  Updated balance: Rs. " + repository.findById(accountNumber).getBalance());

        IAccount secondAccount = AccountFactory.createAccount(
                "CURRENT", repository.nextAccountNumber(), "Meera Shah", 32, 50000);
        repository.save(secondAccount);
        List<IAccount> snapshot = repository.findAll();
        check(snapshot.size() == 2, "findAll should return both accounts");
        snapshot.clear();
        check(repository.findAll().size() == 2, "findAll should return a defensive copy");
        check(repository.nextAccountNumber() == 1003, "Account numbers should increment from 1001");

        repository.delete(secondAccount.getAccountNumber());
        check(!repository.exists(secondAccount.getAccountNumber()), "Deleted account should not exist");
        System.out.println("  Total accounts in repository: " + repository.findAll().size());
        System.out.println("  -> PASSED");
    }

    private static void testTransactionRepositoryOperations() {
        System.out.println("\n[TEST 2] Transaction Repository Operations:");
        TransactionRepository repository = new InMemoryTransactionRepository();
        Transaction deposit = createTransaction(1001, TransactionType.DEPOSIT, 0, 0);
        Transaction transfer = createTransaction(1001, TransactionType.TRANSFER, 1001, 1002);
        repository.save(deposit);
        repository.save(transfer);

        check(repository.findAll().size() == 2, "Both transactions should be stored");
        check(repository.findByAccount(1001).size() == 2,
                "Account query should include accountNumber and fromAccount matches");
        check(repository.findByAccount(1002).size() == 1,
                "Account query should include toAccount matches");
        check(repository.findByAccount(9999).isEmpty(), "Unknown account should have no transactions");
        System.out.println("  Saved 2 transaction records.");
        System.out.println("  Found transactions for #1001: " + repository.findByAccount(1001).size());

        List<Transaction> snapshot = repository.findAll();
        snapshot.clear();
        check(repository.findAll().size() == 2, "findAll should return a defensive copy");
        repository.clear();
        check(repository.findAll().isEmpty(), "clear should remove every transaction");
        System.out.println("  -> PASSED");
    }

    private static void testServiceIntegration() throws Exception {
        System.out.println("\n[TEST 3] Service Integration with Repositories:");
        AccountRepository accountRepository = new InMemoryAccountRepository();
        TransactionRepository transactionRepository = new InMemoryTransactionRepository();
        TransactionLogger logger = new TransactionLogger(new MemoryLogDestination());
        AccountService service = new AccountService(accountRepository, transactionRepository, logger);

        IAccount sender = service.openAccount("SAVINGS", "John Doe", 25, 50000);
        IAccount recipient = service.openAccount("SAVINGS", "Jane Smith", 30, 50000);
        sender.setPin(1234);
        recipient.setPin(5678);
        check(accountRepository.findById(sender.getAccountNumber()) == sender,
                "AccountService should save newly opened accounts through the repository");

        service.deposit(sender.getAccountNumber(), 5000);
        service.withdraw(sender.getAccountNumber(), 2000, 1234);
        service.transfer(sender.getAccountNumber(), recipient.getAccountNumber(), 1000, 1234);
        check(sender.getBalance() == 52000, "Sender balance should include all service operations");
        check(recipient.getBalance() == 51000, "Recipient balance should include transfer");
        check(transactionRepository.findAll().size() == 3,
                "Each successful service operation should be stored in transaction repository");
        check(transactionRepository.findByAccount(recipient.getAccountNumber()).size() == 1,
                "Transfer should be queryable from the recipient account");
        check(logger.readAll().size() == 3, "Each successful service operation should be logged");

        service.closeAccount(recipient.getAccountNumber(), 5678);
        check(!service.getAccount(recipient.getAccountNumber()).isActive(),
                "Account closure should update the repository-backed account");
        System.out.println("  Opened account #" + sender.getAccountNumber() + " via AccountService.");
        System.out.println("  Deposit, withdrawal, transfer, and account closure persisted.");
        System.out.println("  -> PASSED");
    }

    private static void testRepositoryFactory() {
        System.out.println("\n[TEST 4] Repository Factory Configuration:");
        check("memory".equals(RepositoryFactory.getPersistenceMode()),
                "Default persistence mode should be memory");
        check(RepositoryFactory.getAccountRepository() instanceof InMemoryAccountRepository,
                "Factory should provide an in-memory account repository");
        check(RepositoryFactory.getTransactionRepository() instanceof InMemoryTransactionRepository,
                "Factory should provide an in-memory transaction repository");
        check(RepositoryFactory.getAccountRepository() == RepositoryFactory.getAccountRepository(),
                "Factory should return a singleton account repository");
        System.out.println("  Mode: " + RepositoryFactory.getPersistenceMode());
        System.out.println("  In-memory repositories created and cached.");
        System.out.println("  -> PASSED");
    }

    private static Transaction createTransaction(int accountNumber, TransactionType type,
                                                 int fromAccount, int toAccount) {
        return new Transaction(Transaction.generateId(), LocalDateTime.now(), accountNumber,
                type, 100, 1000, "SUCCESS", "Repository test transaction",
                fromAccount, toAccount);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
