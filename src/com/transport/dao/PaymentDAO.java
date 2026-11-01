package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PaymentDAO.java - Data Access Object for Payment
 *
 * Handles creation, updates, and calculation of invoice payments and outstanding balances.
 */
public class PaymentDAO {

    /** Create a new payment record linked to an invoice */
    public boolean createPayment(Payment payment) {
        String sql = "INSERT INTO payment (invoice_id, total_amount, amount_received, remaining_amount, payment_status, notes) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payment.getInvoiceId());
            stmt.setDouble(2, payment.getTotalAmount());
            stmt.setDouble(3, payment.getAmountReceived());
            stmt.setDouble(4, payment.getRemainingAmount());
            stmt.setString(5, payment.getPaymentStatus());
            stmt.setString(6, payment.getNotes());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("PaymentDAO.createPayment error: " + e.getMessage());
            return false;
        }
    }

    /** Update an existing payment record when partial or full payment is received */
    public boolean updatePayment(Payment payment) {
        String sql = "UPDATE payment SET amount_received=?, remaining_amount=?, payment_date=?, payment_status=?, notes=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, payment.getAmountReceived());
            stmt.setDouble(2, payment.getRemainingAmount());
            stmt.setDate(3, payment.getPaymentDate() != null
                    ? new java.sql.Date(payment.getPaymentDate().getTime()) : null);
            stmt.setString(4, payment.getPaymentStatus());
            stmt.setString(5, payment.getNotes());
            stmt.setInt(6, payment.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("PaymentDAO.updatePayment error: " + e.getMessage());
            return false;
        }
    }

    /** Retrieve all payment records with joined invoice and company information */
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.*, i.invoice_number, c.company_name " +
                     "FROM payment p " +
                     "JOIN invoice i ON p.invoice_id = i.id " +
                     "JOIN company c ON i.company_id = c.id " +
                     "ORDER BY p.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("PaymentDAO.getAllPayments error: " + e.getMessage());
        }
        return payments;
    }

    /** Retrieve payment record by invoice ID */
    public Payment getPaymentByInvoiceId(int invoiceId) {
        String sql = "SELECT p.*, i.invoice_number, c.company_name " +
                     "FROM payment p " +
                     "JOIN invoice i ON p.invoice_id = i.id " +
                     "JOIN company c ON i.company_id = c.id " +
                     "WHERE p.invoice_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.out.println("PaymentDAO.getPaymentByInvoiceId error: " + e.getMessage());
        }
        return null;
    }

    /** Calculate total pending unpaid balance across all invoices */
    public double getTotalPendingAmount() {
        String sql = "SELECT COALESCE(SUM(remaining_amount), 0) FROM payment WHERE payment_status != 'Paid'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) {
            System.out.println("PaymentDAO.getTotalPendingAmount error: " + e.getMessage());
        }
        return 0.0;
    }

    /** Helper: convert ResultSet row into a Payment object */
    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setInvoiceId(rs.getInt("invoice_id"));
        p.setInvoiceNumber(rs.getString("invoice_number"));
        p.setCompanyName(rs.getString("company_name"));
        p.setTotalAmount(rs.getDouble("total_amount"));
        p.setAmountReceived(rs.getDouble("amount_received"));
        p.setRemainingAmount(rs.getDouble("remaining_amount"));
        p.setPaymentDate(rs.getDate("payment_date"));
        p.setPaymentStatus(rs.getString("payment_status"));
        p.setNotes(rs.getString("notes"));
        return p;
    }
}
