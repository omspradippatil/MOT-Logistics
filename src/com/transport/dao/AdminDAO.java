package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Admin;
import com.transport.model.Company;
import java.sql.*;

/**
 * AdminDAO.java - Data Access Object for Company Administrator
 *
 * Implements authentication and profile management for Company Admins.
 * Each company has its own independent login credentials stored in the 'company' table.
 */
public class AdminDAO {

    /**
     * Authenticates a Company Admin using either Username OR Email along with Password.
     * Returns an Admin object containing company details if successful, null otherwise.
     */
    public Admin login(String usernameOrEmail, String password) {
        String sql = "SELECT * FROM company WHERE (username = ? OR email = ?) AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usernameOrEmail != null ? usernameOrEmail.trim() : "");
            stmt.setString(2, usernameOrEmail != null ? usernameOrEmail.trim() : "");
            stmt.setString(3, password != null ? password.trim() : "");

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setCompanyId(rs.getInt("id"));
                admin.setCompanyName(rs.getString("company_name"));
                admin.setAdminName(rs.getString("admin_name"));
                admin.setEmail(rs.getString("email"));
                admin.setUsername(rs.getString("username"));
                admin.setPassword(rs.getString("password"));
                admin.setContactNumber(rs.getString("contact_number"));
                admin.setAddress(rs.getString("address"));
                admin.setGstNumber(rs.getString("gst_number"));
                return admin;
            }

        } catch (SQLException e) {
            System.out.println("AdminDAO.login error: " + e.getMessage());
        }

        return null;
    }

    /**
     * Register a new company account.
     */
    public boolean registerCompany(Company company) {
        String sql = "INSERT INTO company (company_name, admin_name, email, username, password, contact_number, address, gst_number) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, company.getCompanyName());
            stmt.setString(2, company.getAdminName());
            stmt.setString(3, company.getEmail());
            stmt.setString(4, company.getUsername());
            stmt.setString(5, company.getPassword());
            stmt.setString(6, company.getContactNumber());
            stmt.setString(7, company.getAddress());
            stmt.setString(8, company.getGstNumber());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("AdminDAO.registerCompany error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates company profile details (contact, address, admin name, gst, etc.)
     */
    public boolean updateCompanyProfile(Admin admin) {
        String sql = "UPDATE company SET company_name=?, admin_name=?, email=?, contact_number=?, address=?, gst_number=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, admin.getCompanyName());
            stmt.setString(2, admin.getAdminName());
            stmt.setString(3, admin.getEmail());
            stmt.setString(4, admin.getContactNumber());
            stmt.setString(5, admin.getAddress());
            stmt.setString(6, admin.getGstNumber());
            stmt.setInt(7, admin.getCompanyId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("AdminDAO.updateCompanyProfile error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates password for a company admin.
     */
    public boolean updatePassword(int companyId, String newPassword) {
        String sql = "UPDATE company SET password=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, companyId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("AdminDAO.updatePassword error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reset password using username or email (Forgot Password recovery).
     */
    public boolean resetPassword(String usernameOrEmail, String newPassword) {
        String sql = "UPDATE company SET password=? WHERE username=? OR email=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, usernameOrEmail);
            stmt.setString(3, usernameOrEmail);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("AdminDAO.resetPassword error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve company admin details by company ID.
     */
    public Admin getCompanyById(int companyId) {
        String sql = "SELECT * FROM company WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setCompanyId(rs.getInt("id"));
                admin.setCompanyName(rs.getString("company_name"));
                admin.setAdminName(rs.getString("admin_name"));
                admin.setEmail(rs.getString("email"));
                admin.setUsername(rs.getString("username"));
                admin.setPassword(rs.getString("password"));
                admin.setContactNumber(rs.getString("contact_number"));
                admin.setAddress(rs.getString("address"));
                admin.setGstNumber(rs.getString("gst_number"));
                return admin;
            }

        } catch (SQLException e) {
            System.out.println("AdminDAO.getCompanyById error: " + e.getMessage());
        }

        return null;
    }
}
