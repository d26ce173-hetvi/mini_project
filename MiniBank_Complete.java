import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/*
 * ============================================================================
 *   CHARUSAT - FACULTY OF TECHNOLOGY AND ENGINEERING (FTE)
 *   CEUE203: OBJECT-ORIENTED PROGRAMMING (OOPJ)
 *   Part B Complete Capstone Project: MiniBank (Merged Practicals 1 to 14)
 * ============================================================================
 */
public class MiniBank_Complete {

    // ------------------------------------------------------------------------
    // Practical 1: Records and Enums
    // ------------------------------------------------------------------------
    public record BankInfo(String name, String branch, String ifsc) {}

    public enum MenuOption {
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        VIEW_STATEMENT,
        LIST_ACCOUNTS,
        SORTED_ACCOUNTS,
        RUN_BATCH_TRANSACTIONS,
        GENERATE_AUDIT_REPORT,
        RUN_UNIT_TESTS,
        EXIT
    }

    // ------------------------------------------------------------------------
    // Practical 7 & 13: Custom Annotations for Metadata & Reflection
    // ------------------------------------------------------------------------
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Id {}

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Positive {
        String message() default "must be > 0";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MaxLength {
        int value();
    }

    // ------------------------------------------------------------------------
    // Practical 8: Custom Exception Hierarchy
    // ------------------------------------------------------------------------
    public static class BankException extends Exception {
        public BankException(String message) { super(message); }
    }

    public static class InsufficientFundsException extends BankException {
        private final long shortfall;
        public InsufficientFundsException(String message, long shortfall) {
            super(message);
            this.shortfall = shortfall;
        }
        public long getShortfall() { return shortfall; }
    }

    public static class AccountNotFoundException extends BankException {
        public AccountNotFoundException(String message) { super(message); }
    }

    public static class InvalidAmountException extends BankException {
        public InvalidAmountException(String message) { super(message); }
    }

    // ------------------------------------------------------------------------
    // Practical 6: Interfaces (Standard, Functional, and Marker)
    // ------------------------------------------------------------------------
    public interface Transactable {
        void deposit(long amount) throws BankException;
        boolean withdraw(long amount) throws BankException;
    }

    public interface InterestBearing {
        double interestRate();
        long getBalance();

        default double yearlyInterest() {
            return getBalance() * (interestRate() / 100.0);
        }
    }

    @FunctionalInterface
    public interface WithdrawRule {
        boolean allow(Account account, long amount);
    }

    public interface Premium {} // Marker interface

    // ------------------------------------------------------------------------
    // Practical 2 & 3: Customer and Static Nested Address
    // ------------------------------------------------------------------------
    public static class Customer implements Serializable, Cloneable {
        private static final long serialVersionUID = 1L;

        @Id
        private final String customerId;
        private String name;
        private String email;
        private String mobile;
        private Address address;

        private static long customerCounter = 100;

        public static class Address implements Serializable {
            private static final long serialVersionUID = 1L;
            private final String line;
            private final String city;
            private final String pincode;

            public Address(String line, String city, String pincode) {
                this.line = line;
                this.city = city;
                this.pincode = pincode;
            }

            @Override
            public String toString() {
                return line + ", " + city + " (" + pincode + ")";
            }
        }

        public Customer(String name, String email, String mobile, Address address) {
            this.customerId = "CUST" + (++customerCounter);
            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.address = address;
        }

        public String getCustomerId() { return customerId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getMobile() { return mobile; }
        public Address getAddress() { return address; }

        @Override
        public Customer clone() {
            try {
                Customer cloned = (Customer) super.clone();
                if (this.address != null) {
                    cloned.address = new Address(this.address.line, this.address.city, this.address.pincode);
                }
                return cloned;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

        @Override
        public String toString() {
            return "[" + customerId + "] " + name + " | Mobile: " + mobile + " | Addr: " + address;
        }
    }

    // ------------------------------------------------------------------------
    // Practical 2, 3, 5, 9, 11, 12: Abstract Account Model & Subclasses
    // ------------------------------------------------------------------------
    public static abstract class Account implements Transactable, InterestBearing, Comparable<Account>, Serializable {
        private static final long serialVersionUID = 1L;

        @Id
        @MaxLength(10)
        protected final String accountNumber;

        @MaxLength(30)
        protected final String ownerName;

        @Positive(message = "Balance cannot be negative")
        protected long balance;

        public Account(String accountNumber, String ownerName, long balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = Math.max(0, balance);
        }

        public abstract boolean canWithdraw(long amount);

        @Override
        public synchronized void deposit(long amount) throws InvalidAmountException {
            if (amount <= 0) {
                throw new InvalidAmountException("Deposit amount must be strictly positive: " + amount);
            }
            this.balance += amount;
        }

        @Override
        public synchronized boolean withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
            if (amount <= 0) {
                throw new InvalidAmountException("Withdrawal amount must be strictly positive: " + amount);
            }
            if (!canWithdraw(amount)) {
                long shortfall = amount - this.balance;
                throw new InsufficientFundsException("Insufficient funds to withdraw Rs. " + amount, shortfall);
            }
            this.balance -= amount;
            return true;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }

        @Override
        public synchronized long getBalance() { return balance; }

        @Override
        public int compareTo(Account other) {
            return this.accountNumber.compareTo(other.accountNumber);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Account account = (Account) o;
            return Objects.equals(accountNumber, account.accountNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountNumber);
        }

        @Override
        public String toString() {
            return String.format("%-10s | %-16s | Rs. %-8d | Type: %-15s",
                    accountNumber, ownerName, balance, getClass().getSimpleName());
        }
    }

    public static class SavingsAccount extends Account {
        private static final long serialVersionUID = 1L;
        private final long minBalance;

        public SavingsAccount(String accountNumber, String ownerName, long balance, long minBalance) {
            super(accountNumber, ownerName, balance);
            this.minBalance = minBalance;
        }

        @Override
        public double interestRate() { return 4.0; }

        @Override
        public synchronized boolean canWithdraw(long amount) {
            return (balance - amount) >= minBalance;
        }

        public long getMinBalance() { return minBalance; }
    }

    public static class CurrentAccount extends Account {
        private static final long serialVersionUID = 1L;
        private final long overdraftLimit;

        public CurrentAccount(String accountNumber, String ownerName, long balance, long overdraftLimit) {
            super(accountNumber, ownerName, balance);
            this.overdraftLimit = overdraftLimit;
        }

        @Override
        public double interestRate() { return 0.0; }

        @Override
        public synchronized boolean canWithdraw(long amount) {
            return (balance - amount) >= -overdraftLimit;
        }

        public long getOverdraftLimit() { return overdraftLimit; }
    }

    public static class FixedDepositAccount extends Account {
        private static final long serialVersionUID = 1L;

        public FixedDepositAccount(String accountNumber, String ownerName, long balance) {
            super(accountNumber, ownerName, balance);
        }

        @Override
        public double interestRate() { return 7.0; }

        @Override
        public boolean canWithdraw(long amount) {
            return false; // locked deposit
        }
    }

    // ------------------------------------------------------------------------
    // Practical 4: Validator (Regex) and Statement Formatter
    // ------------------------------------------------------------------------
    public static class Validator {
        private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
        private static final Pattern EMAIL_PATTERN  = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        private static final Pattern PAN_PATTERN    = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
        private static final Pattern IFSC_PATTERN   = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");

        public static boolean isValidMobile(String m) { return m != null && MOBILE_PATTERN.matcher(m).matches(); }
        public static boolean isValidEmail(String e) { return e != null && EMAIL_PATTERN.matcher(e).matches(); }
        public static boolean isValidPan(String p) { return p != null && PAN_PATTERN.matcher(p).matches(); }
        public static boolean isValidIfsc(String i) { return i != null && IFSC_PATTERN.matcher(i).matches(); }
    }

    public static class StatementFormatter {
        public static String buildStatement(Account account) {
            StringBuilder sb = new StringBuilder();
            sb.append("--------------------------------------------------\n");
            sb.append("             MINIBANK ACCOUNT STATEMENT            \n");
            sb.append("--------------------------------------------------\n");
            sb.append("Account Number : ").append(account.getAccountNumber()).append("\n");
            sb.append("Account Holder : ").append(account.getOwnerName()).append("\n");
            sb.append("Account Type   : ").append(account.getClass().getSimpleName()).append("\n");
            sb.append("Interest Rate  : ").append(account.interestRate()).append("% p.a.\n");
            sb.append("Yearly Interest: Rs. ").append(account.yearlyInterest()).append("\n");
            sb.append("Current Balance: Rs. ").append(account.getBalance()).append("\n");
            sb.append("--------------------------------------------------");
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------------
    // Practical 7: Reflection-based Annotation Validator
    // ------------------------------------------------------------------------
    public static class AnnotationValidator {
        public static List<String> validate(Object obj) {
            List<String> errors = new ArrayList<>();
            Class<?> clazz = obj.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        Object val = field.get(obj);
                        if (field.isAnnotationPresent(Positive.class)) {
                            Positive p = field.getAnnotation(Positive.class);
                            if (val instanceof Number n && n.longValue() < 0) {
                                errors.add("Field '" + field.getName() + "': " + p.message());
                            }
                        }
                        if (field.isAnnotationPresent(MaxLength.class)) {
                            MaxLength m = field.getAnnotation(MaxLength.class);
                            if (val instanceof String s && s.length() > m.value()) {
                                errors.add("Field '" + field.getName() + "': exceeds max length " + m.value());
                            }
                        }
                    } catch (IllegalAccessException ignored) {}
                }
                clazz = clazz.getSuperclass();
            }
            return errors;
        }
    }

    // ------------------------------------------------------------------------
    // Practical 13: Generic Repository<T> with Reflection on @Id
    // ------------------------------------------------------------------------
    public static class Repository<T> {
        private final Map<String, T> storage = new ConcurrentHashMap<>();

        private String extractId(T entity) {
            Class<?> clazz = entity.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
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
                clazz = clazz.getSuperclass();
            }
            throw new IllegalArgumentException("No @Id field found on " + entity.getClass().getSimpleName());
        }

        public T save(T entity) {
            storage.put(extractId(entity), entity);
            return entity;
        }

        @SafeVarargs
        public final void saveAll(T... items) {
            for (T item : items) save(item);
        }

        public T findById(String id) {
            return storage.get(id);
        }

        public List<T> findAll() {
            return new ArrayList<>(storage.values());
        }

        public boolean delete(String id) {
            return storage.remove(id) != null;
        }

        public static <N extends Number> double average(N[] values) {
            if (values == null || values.length == 0) return 0.0;
            double sum = 0;
            for (N v : values) sum += v.doubleValue();
            return sum / values.length;
        }
    }

    // ------------------------------------------------------------------------
    // Practical 10: TransactionProcessor (ExecutorService & Deadlock Prevention)
    // ------------------------------------------------------------------------
    public static class TransactionProcessor {
        private final ExecutorService pool = Executors.newFixedThreadPool(4);

        public void submit(Runnable task) {
            pool.execute(task);
        }

        public void stop() {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Practical 11: StatePersister & NIO Report Generator
    // ------------------------------------------------------------------------
    public static class PersistenceManager {
        public static void saveAccounts(List<Account> accounts, Path file) throws IOException {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(new ArrayList<>(accounts));
            }
        }

        @SuppressWarnings("unchecked")
        public static List<Account> loadAccounts(Path file) throws IOException, ClassNotFoundException {
            if (!Files.exists(file)) return new ArrayList<>();
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                return (List<Account>) ois.readObject();
            }
        }

        public static void logTransaction(Path logFile, String line) {
            try {
                if (logFile.getParent() != null) Files.createDirectories(logFile.getParent());
                Files.writeString(logFile, line + System.lineSeparator(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println("[Log Warning] Could not write audit log: " + e.getMessage());
            }
        }

        public static String generateAuditReport(Path logsDir) throws IOException {
            if (!Files.exists(logsDir)) return "No audit logs found.";

            long deposits = 0, withdrawals = 0, transfers = 0;
            int total = 0;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsDir, "*.log")) {
                for (Path p : stream) {
                    try (BufferedReader reader = Files.newBufferedReader(p)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.trim().split("\\s+");
                            if (parts.length >= 3) {
                                String type = parts[0].toUpperCase();
                                long amt = Long.parseLong(parts[parts.length - 1]);
                                if (type.contains("DEPOSIT")) deposits += amt;
                                else if (type.contains("WITHDRAW")) withdrawals += amt;
                                else if (type.contains("TRANSFER")) transfers += amt;
                                total++;
                            }
                        }
                    }
                }
            }

            return "==================================================\n"
                 + "               AUDIT & LOG SUMMARY                \n"
                 + "==================================================\n"
                 + "Total Logged Operations : " + total + "\n"
                 + "Total Deposited Amount  : Rs. " + deposits + "\n"
                 + "Total Withdrawn Amount  : Rs. " + withdrawals + "\n"
                 + "Total Transferred Amount: Rs. " + transfers + "\n"
                 + "Net Cash Flow (Dep-With): Rs. " + (deposits - withdrawals) + "\n"
                 + "==================================================\n";
        }
    }

