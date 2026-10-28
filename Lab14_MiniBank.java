import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

// Part B - Practical 14: Capstone Assembly, BankService, Concurrency, Persistence, and Validation
public class Lab14_MiniBank {

    // --- Annotations ---
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Id {}

    // --- Exceptions ---
    static class BankException extends Exception {
        public BankException(String msg) { super(msg); }
    }
    static class InsufficientFundsException extends BankException {
        private final long shortfall;
        public InsufficientFundsException(String msg, long shortfall) { super(msg); this.shortfall = shortfall; }
        public long getShortfall() { return shortfall; }
    }
    static class AccountNotFoundException extends BankException {
        public AccountNotFoundException(String msg) { super(msg); }
    }
    static class InvalidAmountException extends BankException {
        public InvalidAmountException(String msg) { super(msg); }
    }

    // --- Model ---
    static class Account implements Serializable {
        private static final long serialVersionUID = 1L;

        @Id
        private final String accountNumber;
        private final String ownerName;
        private long balance;

        public Account(String accountNumber, String ownerName, long balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = balance;
        }

        public synchronized void deposit(long amount) throws InvalidAmountException {
            if (amount <= 0) throw new InvalidAmountException("Amount must be positive.");
            this.balance += amount;
        }

        public synchronized void withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
            if (amount <= 0) throw new InvalidAmountException("Amount must be positive.");
            if (amount > this.balance) throw new InsufficientFundsException("Insufficient balance.", amount - this.balance);
            this.balance -= amount;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }
        public synchronized long getBalance() { return balance; }

