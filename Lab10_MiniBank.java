import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Part B - Practical 10: Thread Pools, Producer-Consumer with wait/notify, and Deadlock Prevention
public class Lab10_MiniBank {

    // Simple Account for demonstration
    static class Account {
        private final String accountNumber;
        private long balance;

        public Account(String accountNumber, long balance) {
            this.accountNumber = accountNumber;
            this.balance = balance;
        }

        public synchronized void deposit(long amount) {
            balance += amount;
        }

        public synchronized void withdraw(long amount) {
            balance -= amount;
        }

        public synchronized long getBalance() { return balance; }
        public String getAccountNumber() { return accountNumber; }
    }

    // 1. TransactionProcessor with ExecutorService thread pool
    static class TransactionProcessor {
        private final ExecutorService executor = Executors.newFixedThreadPool(4);

        public void submit(Runnable task) {
            executor.execute(task);
        }

        public void stop() {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    // 2. Producer-Consumer buffer using wait() and notify()
    static class TransactionQueue {
        private final Queue<String> buffer = new LinkedList<>();
        private final int capacity = 3;

        public synchronized void produce(String tx) throws InterruptedException {
            while (buffer.size() == capacity) {
                wait(); // wait until buffer has space
            }
            buffer.add(tx);
            System.out.println("[Producer] Added to queue: " + tx);
            notifyAll(); // signal consumer
        }

        public synchronized String consume() throws InterruptedException {
            while (buffer.isEmpty()) {
                wait(); // wait until item is produced
            }
            String tx = buffer.poll();
            System.out.println("[Consumer] Processed from queue: " + tx);
            notifyAll(); // signal producer
            return tx;
        }
    }

    // 3. Deadlock demonstration and fix
    public static void safeTransfer(Account from, Account to, long amount) {
        // Fix: Always lock smaller accountNumber first to guarantee consistent lock ordering
        Account firstLock = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Account secondLock = firstLock == from ? to : from;

        synchronized (firstLock) {
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            synchronized (secondLock) {
                from.withdraw(amount);
                to.deposit(amount);
                System.out.printf("[SafeTransfer] Transferred %d from %s to %s%n",
                        amount, from.getAccountNumber(), to.getAccountNumber());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Lab 10: Thread Pools, wait/notify, and Deadlock Prevention ===");

        // Part 1: Thread Pool Transaction Processing
        System.out.println("\n--- Step 1: Processing Tasks with Thread Pool ---");
        Account testAcc = new Account("AC0001", 1000);
        TransactionProcessor processor = new TransactionProcessor();

        for (int i = 1; i <= 8; i++) {
            final int txId = i;
            processor.submit(() -> {
                testAcc.deposit(100);
                System.out.println("Pool Task #" + txId + " finished by thread: " + Thread.currentThread().getName());
            });
        }
        processor.stop();
        System.out.println("Final balance after thread pool jobs: Rs. " + testAcc.getBalance());

        // Part 2: Producer - Consumer with wait() and notify()
        System.out.println("\n--- Step 2: Producer-Consumer Coordination ---");
        TransactionQueue queue = new TransactionQueue();
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    queue.produce("TXN-" + i);
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {}
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    queue.consume();
                    Thread.sleep(80);
                }
            } catch (InterruptedException ignored) {}
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        // Part 3: Deadlock Prevention with Consistent Lock Ordering
        System.out.println("\n--- Step 3: Deadlock Prevention via Consistent Lock Ordering ---");
        Account accA = new Account("AC0001", 5000);
        Account accB = new Account("AC0002", 5000);

        Thread t1 = new Thread(() -> safeTransfer(accA, accB, 1000), "TransferThread-1");
        Thread t2 = new Thread(() -> safeTransfer(accB, accA, 500), "TransferThread-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Both concurrent opposite transfers finished with NO DEADLOCK!");
        System.out.println("accA Balance: Rs. " + accA.getBalance() + " | accB Balance: Rs. " + accB.getBalance());
    }
}
