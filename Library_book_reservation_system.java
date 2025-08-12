import java.sql.*;
import java.util.Scanner;

public class Library_book_reservation_system {
    private static final String URL = "jdbc:mysql://localhost:3306/library_system";
    private static final String USER = "root";
    private static final String PASSWORD = "aayushwedsmehek";
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("ARE YOU A STUDENT OR LIBRARY STAFF ?");
            String role = scanner.nextLine().trim().toLowerCase();

            if (role.equals("student") || role.equals("member")) {
                studentMenu(conn);
            } else if (role.equals("staff") || role.equals("library staff")) {
                staffMenu(conn);
            } else {
                System.out.println("Invalid input! Please restart and enter 'student' or 'library staff'.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void studentMenu(Connection conn) throws SQLException {
        while (true) {
            System.out.println("\n===== STUDENT MENU =====");
            System.out.println("1. View All Books");
            System.out.println("2. Search Book by Name");
            System.out.println("3. Reserve a Book");
            System.out.println("4. Exit to main menu");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    viewAllBooks(conn);
                    break;
                case 2:
                    searchBook(conn);
                    break;
                case 3:
                    reserveBook(conn);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }

    private static void staffMenu(Connection conn) throws SQLException {
        while (true) {
            System.out.println("\n===== STAFF MENU =====");
            System.out.println("1. Add New Book");
            System.out.println("2. Remove Book");
            System.out.println("3. View All Reservations");
            System.out.println("4. Exit");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addBook(conn);
                    break;
                case 2:
                    removeBook(conn);
                    break;
                case 3:
                    viewReservations(conn);
                    break;
                case 4:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }

    private static void viewAllBooks(Connection conn) throws SQLException {
        String sql = "SELECT * FROM books";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getInt("book_id") + " | " +
                        rs.getString("book_name") + " | " +
                        rs.getString("book_author") + " | " +
                        rs.getInt("book_quantity"));
            }
        }
    }

    private static void searchBook(Connection conn) throws SQLException {
        System.out.print("Enter book name to search: ");
        String name = scanner.nextLine();
        String sql = "SELECT * FROM books WHERE book_name LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getInt("book_id") + " | " +
                            rs.getString("book_name") + " | " +
                            rs.getString("book_author") + " | " +
                            rs.getInt("book_quantity"));
                }
            }
        }
    }

    private static void reserveBook(Connection conn) {
        try {
            System.out.print("Enter Member ID: ");
            int memberId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter Book ID: ");
            int bookId = Integer.parseInt(scanner.nextLine());

            // Check if Member exists
            PreparedStatement checkMember = conn.prepareStatement(
                    "SELECT COUNT(*) FROM members WHERE member_id = ?");
            checkMember.setInt(1, memberId);
            ResultSet memberRs = checkMember.executeQuery();
            memberRs.next();
            if (memberRs.getInt(1) == 0) {
                System.out.println("Member ID not found! Please register first.");
                return;
            }

            // Check if Book exists
            PreparedStatement checkBook = conn.prepareStatement(
                    "SELECT COUNT(*) FROM books WHERE book_id = ?");
            checkBook.setInt(1, bookId);
            ResultSet bookRs = checkBook.executeQuery();
            bookRs.next();
            if (bookRs.getInt(1) == 0) {
                System.out.println("Book ID not found!");
                return;
            }

            // Insert reservation
            PreparedStatement reserveStmt = conn.prepareStatement(
                    "INSERT INTO reservations (member_id, book_id, reservation_date) VALUES (?, ?, NOW())");
            reserveStmt.setInt(1, memberId);
            reserveStmt.setInt(2, bookId);
            int rows = reserveStmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Book reserved successfully!");
            } else {
                System.out.println("Could not reserve the book.");
            }

        } catch (Exception e) {
            System.out.println("Error reserving book: " + e.getMessage());
        }
    }

    private static void addBook(Connection conn) throws SQLException {
        System.out.print("Enter Book ID: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter book name: ");
        String name = scanner.nextLine();
        scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        scanner.nextLine();

        String sql = "INSERT INTO books (book_id ,book_name, book_author, book_quantity) VALUES (? , ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setString(2, name);
            pstmt.setString(3, author);
            pstmt.setInt(4, quantity);


            pstmt.executeUpdate();
            System.out.println("Book added successfully!");
        }
    }

    private static void removeBook(Connection conn) throws SQLException {
        System.out.print("Enter book ID to remove: ");
        int bookId = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM books WHERE book_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
            System.out.println("Book removed successfully!");
        }
    }

    private static void viewReservations(Connection conn) throws SQLException {
        String sql = "SELECT * FROM reservations";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getInt("reservation_id") + " | " +
                        rs.getInt("member_id") + " | " +
                        rs.getInt("book_id") + " | " +
                        rs.getDate("reservation_date"));
            }
        }
    }
}
