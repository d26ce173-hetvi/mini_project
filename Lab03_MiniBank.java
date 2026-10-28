import java.util.Objects;

// Part B - Practical 3: Object Methods (toString, equals, hashCode, clone) and Nested Classes
public class Lab03_MiniBank {

    static class Customer implements Cloneable {
        private String name;
        private String email;
        private String mobile;
        private final String customerId;
        private Address address;

        private static long customerCounter = 100;

        private static String generateCustomerId() {
            return "CUST" + (++customerCounter);
        }

        // Static nested class Address
        public static class Address {
            private String line;
            private String city;
            private String pincode;

            public Address(String line, String city, String pincode) {
                this.line = line;
                this.city = city;
                this.pincode = pincode;
            }

            public String getLine() { return line; }
            public String getCity() { return city; }
            public String getPincode() { return pincode; }

            @Override
            public String toString() {
                return line + ", " + city + " - " + pincode;
            }
        }

        public Customer(String name, String email, String mobile, Address address) {
            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.customerId = generateCustomerId();
            this.address = address;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getMobile() { return mobile; }
        public String getCustomerId() { return customerId; }
        public Address getAddress() { return address; }

        @Override
        public Customer clone() {
            try {
                Customer cloned = (Customer) super.clone();
                if (this.address != null) {
                    cloned.address = new Address(this.address.line, this.address.city, this.address.pincode);
                }
                return cloned;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
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

        // Constructor to explicitly set accountNumber (used to test equals)
        public Account(String accountNumber, String ownerName, long balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = Math.max(0, balance);
            this.active = true;
        }

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }
        public long getBalance() { return balance; }

        @Override
        public String toString() {
            return accountNumber + " | " + ownerName + " | " + balance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Account account = (Account) o;
            return Objects.equals(accountNumber, account.accountNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountNumber);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 03: Object Methods and Nested Address Class ===");

        // Customer with nested Address and clone
        Customer.Address addr = new Customer.Address("12 Green Park", "Anand", "388001");
        Customer c1 = new Customer("Riya Patel", "riya@example.com", "9876543210", addr);
        Customer c2 = c1.clone();

        System.out.println("Customer 1: " + c1.getCustomerId() + " | Address: " + c1.getAddress());
        System.out.println("Customer 2 (Cloned): " + c2.getCustomerId() + " | Address: " + c2.getAddress());
        System.out.println("Are addresses distinct instances? " + (c1.getAddress() != c2.getAddress()));

        // Account toString and equals test
        Account a1 = new Account("AC0001", "Riya Patel", 4000);
        Account a2 = new Account("AC0001", "Riya Duplicate", 5000);
        Account a3 = new Account("AC0002", "Aarav Shah", 3000);

        System.out.println("\nPrinting a1 with toString(): " + a1);
        System.out.println("a1.equals(a2) (matching accountNumber AC0001): " + a1.equals(a2));
        System.out.println("a1.equals(a3) (different accountNumber): " + a1.equals(a3));

        // Testing instanceof
        Object testObj = a1;
        if (testObj instanceof Account acc) {
            System.out.println("instanceof check passed! Confirmed Account owner: " + acc.getOwnerName());
        }
    }
}
