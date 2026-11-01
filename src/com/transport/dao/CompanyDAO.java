package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Company;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CompanyDAO.java - Data Access Object for Company
 *
 * Handles all database operations for the Company table:
 * - Add a new company
 * - Update company details
 * - Get all companies
 * - Get company by ID
 */
public class CompanyDAO {

    /** Add a new company to the database */
    public boolean addCompany(Company company) {
        String sql = "INSERT INTO company (company_name, address, contact_number, gst_number) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, company.getCompanyName());
            stmt.setString(2, company.getAddress());
            stmt.setString(3, company.getContactNumber());
            stmt.setString(4, company.getGstNumber());

            int rows = stmt.executeUpdate();
            return rows > 0; // true if insert was successful

        } catch (SQLException e) {
            System.out.println("CompanyDAO.addCompany error: " + e.getMessage());
            return false;
        }
    }

    /** Update an existing company */
    public boolean updateCompany(Company company) {
        String sql = "UPDATE company SET company_name=?, address=?, contact_number=?, gst_number=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, company.getCompanyName());
            stmt.setString(2, company.getAddress());
            stmt.setString(3, company.getContactNumber());
            stmt.setString(4, company.getGstNumber());
            stmt.setInt(5, company.getId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("CompanyDAO.updateCompany error: " + e.getMessage());
            return false;
        }
    }

    /** Get all companies from database */
    public List<Company> getAllCompanies() {
        List<Company> companies = new ArrayList<>();
        String sql = "SELECT * FROM company ORDER BY company_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Company c = new Company();
                c.setId(rs.getInt("id"));
                c.setCompanyName(rs.getString("company_name"));
                c.setAddress(rs.getString("address"));
                c.setContactNumber(rs.getString("contact_number"));
                c.setGstNumber(rs.getString("gst_number"));
                companies.add(c);
            }

        } catch (SQLException e) {
            System.out.println("CompanyDAO.getAllCompanies error: " + e.getMessage());
        }

        return companies;
    }

    /** Get one company by ID */
    public Company getCompanyById(int id) {
        String sql = "SELECT * FROM company WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Company c = new Company();
                c.setId(rs.getInt("id"));
                c.setCompanyName(rs.getString("company_name"));
                c.setAddress(rs.getString("address"));
                c.setContactNumber(rs.getString("contact_number"));
                c.setGstNumber(rs.getString("gst_number"));
                return c;
            }

        } catch (SQLException e) {
            System.out.println("CompanyDAO.getCompanyById error: " + e.getMessage());
        }

        return null;
    }

    /** Delete company by ID */
    public boolean deleteCompany(int id) {
        String sql = "DELETE FROM company WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("CompanyDAO.deleteCompany error: " + e.getMessage());
            return false;
        }
    }
}
