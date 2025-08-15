import java.sql.*;
import java.util.ArrayList;

import java.util.Scanner;

public class online_food_ordering_system {

    public static void main(String[] args) throws ClassNotFoundException {
        String url = "jdbc:mysql://localhost:3306/food_food";
        String username = "root";
        String password = "aayushwedsmehek";

        ArrayList<String> cart = new ArrayList<>();
        Scanner sc = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Drivers are loaded successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("Error in loading drivers: " + e.getMessage());
        }

        try (Connection con = DriverManager.getConnection(url, username, password)) {
            System.out.println("Connected to database successfully");

            boolean mainMenu = true;
            while (mainMenu) {
                System.out.println("\n======================================");
                System.out.println("     WELCOME TO BITEBUDDY ");
                System.out.println("  Your Delicious Food Ordering App");
                System.out.println("======================================\n");
                System.out.println("Are you a Customer or Staff? (customer/staff/exit)");
                String userType = sc.nextLine().trim().toLowerCase();

                if (userType.equals("staff")) {
                    // Staff password check
                    System.out.print("Enter staff password: ");
                    String staffPass = sc.nextLine();
                    if (!staffPass.equals("admin123")) { // password here
                        System.out.println(" Access Denied! Returning to main menu...");
                        continue; // go back to main menu
                    }

                    // Staff menu
                    boolean staffRunning = true;
                    while (staffRunning) {
                        System.out.println("\n=== STAFF MENU ===");
                        System.out.println("1. View Orders");
                        System.out.println("2. Update Food Menu");
                        System.out.println("3. Exit Staff Menu");
                        System.out.print("Choose an option: ");
                        int staffChoice = sc.nextInt();
                        sc.nextLine();

                        switch (staffChoice) {
                            case 1:
                                // View orders
                                String orderSql = "SELECT * FROM orders";
                                try (Statement stmt = con.createStatement();
                                     ResultSet rs = stmt.executeQuery(orderSql)) {
                                    System.out.println("\n--- ORDER HISTORY ---");
                                    while (rs.next()) {
                                        System.out.println("Order ID: " + rs.getInt("order_id") +
                                                ", Customer: " + rs.getString("customer_name") +
                                                ", Date: " + rs.getDate("order_date") +
                                                ", Total: ₹" + rs.getDouble("total_amount") +
                                                ", Payment: " + rs.getString("payment_mode"));
                                    }
                                }
                                break;

                            case 2:
                                // Update Food Menu sub-menu
                                boolean menuRunning = true;
                                while (menuRunning) {
                                    System.out.println("\n--- UPDATE FOOD MENU ---");
                                    System.out.println("1. Add New Item");
                                    System.out.println("2. Remove Item");
                                    System.out.println("3. Update Item Price");
                                    System.out.println("4. Back");
                                    System.out.print("Choose an option: ");
                                    int menuChoice = sc.nextInt();
                                    sc.nextLine();

                                    switch (menuChoice) {
                                        case 1:
                                            System.out.print("Enter new item name: ");
                                            String newItem = sc.nextLine();
                                            System.out.print("Enter price: ");
                                            double newPrice = sc.nextDouble();
                                            sc.nextLine();
                                            String insertMenuSql = "INSERT INTO menu (item_name, price) VALUES (?, ?)";
                                            try (PreparedStatement pstmt = con.prepareStatement(insertMenuSql)) {
                                                pstmt.setString(1, newItem);
                                                pstmt.setDouble(2, newPrice);
                                                pstmt.executeUpdate();
                                                System.out.println("Item added successfully!");
                                            }
                                            break;

                                        case 2:
                                            System.out.print("Enter Item ID to remove: ");
                                            int removeId = sc.nextInt();
                                            sc.nextLine();
                                            String deleteMenuSql = "DELETE FROM menu WHERE item_id = ?";
                                            try (PreparedStatement pstmt = con.prepareStatement(deleteMenuSql)) {
                                                pstmt.setInt(1, removeId);
                                                int rows = pstmt.executeUpdate();
                                                if (rows > 0) {
                                                    System.out.println(" Item removed successfully!");
                                                } else {
                                                    System.out.println(" Item not found!");
                                                }
                                            }
                                            break;

                                        case 3:
                                            System.out.print("Enter Item ID to update price: ");
                                            int updateId = sc.nextInt();
                                            System.out.print("Enter new price: ");
                                            double updatePrice = sc.nextDouble();
                                            sc.nextLine();
                                            String updatePriceSql = "UPDATE menu SET price = ? WHERE item_id = ?";
                                            try (PreparedStatement pstmt = con.prepareStatement(updatePriceSql)) {
                                                pstmt.setDouble(1, updatePrice);
                                                pstmt.setInt(2, updateId);
                                                int rows = pstmt.executeUpdate();
                                                if (rows > 0) {
                                                    System.out.println(" Price updated successfully!");
                                                } else {
                                                    System.out.println(" Item not found!");
                                                }
                                            }
                                            break;

                                        case 4:
                                            menuRunning = false;
                                            break;

                                        default:
                                            System.out.println(" Invalid choice!");
                                    }
                                }
                                break;

                            case 3:
                                staffRunning = false;
                                break;

                            default:
                                System.out.println(" Invalid choice!");
                        }
                    }

                } else if (userType.equals("customer")) {
                    // Customer flow
                    boolean running = true;
                    while (running) {
                        String sql = "SELECT item_id, item_name, price FROM menu";
                        try (Statement stmt = con.createStatement();
                             ResultSet rs = stmt.executeQuery(sql)) {
                            System.out.println("\n--------------------- MENU ----------------------");
                            while (rs.next()) {
                                System.out.println(rs.getInt("item_id") + ". " +
                                        rs.getString("item_name") + " - ₹" + rs.getDouble("price"));
                            }
                        }

                        System.out.print("\nEnter the Item ID to select (0 to exit): ");
                        int selectedId = sc.nextInt();
                        sc.nextLine();

                        if (selectedId == 0) {
                            System.out.println("Thanks for visiting!  Your cart: " + cart);
                            running = false;
                            break;
                        }

                        String itemSql = "SELECT item_name FROM menu WHERE item_id = ?";
                        try (PreparedStatement pstmt = con.prepareStatement(itemSql)) {
                            pstmt.setInt(1, selectedId);
                            ResultSet itemRs = pstmt.executeQuery();

                            if (itemRs.next()) {
                                String selectedItemName = itemRs.getString("item_name");
                                System.out.println("\nYou selected: " + selectedItemName);
                                System.out.println("1. Add to Cart");
                                System.out.println("2. Back to Menu");
                                System.out.print("Choose an option: ");
                                int opt = sc.nextInt();
                                sc.nextLine();

                                if (opt == 1) {
                                    cart.add(selectedItemName);
                                    System.out.println(selectedItemName + " added to cart! 🛒");

                                    System.out.println("\nWhat would you like to do now?");
                                    System.out.println("1. Place Order");
                                    System.out.println("2. Continue Shopping");
                                    int action = sc.nextInt();
                                    sc.nextLine();

                                    if (action == 1) {
                                        if (cart.isEmpty()) {
                                            System.out.println("Your cart is empty! Cannot place order.");
                                            continue;
                                        }

                                        System.out.print("Enter your name: ");
                                        String customerName = sc.nextLine();
                                        System.out.print("Enter payment mode (Cash/Card/UPI): ");
                                        String paymentMode = sc.nextLine();

                                        double totalPrice = 0.0;
                                        for (String item : cart) {
                                            String priceSql = "SELECT price FROM menu WHERE item_name = ?";
                                            try (PreparedStatement priceStmt = con.prepareStatement(priceSql)) {
                                                priceStmt.setString(1, item);
                                                ResultSet priceRs = priceStmt.executeQuery();
                                                if (priceRs.next()) {
                                                    totalPrice += priceRs.getDouble("price");
                                                }
                                            }
                                        }

                                        double gst = totalPrice * 0.07;
                                        double finalAmount = totalPrice + gst;

                                        String insertSql = "INSERT INTO orders (order_id, customer_name, order_date, total_amount, payment_mode) VALUES (?, ?, CURRENT_DATE, ?, ?)";
                                        try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                                            int orderId = (int) (System.currentTimeMillis() / 1000);
                                            insertStmt.setInt(1, orderId);
                                            insertStmt.setString(2, customerName);
                                            insertStmt.setDouble(3, finalAmount);
                                            insertStmt.setString(4, paymentMode);
                                            insertStmt.executeUpdate();
                                        }

                                        System.out.printf("\nOrder placed successfully! Total with GST: ₹%.2f%n", finalAmount);
                                        cart.clear();
                                        running = false;
                                    }
                                } else {
                                    System.out.println("Returning to menu...");
                                }
                            } else {
                                System.out.println("Invalid Item ID!");
                            }
                        }
                    }

                } else if (userType.equals("exit")) {
                    mainMenu = false;

                } else {
                    System.out.println("Invalid option! Please type 'customer', 'staff', or 'exit'.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error in establishing connection: " + e.getMessage());
        }
    }
}
