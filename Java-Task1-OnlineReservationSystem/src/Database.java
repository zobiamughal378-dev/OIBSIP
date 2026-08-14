import java.sql.*;
import java.util.Random;

/**
 * Handles all JDBC/SQLite plumbing: connecting, creating tables on first
 * run, and a couple of small helpers (PNR generation, default admin user).
 *
 * Everything goes through PreparedStatement with bound parameters -- never
 * string-concatenated SQL -- to avoid SQL injection.
 */
public class Database {

    // reservation.db will be created in the folder the app is launched from
    private static final String URL = "jdbc:sqlite:reservation.db";

    /** Demo seat cap per train + class + date combination (a real system would
     *  vary this per train/coach; kept as one constant to keep the project simple). */
    public static final int TOTAL_SEATS_PER_CLASS = 30;

    /** Opens a new connection. Caller is responsible for closing it. */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /** Creates tables if they don't already exist and seeds a default user. */
    public static void initialize() {
        String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY," +
                "password TEXT NOT NULL" +
                ")";

        // "id" is the auto-generated numeric Ticket ID; "pnr" is the separate
        // random booking reference shown to passengers.
        String reservationsTable = "CREATE TABLE IF NOT EXISTS reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "pnr TEXT UNIQUE NOT NULL," +
                "passenger_name TEXT NOT NULL," +
                "train_number TEXT NOT NULL," +
                "train_name TEXT NOT NULL," +
                "class_type TEXT NOT NULL," +
                "journey_date TEXT NOT NULL," +
                "source_station TEXT NOT NULL," +
                "destination_station TEXT NOT NULL," +
                "booked_by TEXT," +
                "booking_time TEXT NOT NULL" +
                ")";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(reservationsTable);
            seedDefaultUser(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /** Adds a default admin/admin123 login if the users table is empty. */
    private static void seedDefaultUser(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.executeUpdate();
                }
            }
        }
    }

    /** Checks username/password against the users table. */
    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login check failed: " + e.getMessage(), e);
        }
    }

    /** True if this username is already taken. */
    public static boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Username check failed: " + e.getMessage(), e);
        }
    }

    /** Creates a new login. Caller should check usernameExists() first to give a clean error. */
    public static void registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user: " + e.getMessage(), e);
        }
    }

    /** Generates a random 10-digit numeric PNR that isn't already in use. */
    public static String generateUniquePnr(Connection conn) throws SQLException {
        Random random = new Random();
        String pnr;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(random.nextInt(10));
            }
            pnr = sb.toString();
        } while (pnrExists(conn, pnr));
        return pnr;
    }

    private static boolean pnrExists(Connection conn, String pnr) throws SQLException {
        String sql = "SELECT 1 FROM reservations WHERE pnr = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Number of seats already booked for a given train + class + date. */
    public static int countOccupiedSeats(Connection conn, String trainNumber, String classType, String journeyDate)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE train_number = ? AND class_type = ? AND journey_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            ps.setString(2, classType);
            ps.setString(3, journeyDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
