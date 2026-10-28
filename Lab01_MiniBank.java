import java.util.Scanner;

// Part B - Practical 1: Menu Shell, Enum, Record, and Switch Expression
public class Lab01_MiniBank {

    // Record for bank details
    record BankInfo(String name, String branch) {}

    // Enum for menu actions
    enum MenuOption {
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        EXIT
    }

    public static void main(String[] args) {
        BankInfo info = new BankInfo("MiniBank India", "Charusat Branch");
        System.out.println("========================================");
        System.out.println("  Welcome to " + info.name() + " (" + info.branch() + ")");
        System.out.println("========================================");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Open Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Exit");
            System.out.print("Enter choice (1-5): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input! Please enter a number between 1 and 5.");
                scanner.next(); // consume invalid token
                continue;
            }

            int choice = scanner.nextInt();
            MenuOption option = switch (choice) {
                case 1 -> MenuOption.OPEN_ACCOUNT;
                case 2 -> MenuOption.DEPOSIT;
                case 3 -> MenuOption.WITHDRAW;
                case 4 -> MenuOption.TRANSFER;
                case 5 -> MenuOption.EXIT;
                default -> null;
            };

            if (option == null) {
                System.out.println("Invalid option. Please choose 1 to 5.");
                continue;
            }

            // Switch expression placeholder
            switch (option) {
                case OPEN_ACCOUNT -> System.out.println("Open Account - to be implemented in a later lab");
                case DEPOSIT      -> System.out.println("Deposit - to be implemented in a later lab");
                case WITHDRAW     -> System.out.println("Withdraw - to be implemented in a later lab");
                case TRANSFER     -> System.out.println("Transfer - to be implemented in a later lab");
                case EXIT         -> {
                    System.out.println("Thank you for banking with us. Goodbye!");
                    running = false;
                }
            }
        }
        scanner.close();
    }
}