    // ------------------------------------------------------------------------
    // Practical 14: Central Layered BankService
    // ------------------------------------------------------------------------
    public static class BankService {
        private final Repository<Account> repository = new Repository<>();
        private final Path dataPath = Paths.get("data", "minibank_accounts.dat");
        private final Path auditLog = Paths.get("data", "logs", "transactions.log");

        public BankService() {
            loadState();
        }

        public void loadState() {
            try {
                List<Account> loaded = PersistenceManager.loadAccounts(dataPath);
                for (Account a : loaded) repository.save(a);
            } catch (Exception ignored) {}
        }

        public void saveState() {
            try {
                PersistenceManager.saveAccounts(repository.findAll(), dataPath);
            } catch (Exception e) {
                System.out.println("Error saving state: " + e.getMessage());
            }
        }

        public void openAccount(Account account) {
            List<String> errors = AnnotationValidator.validate(account);
            if (!errors.isEmpty()) {
                System.out.println("[Validation Failed] " + errors.get(0));
                return;
            }
            repository.save(account);
            PersistenceManager.logTransaction(auditLog, "OPEN " + account.getAccountNumber() + " " + account.getBalance());
            System.out.println("Successfully opened: " + account);
        }

        public void deposit(String accNo, long amount) throws BankException {
            Account acc = repository.findById(accNo);
            if (acc == null) throw new AccountNotFoundException("Account " + accNo + " does not exist.");
            acc.deposit(amount);
            PersistenceManager.logTransaction(auditLog, "DEPOSIT " + accNo + " " + amount);
            System.out.println("Deposited Rs. " + amount + " | Current Balance: Rs. " + acc.getBalance());
        }