        @Override
        public String toString() {
            return String.format("%s | %-15s | Rs. %d", accountNumber, ownerName, balance);
        }
    }

    // --- Validator ---
    static class Validator {
        private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
        public static boolean isValidMobile(String mobile) {
            return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
        }
    }

    // --- Generic Repository ---
    static class Repository<T> {
        private final ConcurrentHashMap<String, T> storage = new ConcurrentHashMap<>();

        private String extractId(T entity) {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Object val = field.get(entity);
                        return val != null ? val.toString() : null;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            throw new IllegalArgumentException("No @Id field found.");
        }

        public void save(T entity) {
            storage.put(extractId(entity), entity);
        }

        public T findById(String id) {
            return storage.get(id);
        }

        public List<T> findAll() {
            return new ArrayList<>(storage.values());
        }

        public void clear() {
            storage.clear();
        }
    }

    // --- BankService ---
    static class BankService {
        private final Repository<Account> repository = new Repository<>();
        private final Path dataFile = Paths.get("minibank_accounts.dat");

        public void openAccount(String accNo, String owner, long initialDeposit) {
            Account acc = new Account(accNo, owner, initialDeposit);
            repository.save(acc);
            System.out.println("Account opened successfully: " + acc);
        }

        public void deposit(String accNo, long amount) throws BankException {
            Account acc = repository.findById(accNo);
            if (acc == null) throw new AccountNotFoundException("Account " + accNo + " not found.");
            acc.deposit(amount);
            System.out.println("Deposited Rs. " + amount + " to " + accNo + ". New Balance: Rs. " + acc.getBalance());
        }

        public void withdraw(String accNo, long amount) throws BankException {
            Account acc = repository.findById(accNo);
            if (acc == null) throw new AccountNotFoundException("Account " + accNo + " not found.");
            acc.withdraw(amount);
            System.out.println("Withdrawn Rs. " + amount + " from " + accNo + ". New Balance: Rs. " + acc.getBalance());
        }

        public void transfer(String fromAccNo, String toAccNo, long amount) throws BankException {
            Account from = repository.findById(fromAccNo);
            Account to = repository.findById(toAccNo);
            if (from == null) throw new AccountNotFoundException("Sender account " + fromAccNo + " not found.");
            if (to == null) throw new AccountNotFoundException("Receiver account " + toAccNo + " not found.");

            // Avoid deadlock using consistent lock ordering
            Account first = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
            Account second = (first == from) ? to : from;

            synchronized (first) {
                synchronized (second) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
            System.out.println("Transferred Rs. " + amount + " from " + fromAccNo + " to " + toAccNo);
        }

        public void printStatement(String accNo) throws BankException {
            Account acc = repository.findById(accNo);
            if (acc == null) throw new AccountNotFoundException("Account " + accNo + " not found.");
            System.out.println("----------------------------------------");
            System.out.println("Account Number : " + acc.getAccountNumber());
            System.out.println("Account Holder : " + acc.getOwnerName());
            System.out.println("Current Balance: Rs. " + acc.getBalance());
            System.out.println("----------------------------------------");
        }

        public void listAccounts() {
            List<Account> list = repository.findAll();
            if (list.isEmpty()) {
                System.out.println("No accounts found in system.");
                return;
            }
            System.out.println("\n--- All Accounts in MiniBank ---");
            for (Account acc : list) {
                System.out.println(acc);
            }
        }

        @SuppressWarnings("unchecked")
        public void loadAccounts() {
            if (!Files.exists(dataFile)) return;
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(dataFile))) {
                List<Account> accounts = (List<Account>) ois.readObject();
                for (Account acc : accounts) {
                    repository.save(acc);
                }
                System.out.println("Loaded " + accounts.size() + " accounts from persistence file.");
            } catch (Exception e) {
                System.out.println("Could not load saved accounts: " + e.getMessage());
            }
        }

        public void saveAccounts() {
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(dataFile))) {
                oos.writeObject(repository.findAll());
                System.out.println("Saved " + repository.findAll().size() + " accounts to persistence file.");
            } catch (Exception e) {
                System.out.println("Could not save accounts: " + e.getMessage());
            }
        }

        public Repository<Account> getRepository() {
            return repository;
        }
    }

    // --- Automated Self-Tests (Replicating JUnit assertions) ---
    public static void runAutomatedTests(BankService service) {
        System.out.println("\n[Testing] Running Automated Verification Tests...");
        try {
            // Test 1: Deposit increases balance
            Account t1 = new Account("TEST01", "Test User 1", 1000);
            t1.deposit(500);
            assert t1.getBalance() == 1500 : "Deposit test failed!";
            System.out.println(" [PASS] Test 1: Deposit increases balance correctly.");

            // Test 2: Overdraft throws InsufficientFundsException
            boolean caught = false;
            try {
                t1.withdraw(5000);
            } catch (InsufficientFundsException e) {
                caught = true;
                assert e.getShortfall() == 3500 : "Incorrect shortfall calculation!";
            }
            assert caught : "Withdraw limit did not throw exception!";
            System.out.println(" [PASS] Test 2: Over-withdrawal correctly throws InsufficientFundsException.");

            // Test 3: Concurrency test with thread pool
            Account concurrentAcc = new Account("TEST02", "Concurrent User", 0);
            ExecutorService pool = Executors.newFixedThreadPool(4);
            int tasks = 100;
            for (int i = 0; i < tasks; i++) {
                pool.execute(() -> {
                    try {
                        concurrentAcc.deposit(10);
                    } catch (Exception ignored) {}
                });
            }
            pool.shutdown();
            pool.awaitTermination(3, TimeUnit.SECONDS);
            assert concurrentAcc.getBalance() == 1000 : "Concurrency test failed! Balance: " + concurrentAcc.getBalance();
            System.out.println(" [PASS] Test 3: Concurrent deposits keep exact balance (thread safety verified).");
            System.out.println("[Testing] ALL TESTS PASSED SUCCESSFULLY!\n");
        } catch (Throwable e) {
            System.out.println("[FAIL] Test encountered error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   CHARUSAT MiniBank - Final Capstone (Lab 14)   ");
        System.out.println("=================================================");

        BankService service = new BankService();
        service.loadAccounts();

        // Seed demo accounts if empty
        if (service.getRepository().findAll().isEmpty()) {
            service.openAccount("AC0001", "Riya Patel", 10000);
            service.openAccount("AC0002", "Aarav Shah", 5000);
            service.openAccount("AC0003", "Diya Sharma", 15000);
        }

        // Run automated tests first
        runAutomatedTests(service);

        // Interactive Menu Shell
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n----------------- MAIN MENU -----------------");
            System.out.println("1. Open Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Print Statement");
            System.out.println("6. List All Accounts");
            System.out.println("7. Save & Exit");
            System.out.print("Choose option (1-7): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Please enter a valid number!");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            try {
                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter Account Number (e.g. AC0004): ");
                        String accNo = scanner.nextLine().trim();
                        System.out.print("Enter Owner Name: ");
                        String owner = scanner.nextLine().trim();
                        System.out.print("Enter Opening Balance: ");
                        long bal = scanner.nextLong();
                        service.openAccount(accNo, owner, bal);
                    }
                    case 2 -> {
                        System.out.print("Enter Account Number: ");
                        String accNo = scanner.nextLine().trim();
                        System.out.print("Enter Amount to Deposit: ");
                        long amt = scanner.nextLong();
                        service.deposit(accNo, amt);
                    }
                    case 3 -> {
                        System.out.print("Enter Account Number: ");
                        String accNo = scanner.nextLine().trim();
                        System.out.print("Enter Amount to Withdraw: ");
                        long amt = scanner.nextLong();
                        service.withdraw(accNo, amt);
                    }
                    case 4 -> {
                        System.out.print("Enter Sender Account Number: ");
                        String from = scanner.nextLine().trim();
                        System.out.print("Enter Recipient Account Number: ");
                        String to = scanner.nextLine().trim();
                        System.out.print("Enter Amount to Transfer: ");
                        long amt = scanner.nextLong();
                        service.transfer(from, to, amt);
                    }
                    case 5 -> {
                        System.out.print("Enter Account Number: ");
                        String accNo = scanner.nextLine().trim();
                        service.printStatement(accNo);
                    }
                    case 6 -> service.listAccounts();
                    case 7 -> {
                        service.saveAccounts();
                        System.out.println("Thank you for using MiniBank! Goodbye.");
                        running = false;
                    }
                    default -> System.out.println("Invalid selection. Please choose between 1 and 7.");
                }
            } catch (BankException e) {
                System.out.println("Transaction Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("System Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}
