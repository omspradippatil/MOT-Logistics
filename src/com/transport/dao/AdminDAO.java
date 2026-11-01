package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Admin;
import java.sql.*;

/**
 * AdminDAO.java - Data Access Object for Admin
 *
 * DAO = Data Access Object
 * This class talks to the database on behalf of the Admin.
 * It converts database rows into Admin objects and vice versa.
 *
 * KEY CONCEPTS:
 * - JDBC (PreparedStatement, ResultSet)
 * - Exception Handling
 * - Encapsulation
 */
public class AdminDAO {

    /**
     * Checks if username and password match an admin in the database.
     * Returns the Admin object if login is successful, null otherwise.
     */
    public Admin login(String username, String password) {
        // SQL query with ? placeholders (prevents SQL injection)
        String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Login successful - create Admin object from result
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setUsername(rs.getString("username"));
                admin.setPassword(rs.getString("password"));
                admin.setFullName(rs.getString("full_name"));
                return admin;
            }

        } catch (SQLException e) {
            System.out.println("AdminDAO.login error: " + e.getMessage());
        }

        return null; // Login failed
    }
}
