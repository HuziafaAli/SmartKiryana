package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/smartkiryana_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    // Opens a fresh connection to the PostgreSQL database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Verifies the database is reachable at startup
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}
