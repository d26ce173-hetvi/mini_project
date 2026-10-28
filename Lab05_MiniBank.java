// Part B - Practical 5: Abstract Classes, Inheritance, and Polymorphism
public class Lab05_MiniBank {

    // 1. Abstract Account Base Class
    static abstract class Account {
        private final String accountNumber;
        private final String ownerName;
        protected long balance;

        public Account(String accountNumber, String ownerName, long balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = balance;
        }

        public abstract double interestRate();
        public abstract boolean canWithdraw(long amount);

        public void deposit(long amount) {
            if (amount > 0) balance += amount;
        }

        public boolean withdraw(long amount) {
            if (amount > 0 && canWithdraw(amount)) {
                balance -= amount;
                return true;
            }
            return false;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }
        public long getBalance() { return balance; }

        @Override
        public String toString() {
            return "[" + getClass().getSimpleName() + "] " + accountNumber + " | " + ownerName + " | Balance: " + balance;
        }
    }

    // 2. SavingsAccount Subclass
    static class SavingsAccount extends Account {
        private final long minBalance;

        public SavingsAccount(String accountNumber, String ownerName, long balance, long minBalance) {
            super(accountNumber, ownerName, balance);
            this.minBalance = minBalance;
        }

        @Override
        public double interestRate() {
            return 4.0;
        }

        @Override
        public boolean canWithdraw(long amount) {
            return (balance - amount) >= minBalance;
        }

        public long getMinBalance() { return minBalance; }
    }

    // 3. CurrentAccount Subclass
    static class CurrentAccount extends Account {
        private final long overdraftLimit;

        public CurrentAccount(String accountNumber, String ownerName, long balance, long overdraftLimit) {
            super(accountNumber, ownerName, balance);
            this.overdraftLimit = overdraftLimit;
        }

        @Override
        public double interestRate() {
            return 0.0;
        }

        @Override
        public boolean canWithdraw(long amount) {
            return (balance - amount) >= -overdraftLimit;
        }

        public long getOverdraftLimit() { return overdraftLimit; }
    }

    // 4. FixedDepositAccount Subclass
    static class FixedDepositAccount extends Account {
        public FixedDepositAccount(String accountNumber, String ownerName, long balance) {
            super(accountNumber, ownerName, balance);
        }

        @Override
        public double interestRate() {
            return 7.0;
        }

        @Override
        public boolean canWithdraw(long amount) {
            return false; // locked deposit
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 05: Abstract Classes and Polymorphism ===");

        Account[] accounts = new Account[] {
            new SavingsAccount("SA001", "Riya Patel", 10000, 1000),
            new CurrentAccount("CA001", "Apex Tech Ltd", 20000, 5000),
            new FixedDepositAccount("FD001", "Karan Dave", 50000)
        };

        System.out.println("\n--- Demonstrating Run-time Polymorphism ---");
        for (Account acc : accounts) {
            System.out.printf("%s -> Interest Rate: %.1f%%%n", acc.toString(), acc.interestRate());

            // Pattern matching instanceof
            if (acc instanceof CurrentAccount ca) {
                System.out.println("   [Special Info] Overdraft Limit permitted: Rs. " + ca.getOverdraftLimit());
            } else if (acc instanceof SavingsAccount sa) {
                System.out.println("   [Special Info] Minimum Balance required: Rs. " + sa.getMinBalance());
            } else if (acc instanceof FixedDepositAccount) {
                System.out.println("   [Special Info] Fixed Deposit is locked for withdrawal.");
            }
        }
    }
}
