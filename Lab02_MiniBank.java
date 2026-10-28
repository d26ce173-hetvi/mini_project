// Part B - Practical 2: Customer and Account Classes with Encapsulation
public class Lab02_MiniBank {

    static class Customer {
        private String name;
        private String email;
        private String mobile;
        private final String customerId;

        private static long customerCounter = 100;

        private static String generateCustomerId() {
            return "CUST" + (++customerCounter);
        }

        public Customer(String name, String email, String mobile) {
            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.customerId = generateCustomerId();
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getMobile() { return mobile; }
        public String getCustomerId() { return customerId; }
    }

    static class Account {
        private final String accountNumber;
        private String ownerName;
        private long balance;
        private boolean active;

        private static long accountCounter = 0;

        private static String generateAccountNumber() {
            return String.format("AC%04d", ++accountCounter);
        }

        public Account(String ownerName, long balance) {
            this.accountNumber = generateAccountNumber();
            this.ownerName = ownerName;
            this.balance = Math.max(0, balance);
            this.active = true;
        }

        public Account(String ownerName) {
            this(ownerName, 0);
        }

        public void deposit(long amount) {
            if (amount > 0) {
                balance += amount;
            }
        }

        public boolean withdraw(long amount) {
            if (amount > 0 && balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }
        public long getBalance() { return balance; }
        public boolean isActive() { return active; }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 02: Customer and Account Classes ===");

        Customer c1 = new Customer("Riya Patel", "riya@example.com", "9876543210");
        System.out.println("Created Customer: " + c1.getCustomerId() + " - " + c1.getName());

        Account[] accounts = new Account[3];
        accounts[0] = new Account("Riya Patel", 5000);
        accounts[1] = new Account("Aarav Shah", 2000);
        accounts[2] = new Account("Diya Sharma"); // balance 0

        System.out.println("\n--- Performing Transactions on " + accounts[0].getAccountNumber() + " (Owner: " + accounts[0].getOwnerName() + ") ---");
        System.out.println("Initial Balance: " + accounts[0].getBalance());

        accounts[0].deposit(2000);
        System.out.println("After deposit(2000): " + accounts[0].getBalance()); // 7000

        boolean w1 = accounts[0].withdraw(3000);
        System.out.println("withdraw(3000) success: " + w1 + " | Balance: " + accounts[0].getBalance()); // 4000

        boolean w2 = accounts[0].withdraw(10000);
        System.out.println("withdraw(10000) success: " + w2 + " | Balance: " + accounts[0].getBalance()); // 4000

        System.out.println("\n--- Summary of All Accounts ---");
        for (Account acc : accounts) {
            System.out.println(acc.getAccountNumber() + " | Owner: " + acc.getOwnerName() + " | Balance: " + acc.getBalance() + " | Active: " + acc.isActive());
        }
    }
}
