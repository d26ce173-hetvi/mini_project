import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

// Part B - Practical 11: Persistence (Serialization) and NIO File Operations / Report Generation
public class Lab11_MiniBank {

    // 1. Serializable Account Model
    static class Account implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String accountNumber;
        private final String ownerName;
        private long balance;

        public Account(String accountNumber, String ownerName, long balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = balance;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }
        public long getBalance() { return balance; }

        @Override
        public String toString() {
            return accountNumber + " | " + ownerName + " | Rs. " + balance;
        }
    }

    // 2. StatePersister using ObjectOutputStream and ObjectInputStream
    static class StatePersister {
        public static void save(Account[] accounts, Path file) throws IOException {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(accounts);
            }
        }

        public static Account[] load(Path file) throws IOException, ClassNotFoundException {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                return (Account[]) ois.readObject();
            }
        }
    }

    // 3. TransactionLog using Files.write with CREATE and APPEND
    static class TransactionLog {
        public static void append(Path file, String line) throws IOException {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }

    // 4. ReportGenerator using Files.newDirectoryStream and BufferedReader
    static class ReportGenerator {
        public static void generateReport(Path logsDir, Path reportFile) throws IOException {
            long totalDeposits = 0;
            long totalWithdrawals = 0;
            int totalTransactions = 0;

            if (!Files.exists(logsDir)) {
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsDir, "*.log")) {
                for (Path entry : stream) {
                    BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                    System.out.println("Processing log: " + entry.getFileName() + " (" + attrs.size() + " bytes)");

                    try (BufferedReader reader = Files.newBufferedReader(entry)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) continue;
                            String[] parts = line.split("\\s+");
                            if (parts.length >= 3) {
                                String type = parts[0].toUpperCase();
                                long amount = Long.parseLong(parts[2]);
                                if (type.equals("DEPOSIT")) {
                                    totalDeposits += amount;
                                    totalTransactions++;
                                } else if (type.equals("WITHDRAW")) {
                                    totalWithdrawals += amount;
                                    totalTransactions++;
                                }
                            }
                        }
                    }
                }
            }

            long netChange = totalDeposits - totalWithdrawals;
            String reportContent = "========================================\n"
                    + "      MINIBANK END-OF-DAY REPORT        \n"
                    + "========================================\n"
                    + "Total Transactions Processed: " + totalTransactions + "\n"
                    + "Total Deposits              : Rs. " + totalDeposits + "\n"
                    + "Total Withdrawals           : Rs. " + totalWithdrawals + "\n"
                    + "Net Cash Flow               : Rs. " + netChange + "\n"
                    + "========================================\n";

            Files.writeString(reportFile, reportContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 11: Persistence and NIO Report Generation ===");

        Path dataDir = Paths.get("data");
        Path accountsFile = dataDir.resolve("accounts.dat");
        Path logsDir = dataDir.resolve("logs");
        Path dailyLog = logsDir.resolve("daily_transactions.log");
        Path reportFile = dataDir.resolve("summary_report.txt");

        try {
            // 1. Serialization save & load test
            System.out.println("\n--- Step 1: Saving and Reloading Account State ---");
            Account[] originalAccounts = new Account[] {
                new Account("AC0001", "Riya Patel", 8500),
                new Account("AC0002", "Aarav Shah", 12000),
                new Account("AC0003", "Diya Sharma", 4000)
            };

            StatePersister.save(originalAccounts, accountsFile);
            System.out.println("Saved 3 accounts to " + accountsFile);

            Account[] loadedAccounts = StatePersister.load(accountsFile);
            System.out.println("Reloaded accounts from file:");
            for (Account a : loadedAccounts) {
                System.out.println(" -> " + a);
            }

            // 2. TransactionLog append
            System.out.println("\n--- Step 2: Appending to Transaction Log ---");
            Files.deleteIfExists(dailyLog); // clean start
            TransactionLog.append(dailyLog, "DEPOSIT AC0001 500");
            TransactionLog.append(dailyLog, "WITHDRAW AC0002 200");
            TransactionLog.append(dailyLog, "DEPOSIT AC0003 1500");
            TransactionLog.append(dailyLog, "WITHDRAW AC0001 300");
            System.out.println("Logged 4 transactions to " + dailyLog);

            // 3. Report generation
            System.out.println("\n--- Step 3: Generating End-of-Day Report ---");
            ReportGenerator.generateReport(logsDir, reportFile);

            System.out.println("\nGenerated Report Content:\n");
            System.out.println(Files.readString(reportFile));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
