import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Part B - Practical 13: Generic Repository<T>, Reflection on @Id, Varargs, and Bounded Types
public class Lab13_MiniBank {

    // 1. @Id Annotation
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Id {}

    // 2. Generic Repository Class
    static class Repository<T> {
        private final Map<String, T> storage = new HashMap<>();

        // Extracts ID from field marked with @Id using reflection
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
                            throw new RuntimeException("Cannot access @Id field", e);
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
            throw new IllegalArgumentException("No field annotated with @Id found in " + entity.getClass().getSimpleName());
        }

        public T save(T entity) {
            String id = extractId(entity);
            if (id == null) {
                throw new IllegalArgumentException("Entity ID value cannot be null");
            }
            storage.put(id, entity);
            return entity;
        }

        @SafeVarargs
        public final void saveAll(T... items) {
            for (T item : items) {
                save(item);
            }
        }

        public T findById(String id) {
            return storage.get(id);
        }

        public ArrayList<T> findAll() {
            return new ArrayList<>(storage.values());
        }

        public boolean delete(String id) {
            return storage.remove(id) != null;
        }

        public int count() {
            return storage.size();
        }

        // Bounded Generic Method: Works on any subtype of Number
        public static <N extends Number> double average(N[] values) {
            if (values == null || values.length == 0) return 0.0;
            double sum = 0;
            for (N val : values) {
                sum += val.doubleValue();
            }
            return sum / values.length;
        }
    }

    // Model 1: Account
    static class Account {
        @Id
        private final String accountNumber;
        private final String ownerName;
        private final long balance;

        public Account(String accountNumber, String ownerName, long balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = balance;
        }

        @Override
        public String toString() {
            return "Account[" + accountNumber + ", " + ownerName + ", Rs. " + balance + "]";
        }
    }

    // Model 2: Customer (Demonstrates reusability of Repository<T>)
    static class Customer {
        @Id
        private final String customerId;
        private final String name;

        public Customer(String customerId, String name) {
            this.customerId = customerId;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Customer[" + customerId + ", " + name + "]";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 13: Generic Repository with Reflection ===");

        // Repository for Account
        System.out.println("\n--- Testing Repository<Account> ---");
        Repository<Account> accountRepo = new Repository<>();
        Account a1 = new Account("AC0001", "Riya Patel", 15000);
        Account a2 = new Account("AC0002", "Aarav Shah", 8000);
        Account a3 = new Account("AC0003", "Diya Sharma", 22000);

        accountRepo.saveAll(a1, a2, a3);
        System.out.println("Total Accounts Saved: " + accountRepo.count());

        Account fetched = accountRepo.findById("AC0002");
        System.out.println("Fetched by id (AC0002): " + fetched);

        System.out.println("All Accounts in Repository:");
        for (Account a : accountRepo.findAll()) {
            System.out.println(" -> " + a);
        }

        // Reusing Repository for Customer
        System.out.println("\n--- Testing Reusability with Repository<Customer> ---");
        Repository<Customer> customerRepo = new Repository<>();
        customerRepo.save(new Customer("CUST101", "Riya Patel"));
        customerRepo.save(new Customer("CUST102", "Aarav Shah"));

        System.out.println("Fetched Customer CUST101: " + customerRepo.findById("CUST101"));

        // Testing Bounded Type Generic Method
        System.out.println("\n--- Testing Bounded Generic Method: average() ---");
        Integer[] intScores = { 10, 20, 30, 40, 50 };
        Double[] balances = { 15000.50, 8000.75, 22000.00 };

        System.out.println("Average of Integer array: " + Repository.average(intScores));
        System.out.println("Average of Double array : " + Repository.average(balances));
    }
}
