package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Invoice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InvoiceDAO.java - Data Access Object for Invoice
 *
 * Handles invoice generation records, numbering, and lookup in MySQL.
 */
public class InvoiceDAO {

    /** Generate next invoice number like INV-0001 */
    public String generateInvoiceNumber() {
        String sql = "SELECT COUNT(*) FROM invoice";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("INV-%04d", count);
            }

        } catch (SQLException e) {
            System.out.println("InvoiceDAO.generateInvoiceNumber error: " + e.getMessage());
        }
        return "INV-0001";
    }

    /** Save a new invoice record to database */
    public boolean saveInvoice(Invoice invoice) {
        String sql = "INSERT INTO invoice (invoice_number, invoice_date, company_id, driver_id, vehicle_id, billing_month, billing_year, total_trips, total_amount, status) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, invoice.getInvoiceNumber());
            stmt.setDate(2, new java.sql.Date(invoice.getInvoiceDate().getTime()));
            stmt.setInt(3, invoice.getCompanyId());
            if (invoice.getDriverId() > 0) {
                stmt.setInt(4, invoice.getDriverId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            if (invoice.getVehicleId() > 0) {
                stmt.setInt(5, invoice.getVehicleId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, invoice.getBillingMonth());
            stmt.setInt(7, invoice.getBillingYear());
            stmt.setInt(8, invoice.getTotalTrips());
            stmt.setDouble(9, invoice.getTotalAmount());
            stmt.setString(10, invoice.getStatus());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("InvoiceDAO.saveInvoice error: " + e.getMessage());
            return false;
        }
    }

    /** Retrieve ID of the most recently inserted invoice */
    public int getLastInsertedInvoiceId() {
        String sql = "SELECT MAX(id) FROM invoice";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.out.println("InvoiceDAO.getLastInsertedInvoiceId error: " + e.getMessage());
        }
        return -1;
    }

    /** Retrieve all invoices ordered from newest to oldest */
    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name, d.driver_name, v.registration_number " +
                     "FROM invoice i " +
                     "JOIN company c ON i.company_id = c.id " +
                     "LEFT JOIN driver d ON i.driver_id = d.id " +
                     "LEFT JOIN vehicle v ON i.vehicle_id = v.id " +
                     "ORDER BY i.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                invoices.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("InvoiceDAO.getAllInvoices error: " + e.getMessage());
        }
        return invoices;
    }

    /** Retrieve invoices associated with a specific driver */
    public List<Invoice> getInvoicesByDriver(int driverId) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name, d.driver_name, v.registration_number " +
                     "FROM invoice i " +
                     "JOIN company c ON i.company_id = c.id " +
                     "LEFT JOIN driver d ON i.driver_id = d.id " +
                     "LEFT JOIN vehicle v ON i.vehicle_id = v.id " +
                     "WHERE i.driver_id=? " +
                     "ORDER BY i.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, driverId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                invoices.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("InvoiceDAO.getInvoicesByDriver error: " + e.getMessage());
        }
        return invoices;
    }

    /** Retrieve invoices associated with a specific company */
    public List<Invoice> getInvoicesByCompany(int companyId) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name, d.driver_name, v.registration_number " +
                     "FROM invoice i " +
                     "JOIN company c ON i.company_id = c.id " +
                     "LEFT JOIN driver d ON i.driver_id = d.id " +
                     "LEFT JOIN vehicle v ON i.vehicle_id = v.id " +
                     "WHERE i.company_id=? " +
                     "ORDER BY i.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                invoices.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("InvoiceDAO.getInvoicesByCompany error: " + e.getMessage());
        }
        return invoices;
    }

    /** Retrieve an invoice by ID */
    public Invoice getInvoiceById(int id) {
        String sql = "SELECT i.*, c.company_name, d.driver_name, v.registration_number " +
                     "FROM invoice i " +
                     "JOIN company c ON i.company_id = c.id " +
                     "LEFT JOIN driver d ON i.driver_id = d.id " +
                     "LEFT JOIN vehicle v ON i.vehicle_id = v.id " +
                     "WHERE i.id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.out.println("InvoiceDAO.getInvoiceById error: " + e.getMessage());
        }
        return null;
    }

    /** Total count of generated invoices */
    public int getTotalInvoiceCount() {
        String sql = "SELECT COUNT(*) FROM invoice";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.out.println("InvoiceDAO.getTotalInvoiceCount error: " + e.getMessage());
        }
        return 0;
    }

    /** Helper: convert ResultSet row into an Invoice object */
    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        inv.setInvoiceDate(rs.getDate("invoice_date"));
        inv.setCompanyId(rs.getInt("company_id"));
        inv.setCompanyName(rs.getString("company_name"));
        inv.setDriverId(rs.getInt("driver_id"));
        inv.setDriverName(rs.getString("driver_name"));
        inv.setVehicleId(rs.getInt("vehicle_id"));
        inv.setVehicleNumber(rs.getString("registration_number"));
        inv.setBillingMonth(rs.getInt("billing_month"));
        inv.setBillingYear(rs.getInt("billing_year"));
        inv.setTotalTrips(rs.getInt("total_trips"));
        inv.setTotalAmount(rs.getDouble("total_amount"));
        inv.setStatus(rs.getString("status"));
        return inv;
    }
}
