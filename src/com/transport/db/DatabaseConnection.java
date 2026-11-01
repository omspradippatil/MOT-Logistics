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

    // --- Database settings ---
    private static final String URL      = "jdbc:mysql://localhost:3306/transport_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "";   // Change to your MySQL root password

    // Private constructor - nobody can create an object of this class
    private DatabaseConnection() {}

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
        return DriverManager.getConnection(URL, USER, PASSWORD);
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