        public void withdraw(String accNo, long amount) throws BankException {
            Account acc = repository.findById(accNo);
            if (acc == null) throw new AccountNotFoundException("Account " + accNo + " does not exist.");
            acc.withdraw(amount);
            PersistenceManager.logTransaction(auditLog, "WITHDRAW " + accNo + " " + amount);
            System.out.println("Withdrawn Rs. " + amount + " | Current Balance: Rs. " + acc.getBalance());
        }

        public void transfer(String fromAccNo, String toAccNo, long amount) throws BankException {
            if (fromAccNo.equals(toAccNo)) {
                throw new BankException("Sender and recipient cannot be the same account.");
            }
            Account from = repository.findById(fromAccNo);
            Account to = repository.findById(toAccNo);
            if (from == null) throw new AccountNotFoundException("Sender account " + fromAccNo + " not found.");
            if (to == null) throw new AccountNotFoundException("Recipient account " + toAccNo + " not found.");

            // Consistent Lock Ordering to eliminate deadlock
            Account first = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
            Account second = (first == from) ? to : from;

            synchronized (first) {
                synchronized (second) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
            PersistenceManager.logTransaction(auditLog, "TRANSFER " + fromAccNo + " -> " + toAccNo + " " + amount);
            System.out.println("Successfully transferred Rs. " + amount + " from " + fromAccNo + " to " + toAccNo);
        }

        public void printStatement(String accNo) throws BankException {
            Account acc = repository.findById(accNo);
            if (acc == null) throw new AccountNotFoundException("Account " + accNo + " not found.");
            System.out.println(StatementFormatter.buildStatement(acc));
        }

        public void listAccounts() {
            List<Account> accounts = repository.findAll();
            if (accounts.isEmpty()) {
                System.out.println("No accounts registered yet.");
                return;
            }
            System.out.println("\n--- All Registered Accounts ---");
            for (Account a : accounts) {
                System.out.println(" -> " + a);
            }
        }

        public void listSortedByBalance() {
            List<Account> accounts = repository.findAll();
            accounts.sort(Comparator.comparingLong(Account::getBalance).reversed());
            System.out.println("\n--- Accounts Sorted by Balance (Highest First) ---");
            for (Account a : accounts) {
                System.out.println(" -> " + a);
            }
        }

        public void runBatchDemo() {
            System.out.println("\n[Batch] Starting multithreaded batch processing with 4 worker threads...");
            TransactionProcessor processor = new TransactionProcessor();
            Account demoAcc = repository.findById("AC0001");
            if (demoAcc == null) {
                demoAcc = new SavingsAccount("AC0001", "Demo User", 1000, 500);
                repository.save(demoAcc);
            }

            final Account target = demoAcc;
            for (int i = 1; i <= 10; i++) {
                final int id = i;
                processor.submit(() -> {
                    try {
                        target.deposit(100);
                        PersistenceManager.logTransaction(auditLog, "DEPOSIT " + target.getAccountNumber() + " 100");
                        System.out.println(" Batch task #" + id + " completed by " + Thread.currentThread().getName());
                    } catch (Exception ignored) {}
                });
            }
            processor.stop();
            System.out.println("[Batch] Complete! New balance of " + target.getAccountNumber() + ": Rs. " + target.getBalance());
        }

        public void printAuditReport() {
            try {
                System.out.println(PersistenceManager.generateAuditReport(Paths.get("data", "logs")));
            } catch (IOException e) {
                System.out.println("Could not generate report: " + e.getMessage());
            }
        }

        public Repository<Account> getRepository() { return repository; }
    }

