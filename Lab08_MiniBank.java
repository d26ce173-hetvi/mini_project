// Part B - Practical 8: Exception Handling Hierarchy, try-catch-finally, and try-with-resources
public class Lab08_MiniBank {

    // 1. Exception Hierarchy
    static class BankException extends Exception {
        public BankException(String message) {
            super(message);
        }
    }

    static class InsufficientFundsException extends BankException {
        private final long shortfall;

        public InsufficientFundsException(String message, long shortfall) {
            super(message);
            this.shortfall = shortfall;
        }

        public long getShortfall() {
            return shortfall;
        }
    }

    static class AccountNotFoundException extends BankException {
        public AccountNotFoundException(String message) {
            super(message);
        }
    }

    static class InvalidAmountException extends BankException {
        public InvalidAmountException(String message) {
            super(message);
        }
    }

    // AutoCloseable resource for try-with-resources
    static class BankSession implements AutoCloseable {
        private final String sessionId;

        public BankSession(String sessionId) {
            this.sessionId = sessionId;
            System.out.println("[SESSION OPEN] Started bank audit session: " + sessionId);
        }

        @Override
        public void close() {
            System.out.println("[SESSION CLOSE] Bank audit session closed cleanly: " + sessionId);
        }
    }

    // Account with Exception Handling
    static class Account {
        private final String accountNumber;
        private final String ownerName;
        private long balance;

        public Account(String accountNumber, String ownerName, long balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = balance;
        }

        public void deposit(long amount) throws InvalidAmountException {
            if (amount <= 0) {
                throw new InvalidAmountException("Deposit amount must be positive: " + amount);
            }
            balance += amount;
        }

        public void withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
            if (amount <= 0) {
                throw new InvalidAmountException("Withdrawal amount must be positive: " + amount);
            }
            if (amount > balance) {
                long shortfall = amount - balance;
                throw new InsufficientFundsException("Insufficient funds! Cannot withdraw " + amount, shortfall);
            }
            balance -= amount;
        }

        public void transfer(Account to, long amount) throws BankException {
            if (to == null) {
                throw new AccountNotFoundException("Recipient account cannot be null");
            }
            System.out.println("Initiating transfer of Rs. " + amount + " to " + to.getAccountNumber());
            try {
                this.withdraw(amount);
                to.deposit(amount);
                System.out.println("Transfer successful!");
            } catch (BankException e) {
                System.out.println("[Transfer Log] Transfer failed: " + e.getMessage());
                throw e; // re-throw
            } finally {
                System.out.println("[Audit] Transfer operation completed its cycle.");
            }
        }

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }
        public long getBalance() { return balance; }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 08: Exception Handling in MiniBank ===");

        Account a1 = new Account("AC0001", "Riya Patel", 1000);
        Account a2 = new Account("AC0002", "Aarav Shah", 500);

        // Demonstrating try-with-resources
        try (BankSession session = new BankSession("SES-9982")) {

            // 1. Valid deposit
            a1.deposit(500);
            System.out.println("Deposited 500 into " + a1.getAccountNumber() + ". Balance: " + a1.getBalance());

            // 2. Insufficient Funds Exception
            System.out.println("\nAttempting to withdraw 5000 from balance of 1500...");
            try {
                a1.withdraw(5000);
            } catch (InsufficientFundsException e) {
                System.out.println("Caught Expected Error: " + e.getMessage() + " | Shortfall amount: Rs. " + e.getShortfall());
            } catch (InvalidAmountException e) {
                System.out.println("Caught Invalid Amount: " + e.getMessage());
            }

            // 3. Invalid Amount Exception
            System.out.println("\nAttempting negative deposit...");
            try {
                a1.deposit(-200);
            } catch (InvalidAmountException e) {
                System.out.println("Caught Expected Error: " + e.getMessage());
            }

            // 4. Transfer with try-catch-finally
            System.out.println("\nAttempting transfer of 200 from a1 to a2...");
            try {
                a1.transfer(a2, 200);
            } catch (BankException e) {
                System.out.println("Transfer error: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Unexpected top-level error: " + e.getMessage());
        } finally {
            System.out.println("\nFinal Balance of AC0001: Rs. " + a1.getBalance());
            System.out.println("Final Balance of AC0002: Rs. " + a2.getBalance());
        }
    }
}
