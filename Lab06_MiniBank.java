import static java.lang.Math.max;

// Part B - Practical 6: Interfaces, Functional Interfaces, Lambdas, and Marker Interfaces
public class Lab06_MiniBank {

    // 1. Transactable Interface
    interface Transactable {
        void deposit(long amount);
        boolean withdraw(long amount);
    }

    // 2. InterestBearing Interface with default method
    interface InterestBearing {
        double interestRate();
        long getBalance();

        default double yearlyInterest() {
            return getBalance() * (interestRate() / 100.0);
        }
    }

    // 3. Functional Interface WithdrawRule
    @FunctionalInterface
    interface WithdrawRule {
        boolean allow(Account account, long amount);
    }

    // 4. Marker Interface Premium
    interface Premium {}

    // Account implementing Transactable and InterestBearing
    static class Account implements Transactable, InterestBearing {
        private final String accountNumber;
        private final String ownerName;
        private long balance;

        public Account(String accountNumber, String ownerName, long balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = max(0, balance); // using static import of Math.max
        }

        @Override
        public void deposit(long amount) {
            if (amount > 0) balance += amount;
        }

        @Override
        public boolean withdraw(long amount) {
            if (amount > 0 && balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        }

        @Override
        public double interestRate() {
            return 4.5;
        }

        @Override
        public long getBalance() {
            return balance;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }
    }

    // Subclass marking an account as Premium
    static class PremiumAccount extends Account implements Premium {
        public PremiumAccount(String accountNumber, String ownerName, long balance) {
            super(accountNumber, ownerName, balance);
        }

        @Override
        public double interestRate() {
            return 6.0; // special rate for premium
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 06: Interfaces, Lambdas, and Marker Interfaces ===");

        Account regular = new Account("AC101", "Riya Patel", 10000);
        PremiumAccount vip = new PremiumAccount("AC102", "Vikram Malhotra", 50000);

        System.out.println("Regular Account Yearly Interest: Rs. " + regular.yearlyInterest());
        System.out.println("VIP Account Yearly Interest: Rs. " + vip.yearlyInterest());

        // Marker interface check
        if (vip instanceof Premium) {
            System.out.println("Account " + vip.getAccountNumber() + " is marked as a PREMIUM account!");
        }

        System.out.println("\n--- Testing Functional Interface: WithdrawRule ---");
        long withdrawAttempt = 15000;

        // 1. Anonymous class implementation (limit withdraw to 20,000 max)
        WithdrawRule dailyLimitRule = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return amount <= 20000 && account.getBalance() >= amount;
            }
        };

        // 2. Lambda expression implementation (VIP rule: minimum balance after withdraw must be >= 5000)
        WithdrawRule vipMinBalanceRule = (account, amount) -> (account.getBalance() - amount) >= 5000;

        System.out.println("Anonymous class check (daily limit 20k for VIP): " + dailyLimitRule.allow(vip, withdrawAttempt));
        System.out.println("Lambda expression check (VIP min balance 5k): " + vipMinBalanceRule.allow(vip, withdrawAttempt));
    }
}