    // ------------------------------------------------------------------------
    // Built-in Unit Verification Suite (Milestone 3 & Practical 14 Requirement)
    // ------------------------------------------------------------------------
    public static void runSelfTests() {
        System.out.println("\n==========================================");
        System.out.println("        RUNNING OOPJ LAB TEST SUITE        ");
        System.out.println("==========================================");

        int passed = 0;
        int total = 4;

        // Test 1: Savings minimum balance rule
        SavingsAccount sa = new SavingsAccount("TST01", "Tester", 2000, 500);
        try {
            sa.deposit(1000);
            if (sa.getBalance() == 3000) passed++;
            System.out.println("[PASS] Test 1: Deposit calculation verified (3000).");
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1 failed: " + e.getMessage());
        }

        // Test 2: Overdraft limit enforcement
        try {
            sa.withdraw(2600); // 3000 - 2600 = 400 < minBalance 500
            System.out.println("[FAIL] Test 2: Withdrawal should have failed!");
        } catch (InsufficientFundsException e) {
            passed++;
            System.out.println("[PASS] Test 2: InsufficientFundsException correctly raised (shortfall: " + e.getShortfall() + ").");
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: Unexpected exception: " + e);
        }

        // Test 3: Customer nested class & deep cloning
        Customer.Address addr = new Customer.Address("College Road", "Changa", "388421");
        Customer c1 = new Customer("Karan", "karan@example.com", "9876543210", addr);
        Customer c2 = c1.clone();
        if (c1.getCustomerId().equals(c2.getCustomerId()) && c1.getAddress() != c2.getAddress()) {
            passed++;
            System.out.println("[PASS] Test 3: Customer deep clone integrity verified.");
        } else {
            System.out.println("[FAIL] Test 3: Clone validation failed.");
        }

        // Test 4: Regex Validator
        if (Validator.isValidMobile("9876543210") && !Validator.isValidMobile("123")) {
            passed++;
            System.out.println("[PASS] Test 4: Mobile regex pattern verified.");
        } else {
            System.out.println("[FAIL] Test 4: Regex validation failed.");
        }

        System.out.println("------------------------------------------");
        System.out.printf("Test Suite Results: %d / %d Tests Passed%n", passed, total);
        System.out.println("==========================================\n");
    }

