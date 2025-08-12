import java.sql.*;
import java.util.Scanner;

public class online_registration_system {

    private static final String URL = "jdbc:mysql://localhost:3306/online_course_registration_system";
    private static final String USER = "root";
    private static final String PASSWORD = "aayushwedsmehek";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to Database!");

            while (true) {
                System.out.println("\n===== ONLINE REGISTRATION SYSTEM =====");
                System.out.println("1. Student Registration");
                System.out.println("2. Student Corner (Login)");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();
                sc.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        registerStudent(conn);
                        break;
                    case 2:
                        studentCorner(conn);
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice! Try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to register a student
    public static void registerStudent(Connection conn) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Student Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Student Branch: ");
        String branch = sc.nextLine();

        System.out.print("Enter Student Email: ");
        String email = sc.nextLine();

        try {
            String sql = "INSERT INTO students (student_name, student_branch, student_email) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, branch);
            pstmt.setString(3, email);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Student Registered Successfully!");
            } else {
                System.out.println("Registration Failed!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Student Corner (Login + Menu)
    public static void studentCorner(Connection conn) {
        Scanner sc = new Scanner(System.in);

        // LOGIN
        System.out.print("Enter your registered email: ");
        String email = sc.nextLine();

        int studentId = getStudentIdByEmail(conn, email);
        if (studentId == -1) {
            System.out.println("No student found with this email. Please register first.");
            return;
        }

        System.out.println("Login Successful! Welcome.");

        boolean stayInStudentCorner = true;

        while (stayInStudentCorner) {
            System.out.println("\n--- Student Corner ---");
            System.out.println("1. View Available Courses");
            System.out.println("2. Register for a Course");
            System.out.println("3. View My Registrations");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int opt = sc.nextInt();
            sc.nextLine();

            switch (opt) {
                case 1:
                    viewCourses(conn);
                    break;

                case 2:
                    registerForCourse(conn, studentId);
                    break;

                case 3:
                    viewMyRegistrations(conn, studentId);
                    break;

                case 4:
                    stayInStudentCorner = false;
                    break;

                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }

    // Get student ID from email
    public static int getStudentIdByEmail(Connection conn, String email) {
        try {
            String sql = "SELECT student_id FROM students WHERE student_email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("student_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // not found
    }

    // View all available courses
    public static void viewCourses(Connection conn) {
        try {
            String sql = "SELECT * FROM courses";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\nAvailable Courses:");
            while (rs.next()) {
                System.out.println(rs.getInt("course_id") + ". " +
                        rs.getString("course_name") + " - " +
                        rs.getString("course_description"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Register for a course
    public static void registerForCourse(Connection conn, int studentId) {
        Scanner sc = new Scanner(System.in);
        viewCourses(conn);

        System.out.print("\nEnter Course ID to register: ");
        int courseId = sc.nextInt();
        sc.nextLine();

        try {
            String sql = "INSERT INTO registration (student_id, course_id, registration_date) VALUES (?, ?, CURDATE())";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Course Registered Successfully!");
            } else {
                System.out.println("Failed to register for the course!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // View student's registered courses
    public static void viewMyRegistrations(Connection conn, int studentId) {
        try {
            String sql = "SELECT c.course_name, c.course_description, r.registration_date " +
                    "FROM registration r " +
                    "JOIN courses c ON r.course_id = c.course_id " +
                    "WHERE r.student_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);

            ResultSet rs = pstmt.executeQuery();
            System.out.println("\nMy Registrations:");
            boolean hasCourses = false;
            while (rs.next()) {
                hasCourses = true;
                System.out.println(rs.getString("course_name") + " - " +
                        rs.getString("course_description") +
                        " (Registered on: " + rs.getDate("registration_date") + ")");
            }
            if (!hasCourses) {
                System.out.println("No courses registered yet.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


