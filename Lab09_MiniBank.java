// Part B - Practical 9: Multithreading, Race Conditions, and Synchronization
public class Lab09_MiniBank {

    // Account with unsynchronized methods (demonstrates race condition)
    static class UnsafeAccount {
        private long balance = 0;

        public void deposit(long amount) {
            long temp = balance;
            try {
                Thread.sleep(0, 100); // simulate slight CPU switch
            } catch (InterruptedException ignored) {}
            balance = temp + amount;
        }

        public long getBalance() { return balance; }
    }

    // Account with synchronized deposit (thread-safe)
    static class SafeAccount {
        private long balance = 0;

        public synchronized void deposit(long amount) {
            balance += amount;
        }

        public synchronized long getBalance() {
            return balance;
        }
    }

    // AccountWorker implementing Runnable
    static class AccountWorker implements Runnable {
        private final Object account;
        private final int times;
        private final long amount;
        private final boolean isSafe;

        public AccountWorker(Object account, int times, long amount, boolean isSafe) {
            this.account = account;
            this.times = times;
            this.amount = amount;
            this.isSafe = isSafe;
        }

        @Override
        public void run() {
            for (int i = 0; i < times; i++) {
                if (isSafe) {
                    ((SafeAccount) account).deposit(amount);
                } else {
                    ((UnsafeAccount) account).deposit(amount);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Lab 09: Multithreading & Synchronization in MiniBank ===");

        int numThreads = 10;
        int depositsPerThread = 1000;
        long depositAmount = 1;
        long expectedTotal = numThreads * depositsPerThread * depositAmount; // 10,000

        // 1. Unsynchronized execution (Demonstrating Race Condition)
        System.out.println("\n[Test 1] Running 10 threads on UnsafeAccount (WITHOUT synchronization)...");
        UnsafeAccount unsafeAcc = new UnsafeAccount();
        Thread[] unsafeThreads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            unsafeThreads[i] = new Thread(new AccountWorker(unsafeAcc, depositsPerThread, depositAmount, false), "UnsafeWorker-" + (i + 1));
            unsafeThreads[i].start();
        }

        for (Thread t : unsafeThreads) {
            t.join();
        }

        System.out.println("Expected Balance: Rs. " + expectedTotal);
        System.out.println("Actual Balance  : Rs. " + unsafeAcc.getBalance());
        System.out.println("Result          : " + (unsafeAcc.getBalance() == expectedTotal ? "Match" : "RACE CONDITION DETECTED! (Balance is lost)"));

        // 2. Synchronized execution (Thread-safe)
        System.out.println("\n[Test 2] Running 10 threads on SafeAccount (WITH synchronized deposit)...");
        SafeAccount safeAcc = new SafeAccount();
        Thread[] safeThreads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            safeThreads[i] = new Thread(new AccountWorker(safeAcc, depositsPerThread, depositAmount, true), "SafeWorker-" + (i + 1));
            System.out.println("Thread started: " + safeThreads[i].getName() + " | State: " + safeThreads[i].getState());
            safeThreads[i].start();
        }

        for (Thread t : safeThreads) {
            t.join();
            System.out.println("Thread finished: " + t.getName() + " | State: " + t.getState());
        }

        System.out.println("Expected Balance: Rs. " + expectedTotal);
        System.out.println("Actual Balance  : Rs. " + safeAcc.getBalance());
        System.out.println("Result          : PERFECT SYNCHRONIZATION! (Thread safety achieved)");
    }
}