    // ------------------------------------------------------------------------
    // Main CLI Application Loop
    // ------------------------------------------------------------------------
    public static void main(String[] args) {
        BankInfo bankInfo = new BankInfo("Charusat MiniBank", "FTE Campus Branch", "CHAR0002026");
        System.out.println("============================================================");
        System.out.println("       " + bankInfo.name() + " - " + bankInfo.branch());
        System.out.println("              IFSC: " + bankInfo.ifsc());
        System.out.println("============================================================");

        BankService service = new BankService();

        // Seed initial accounts if empty
        if (service.getRepository().findAll().isEmpty()) {
            service.openAccount(new SavingsAccount("AC0001", "Riya Patel", 10000, 1000));
            service.openAccount(new CurrentAccount("AC0002", "Apex Tech Sol", 25000, 10000));
            service.openAccount(new FixedDepositAccount("FD0001", "Karan Dave", 50000));
        }

        Scanner scanner = new Scanner(System.in);
        boolean active = true;

        while (active) {
            System.out.println("\n============= INTERACTIVE MENU =============");
            System.out.println(" 1. Open Account");
            System.out.println(" 2. Deposit");
            System.out.println(" 3. Withdraw");
            System.out.println(" 4. Transfer Funds");
            System.out.println(" 5. View Account Statement");
            System.out.println(" 6. List All Accounts");
            System.out.println(" 7. View Accounts Sorted by Balance");
            System.out.println(" 8. Run Thread-Pool Batch Simulation");
            System.out.println(" 9. View Audit & End-of-Day Report");
            System.out.println("10. Run Built-in Unit Test Suite");
            System.out.println("11. Save State & Exit");
            System.out.print("Select an option (1-11): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number!");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // clear buffer

            try {
                switch (choice) {
                    case 1 -> {
                        System.out.println("\nSelect Account Type: 1. Savings  2. Current  3. Fixed Deposit");
                        int type = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Enter Account Number (e.g. AC0003): ");
                        String accNo = scanner.nextLine().trim();
                        System.out.print("Enter Owner Name: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Enter Opening Balance: ");
                        long bal = scanner.nextLong();

                        if (type == 1) {
                            service.openAccount(new SavingsAccount(accNo, name, bal, 1000));
                        } else if (type == 2) {
                            service.openAccount(new CurrentAccount(accNo, name, bal, 5000));
                        } else {
                            service.openAccount(new FixedDepositAccount(accNo, name, bal));
                        }
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
                    case 7 -> service.listSortedByBalance();
                    case 8 -> service.runBatchDemo();
                    case 9 -> service.printAuditReport();
                    case 10 -> runSelfTests();
                    case 11 -> {
                        service.saveState();
                        System.out.println("\n[Success] State saved to disk.");
                        System.out.println("Thank you for using Charusat MiniBank. Goodbye!");
                        active = false;
                    }
                    default -> System.out.println("Choice must be between 1 and 11.");
                }
            } catch (BankException e) {
                System.out.println("\n[Banking Exception] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[System Exception] " + e.getMessage());
            }
        }
        scanner.close();
    }
}
