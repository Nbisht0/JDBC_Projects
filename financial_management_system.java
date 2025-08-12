import java.sql.*;
import java.util.Scanner;

public class financial_management_system {

    private static final String URL = "jdbc:mysql://localhost:3306/financial_db";
    private static final String USER = "root";
    private static final String PASSWORD = "aayushwedsmehek";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner sc = new Scanner(System.in)) {

            Class.forName("com.mysql.cj.jdbc.Driver");
            conn.setAutoCommit(false); // Manual commit control

            while (true) {
                System.out.println("\n=== Financial Management System ===");
                System.out.println("1. Add Transaction (Deposit / Withdraw)");
                System.out.println("2. View Transaction History");
                System.out.println("3. View Current Balance");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        addTransaction(conn, sc);
                        break;
                    case 2:
                        viewTransactionHistory(conn);
                        break;
                    case 3:
                        viewCurrentBalance(conn);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice! Try again.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTransaction(Connection conn, Scanner sc) {
        try {
            System.out.print("Enter account ID (1: Checking, 2: Savings): ");
            int accountId = sc.nextInt();
            System.out.print("Enter transaction type (deposit/withdraw): ");
            String type = sc.next().toLowerCase();
            System.out.print("Enter amount: ");
            double amount = sc.nextDouble();

            // Check balance for withdrawals
            if (type.equals("withdraw")) {
                double currentBalance = getAccountBalance(conn, accountId);
                if (amount > currentBalance) {
                    System.out.println("Insufficient balance! Transaction cancelled.");
                    return;
                }
                amount = -amount; // Negative for withdrawal
            }

            // Batch Processing
            Statement stmt = conn.createStatement();

            // Add insert transaction to batch
            String insertQuery = String.format(
                    "INSERT INTO transactions (account_id, transaction_date, amount, transaction_type) VALUES (%d, NOW(), %.2f, '%s')",
                    accountId, amount, type);
            stmt.addBatch(insertQuery);

            // Add update balance to batch
            String updateQuery = String.format(
                    "UPDATE accounts SET balance = balance + %.2f WHERE account_id = %d",
                    amount, accountId);
            stmt.addBatch(updateQuery);

            // Execute batch
            int[] results = stmt.executeBatch();

            // Check if both operations succeeded
            if (results[0] >= 0 && results[1] >= 0) {
                conn.commit();
                System.out.println(" Transaction committed successfully!");
            } else {
                conn.rollback();
                System.out.println(" Transaction rolled back due to failure.");
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
                System.out.println("⚠ Error occurred. Transaction rolled back.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private static double getAccountBalance(Connection conn, int accountId) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        }
        return 0;
    }

    private static void viewTransactionHistory(Connection conn) {
        try {
            String query = "SELECT * FROM transactions ORDER BY transaction_date DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("\n--- Transaction History ---");
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("transaction_id")
                            + " | Account ID: " + rs.getInt("account_id")
                            + " | Date: " + rs.getDate("transaction_date")
                            + " | Amount: " + rs.getDouble("amount")
                            + " | Type: " + rs.getString("transaction_type"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewCurrentBalance(Connection conn) {
        try {
            String query = "SELECT * FROM accounts";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("\n--- Current Balances ---");
                while (rs.next()) {
                    System.out.println("Account ID: " + rs.getInt("account_id")
                            + " | Name: " + rs.getString("account_name")
                            + " | Balance: " + rs.getDouble("balance"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
