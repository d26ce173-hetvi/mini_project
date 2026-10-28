import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Part B - Practical 12: Collections Framework (HashMap, ArrayList, Comparator, Comparable, ConcurrentHashMap)
public class Lab12_MiniBank {

    // Account implementing Comparable<Account>
    static class Account implements Comparable<Account> {
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
        public int compareTo(Account other) {
            return this.accountNumber.compareTo(other.accountNumber);
        }

        @Override
        public String toString() {
            return accountNumber + " | " + ownerName + " | Rs. " + balance;
        }
    }

    // 1. AccountStore with HashMap and ArrayList
    static class AccountStore {
        private final Map<String, Account> accounts = new HashMap<>();
        private final List<String> history = new ArrayList<>();

        public void add(Account account) {
            accounts.put(account.getAccountNumber(), account);
            history.add("Added account: " + account.getAccountNumber() + " for " + account.getOwnerName());
        }

        public Account get(String accountNumber) {
            return accounts.get(accountNumber);
        }

        public Collection<Account> getAll() {
            return accounts.values();
        }

        public List<String> getHistory() {
            return history;
        }

        // Returns accounts sorted by balance descending using a Comparator
        public ArrayList<Account> sortedByBalance() {
            ArrayList<Account> list = new ArrayList<>(accounts.values());
            list.sort(Comparator.comparingLong(Account::getBalance).reversed());
            return list;
        }
    }

    // Thread-safe account storage demonstration
    static class ConcurrentAccountRegistry {
        private final ConcurrentHashMap<String, Account> sharedAccounts = new ConcurrentHashMap<>();

        public void register(Account account) {
            sharedAccounts.put(account.getAccountNumber(), account);
        }

        public Account find(String accNum) {
            return sharedAccounts.get(accNum);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 12: Collections Framework (Store, Sorting, Comparable) ===");

        AccountStore store = new AccountStore();

        // Adding accounts
        store.add(new Account("AC0002", "Riya Patel", 5000));
        store.add(new Account("AC0001", "Aarav Shah", 2000));
        store.add(new Account("AC0003", "Diya Sharma", 9000));

        // 1. Lookup by key
        System.out.println("\n--- Lookup Account by Key (AC0002) ---");
        Account found = store.get("AC0002");
        System.out.println("Found: " + found);

        // 2. Sorting by balance using Comparator
        System.out.println("\n--- Accounts Sorted by Balance (Highest to Lowest) ---");
        ArrayList<Account> sortedAccounts = store.sortedByBalance();
        for (Account a : sortedAccounts) {
            System.out.println(" -> " + a);
        }

        // 3. Natural sorting using Comparable (by Account Number)
        System.out.println("\n--- Accounts Sorted Naturally (by Account Number) ---");
        ArrayList<Account> naturalOrderList = new ArrayList<>(store.getAll());
        Collections.sort(naturalOrderList);
        for (Account a : naturalOrderList) {
            System.out.println(" -> " + a);
        }

        // 4. ConcurrentHashMap demonstration
        System.out.println("\n--- ConcurrentHashMap Registration Test ---");
        ConcurrentAccountRegistry registry = new ConcurrentAccountRegistry();
        registry.register(new Account("AC0004", "Vikram Malhotra", 25000));
        System.out.println("Registered in concurrent map: " + registry.find("AC0004"));
    }
}
