import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Part B - Practical 7: Custom Annotations and Reflection-based Validator
public class Lab07_MiniBank {

    // 1. Custom Annotations
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

    // Account Class with Annotations
    static class Account {
        @Id
        @MaxLength(10)
        private final String accountNumber;

        @MaxLength(20)
        private final String ownerName;

        @Positive(message = "balance must be greater than 0")
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
            return accountNumber + " | " + ownerName + " | " + balance;
        }
    }

    // 2. Reflection-based AnnotationValidator
    static class AnnotationValidator {
        public static String[] validate(Object obj) {
            List<String> errors = new ArrayList<>();
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object val = field.get(obj);

                    // Check @Positive rule
                    if (field.isAnnotationPresent(Positive.class)) {
                        Positive annotation = field.getAnnotation(Positive.class);
                        if (val instanceof Number num && num.longValue() <= 0) {
                            errors.add("Field '" + field.getName() + "': " + annotation.message() + " (current value: " + val + ")");
                        }
                    }

                    // Check @MaxLength rule
                    if (field.isAnnotationPresent(MaxLength.class)) {
                        MaxLength annotation = field.getAnnotation(MaxLength.class);
                        if (val instanceof String str && str.length() > annotation.value()) {
                            errors.add("Field '" + field.getName() + "': length exceeds max limit of " + annotation.value());
                        }
                    }
                } catch (IllegalAccessException e) {
                    errors.add("Unable to access field: " + field.getName());
                }
            }
            return errors.toArray(new String[0]);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 07: Custom Annotations and Reflection Validator ===");

        // Create an invalid account (negative balance and overly long owner name)
        Account invalidAccount = new Account("AC0001", "ThisNameIsFarTooLongForOurFieldRule", -100);
        System.out.println("Validating account: " + invalidAccount);

        String[] errors = AnnotationValidator.validate(invalidAccount);
        System.out.println("Validation Results (Found " + errors.length + " errors):");
        for (String err : errors) {
            System.out.println(" -> [ERROR] " + err);
        }

        System.out.println("\nValidating a valid account:");
        Account validAccount = new Account("AC0002", "Riya Patel", 5000);
        String[] validErrors = AnnotationValidator.validate(validAccount);
        if (validErrors.length == 0) {
            System.out.println(" -> [SUCCESS] All annotation constraints passed!");
        }
    }
}
