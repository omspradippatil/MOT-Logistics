package com.transport.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection.java
 *
 * This class handles the MySQL database connection.
 * It uses the Singleton pattern so only ONE connection object exists.
 *
 * KEY CONCEPTS USED:
 * - Static method (class-level method, no object needed)
 * - JDBC (Java Database Connectivity)
 * - Exception Handling (try-catch)
 */
public class DatabaseConnection {

    // --- Default Database settings ---
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB   = "transport_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    // Private constructor - nobody can create an object of this class
    private DatabaseConnection() {}

    /**
     * Constructs the JDBC URL dynamically from environment variables,
     * falling back to default localhost settings.
     */
    public static String getJdbcUrl() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl;
        }
        String host = System.getenv("DB_HOST");
        if (host == null || host.trim().isEmpty()) host = DEFAULT_HOST;
        String port = System.getenv("DB_PORT");
        if (port == null || port.trim().isEmpty()) port = DEFAULT_PORT;
        String db = System.getenv("DB_NAME");
        if (db == null || db.trim().isEmpty()) db = DEFAULT_DB;

        return "jdbc:mysql://" + host + ":" + port + "/" + db + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    }

    public static String getJdbcUser() {
        String user = System.getenv("DB_USER");
        return (user != null && !user.trim().isEmpty()) ? user : DEFAULT_USER;
    }

    public static String getJdbcPassword() {
        String pass = System.getenv("DB_PASSWORD");
        return (pass != null) ? pass : DEFAULT_PASS;
    }

    /**
     * Returns a new database connection every time it is called.
     * Caller is responsible for closing the connection.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found! Add mysql-connector.jar to lib folder.");
            throw new SQLException("Driver not found", e);
        }
        return DriverManager.getConnection(getJdbcUrl(), getJdbcUser(), getJdbcPassword());
    }

    /**
     * Quick test to check if connection works.
     * Run this once to verify your database is set up correctly.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
}
