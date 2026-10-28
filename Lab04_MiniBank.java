import java.util.regex.Pattern;

// Part B - Practical 4: Regex Validation, Command Parsing, and StringBuilder
public class Lab04_MiniBank {

    static class Account {
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
    }

    // 1. Validator with compiled regular expressions
    static class Validator {
        private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
        private static final Pattern EMAIL_PATTERN  = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        private static final Pattern PAN_PATTERN    = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
        private static final Pattern IFSC_PATTERN   = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");

        public static boolean isValidMobile(String mobile) {
            return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
        }

        public static boolean isValidEmail(String email) {
            return email != null && EMAIL_PATTERN.matcher(email).matches();
        }

        public static boolean isValidPan(String pan) {
            return pan != null && PAN_PATTERN.matcher(pan).matches();
        }

        public static boolean isValidIfsc(String ifsc) {
            return ifsc != null && IFSC_PATTERN.matcher(ifsc).matches();
        }
    }

    // 2. TransactionType enum & Command record
    enum TransactionType {
        DEPOSIT,
        WITHDRAW,
        TRANSFER
    }

    record Command(TransactionType type, String accountNumber, long amount) {}

    // 3. CommandParser
    static class CommandParser {
        public static Command parse(String line) {
            if (line == null || line.trim().isEmpty()) {
                throw new IllegalArgumentException("Command cannot be empty");
            }
            String[] parts = line.trim().split("\\s+");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Expected 3 arguments: <TYPE> <ACCOUNT_NO> <AMOUNT>");
            }
            TransactionType type = TransactionType.valueOf(parts[0].toUpperCase());
            String accountNumber = parts[1];
            long amount = Long.parseLong(parts[2]);
            return new Command(type, accountNumber, amount);
        }
    }

    // 4. StatementFormatter using StringBuilder
    static class StatementFormatter {
        public static String buildStatement(Account account) {
            StringBuilder sb = new StringBuilder();
            sb.append("----------------------------------------\n");
            sb.append("           MINIBANK STATEMENT           \n");
            sb.append("----------------------------------------\n");
            sb.append("Account Number : ").append(account.getAccountNumber()).append("\n");
            sb.append("Account Holder : ").append(account.getOwnerName()).append("\n");
            sb.append("Current Balance: Rs. ").append(account.getBalance()).append("\n");
            sb.append("Status         : Active\n");
            sb.append("----------------------------------------");
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 04: Validation, Command Parsing, and Statement ===");

        // Testing validators
        System.out.println("\n--- Testing Validators ---");
        System.out.println("isValidMobile(\"9876543210\"): " + Validator.isValidMobile("9876543210")); // true
        System.out.println("isValidMobile(\"12345\"): " + Validator.isValidMobile("12345"));           // false
        System.out.println("isValidEmail(\"abc@xyz.com\"): " + Validator.isValidEmail("abc@xyz.com"));   // true
        System.out.println("isValidEmail(\"invalid-email\"): " + Validator.isValidEmail("invalid-email"));// false
        System.out.println("isValidPan(\"ABCDE1234F\"): " + Validator.isValidPan("ABCDE1234F"));         // true
        System.out.println("isValidIfsc(\"SBIN0001234\"): " + Validator.isValidIfsc("SBIN0001234"));     // true

        // Testing CommandParser
        System.out.println("\n--- Testing CommandParser ---");
        String inputLine = "DEPOSIT AC0001 500";
        Command cmd = CommandParser.parse(inputLine);
        System.out.println("Raw input: " + inputLine);
        System.out.println("Parsed Command: type=" + cmd.type() + ", accountNumber=" + cmd.accountNumber() + ", amount=" + cmd.amount());

        // Testing StatementFormatter
        System.out.println("\n--- Testing StatementFormatter ---");
        Account acc = new Account("AC0001", "Riya Patel", 7500);
        System.out.println(StatementFormatter.buildStatement(acc));
    }
}
