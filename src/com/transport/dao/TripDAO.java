package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Trip;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TripDAO.java - Data Access Object for Trip Management
 *
 * Enforces strict multi-tenant isolation:
 * - Company Admins can ONLY view trips for their own company_id.
 * - Drivers can view their trips across multiple companies.
 */
public class TripDAO {

    /** Generate next sequential trip code like TRIP001, TRIP002, ... */
    public String generateTripCode() {
        String sql = "SELECT COUNT(*) FROM trip";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("TRIP%03d", count);
            }

        } catch (SQLException e) {
            System.out.println("TripDAO.generateTripCode error: " + e.getMessage());
        }
        return "TRIP001";
    }

    /** Add a new trip entry */
    public boolean addTrip(Trip trip) {
        String sql = "INSERT INTO trip (trip_code, trip_date, company_id, driver_id, vehicle_id, source_location, destination, goods_description, amount, trip_status, remarks) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trip.getTripCode());
            stmt.setDate(2, new java.sql.Date(trip.getTripDate().getTime()));
            stmt.setInt(3, trip.getCompanyId());
            stmt.setInt(4, trip.getDriverId());
            stmt.setInt(5, trip.getVehicleId());
            stmt.setString(6, trip.getSourceLocation());
            stmt.setString(7, trip.getDestination());
            stmt.setString(8, trip.getGoodsDescription());
            stmt.setDouble(9, trip.getAmount());
            stmt.setString(10, trip.getTripStatus() != null ? trip.getTripStatus() : "Completed");
            stmt.setString(11, trip.getRemarks());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("TripDAO.addTrip error: " + e.getMessage());
            return false;
        }
    }

    /** Update an existing trip record */
    public boolean updateTrip(Trip trip) {
        String sql = "UPDATE trip SET trip_date=?, company_id=?, driver_id=?, vehicle_id=?, source_location=?, destination=?, goods_description=?, amount=?, trip_status=?, remarks=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(trip.getTripDate().getTime()));
            stmt.setInt(2, trip.getCompanyId());
            stmt.setInt(3, trip.getDriverId());
            stmt.setInt(4, trip.getVehicleId());
            stmt.setString(5, trip.getSourceLocation());
            stmt.setString(6, trip.getDestination());
            stmt.setString(7, trip.getGoodsDescription());
            stmt.setDouble(8, trip.getAmount());
            stmt.setString(9, trip.getTripStatus() != null ? trip.getTripStatus() : "Completed");
            stmt.setString(10, trip.getRemarks());
            stmt.setInt(11, trip.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("TripDAO.updateTrip error: " + e.getMessage());
            return false;
        }
    }

    /** Delete a trip by ID */
    public boolean deleteTrip(int id) {
        String sql = "DELETE FROM trip WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("TripDAO.deleteTrip error: " + e.getMessage());
            return false;
        }
    }

    /** Retrieve a single trip by ID */
    public Trip getTripById(int id) {
        String sql = "SELECT t.*, c.company_name, d.driver_name, v.registration_number " +
                     "FROM trip t " +
                     "JOIN company c ON t.company_id = c.id " +
                     "JOIN driver d ON t.driver_id = d.id " +
                     "JOIN vehicle v ON t.vehicle_id = v.id " +
                     "WHERE t.id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.out.println("TripDAO.getTripById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Flexible filter for trips by driver, company, vehicle, month, and year.
     * Pass -1 to ignore any filter parameter.
     */
    public List<Trip> getTripsByFilter(int driverId, int companyId, int vehicleId, int month, int year) {
        return getTripsByFilter(driverId, companyId, vehicleId, month, year, null);
    }

    public List<Trip> getTripsByFilter(int driverId, int companyId, int vehicleId, int month, int year, String searchKeyword) {
        List<Trip> trips = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, c.company_name, d.driver_name, v.registration_number " +
            "FROM trip t " +
            "JOIN company c ON t.company_id = c.id " +
            "JOIN driver d ON t.driver_id = d.id " +
            "JOIN vehicle v ON t.vehicle_id = v.id WHERE 1=1"
        );

        if (driverId > 0)  sql.append(" AND t.driver_id=?");
        if (companyId > 0) sql.append(" AND t.company_id=?");
        if (vehicleId > 0) sql.append(" AND t.vehicle_id=?");
        if (month > 0)     sql.append(" AND MONTH(t.trip_date)=?");
        if (year > 0)      sql.append(" AND YEAR(t.trip_date)=?");
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(t.source_location) LIKE ? OR LOWER(t.destination) LIKE ? OR LOWER(t.trip_code) LIKE ? OR LOWER(c.company_name) LIKE ? OR LOWER(d.driver_name) LIKE ? OR LOWER(v.registration_number) LIKE ? OR LOWER(COALESCE(t.goods_description, '')) LIKE ?)");
        }
        sql.append(" ORDER BY t.trip_date DESC, t.id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (driverId > 0)  stmt.setInt(idx++, driverId);
            if (companyId > 0) stmt.setInt(idx++, companyId);
            if (vehicleId > 0) stmt.setInt(idx++, vehicleId);
            if (month > 0)     stmt.setInt(idx++, month);
            if (year > 0)      stmt.setInt(idx++, year);
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String pattern = "%" + searchKeyword.trim().toLowerCase() + "%";
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trips.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("TripDAO.getTripsByFilter error: " + e.getMessage());
        }
        return trips;
    }

    /** Total count of all trips entered in the system */
    public int getTotalTripCount() {
        String sql = "SELECT COUNT(*) FROM trip";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("TripDAO.getTotalTripCount error: " + e.getMessage());
        }
        return 0;
    }

    /** Sum of earnings for the current month */
    public double getCurrentMonthTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM trip WHERE MONTH(trip_date)=MONTH(CURDATE()) AND YEAR(trip_date)=YEAR(CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("TripDAO.getCurrentMonthTotal error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Strictly isolated trips query for Company Admin.
     * Guaranteed never to leak other companies' data.
     */
    public List<Trip> getTripsByCompany(int companyId) {
        return getTripsByCompany(companyId, -1, -1, -1, -1, null);
    }

    public List<Trip> getTripsByCompany(int companyId, int driverId, int vehicleId, int month, int year, String searchKeyword) {
        List<Trip> trips = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, c.company_name, d.driver_name, v.registration_number " +
            "FROM trip t " +
            "JOIN company c ON t.company_id = c.id " +
            "JOIN driver d ON t.driver_id = d.id " +
            "JOIN vehicle v ON t.vehicle_id = v.id " +
            "WHERE t.company_id = ?"
        );

        if (driverId > 0)  sql.append(" AND t.driver_id = ?");
        if (vehicleId > 0) sql.append(" AND t.vehicle_id = ?");
        if (month > 0)     sql.append(" AND MONTH(t.trip_date) = ?");
        if (year > 0)      sql.append(" AND YEAR(t.trip_date) = ?");
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(t.source_location) LIKE ? OR LOWER(t.destination) LIKE ? OR LOWER(t.trip_code) LIKE ? OR LOWER(d.driver_name) LIKE ? OR LOWER(v.registration_number) LIKE ? OR LOWER(COALESCE(t.goods_description, '')) LIKE ?)");
        }
        sql.append(" ORDER BY t.trip_date DESC, t.id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            stmt.setInt(idx++, companyId);
            if (driverId > 0)  stmt.setInt(idx++, driverId);
            if (vehicleId > 0) stmt.setInt(idx++, vehicleId);
            if (month > 0)     stmt.setInt(idx++, month);
            if (year > 0)      stmt.setInt(idx++, year);
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String pattern = "%" + searchKeyword.trim().toLowerCase() + "%";
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trips.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("TripDAO.getTripsByCompany error: " + e.getMessage());
        }
        return trips;
    }

    /**
     * Retrieves trips for a driver across all companies (or filtered by company).
     */
    public List<Trip> getTripsByDriver(int driverId) {
        return getTripsByDriver(driverId, -1, -1, -1, -1, null);
    }

    public List<Trip> getTripsByDriver(int driverId, int companyId, int vehicleId, int month, int year, String searchKeyword) {
        List<Trip> trips = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, c.company_name, d.driver_name, v.registration_number " +
            "FROM trip t " +
            "JOIN company c ON t.company_id = c.id " +
            "JOIN driver d ON t.driver_id = d.id " +
            "JOIN vehicle v ON t.vehicle_id = v.id " +
            "WHERE t.driver_id = ?"
        );

        if (companyId > 0) sql.append(" AND t.company_id = ?");
        if (vehicleId > 0) sql.append(" AND t.vehicle_id = ?");
        if (month > 0)     sql.append(" AND MONTH(t.trip_date) = ?");
        if (year > 0)      sql.append(" AND YEAR(t.trip_date) = ?");
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(t.source_location) LIKE ? OR LOWER(t.destination) LIKE ? OR LOWER(t.trip_code) LIKE ? OR LOWER(c.company_name) LIKE ? OR LOWER(v.registration_number) LIKE ? OR LOWER(COALESCE(t.goods_description, '')) LIKE ?)");
        }
        sql.append(" ORDER BY t.trip_date DESC, t.id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            stmt.setInt(idx++, driverId);
            if (companyId > 0) stmt.setInt(idx++, companyId);
            if (vehicleId > 0) stmt.setInt(idx++, vehicleId);
            if (month > 0)     stmt.setInt(idx++, month);
            if (year > 0)      stmt.setInt(idx++, year);
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String pattern = "%" + searchKeyword.trim().toLowerCase() + "%";
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trips.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("TripDAO.getTripsByDriver error: " + e.getMessage());
        }
        return trips;
    }

    /** Retrieve trips for invoice generation */
    public List<Trip> getTripsForInvoice(int companyId, int driverId, int vehicleId, int month, int year) {
        List<Trip> trips = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT t.*, c.company_name, d.driver_name, v.registration_number " +
            "FROM trip t " +
            "JOIN company c ON t.company_id = c.id " +
            "JOIN driver d ON t.driver_id = d.id " +
            "JOIN vehicle v ON t.vehicle_id = v.id " +
            "WHERE t.company_id = ?"
        );

        if (year > 0)      sql.append(" AND YEAR(t.trip_date) = ?");
        if (month > 0)     sql.append(" AND MONTH(t.trip_date) = ?");
        if (driverId > 0)  sql.append(" AND t.driver_id = ?");
        if (vehicleId > 0) sql.append(" AND t.vehicle_id = ?");
        sql.append(" ORDER BY t.trip_date ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            stmt.setInt(idx++, companyId);
            if (year > 0)      stmt.setInt(idx++, year);
            if (month > 0)     stmt.setInt(idx++, month);
            if (driverId > 0)  stmt.setInt(idx++, driverId);
            if (vehicleId > 0) stmt.setInt(idx++, vehicleId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trips.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("TripDAO.getTripsForInvoice error: " + e.getMessage());
        }
        return trips;
    }

    /**
     * Compute statistics strictly isolated to a specific Company.
     */
    public Map<String, Object> getCompanyStats(int companyId) {
        Map<String, Object> stats = new HashMap<>();

        String sql = "SELECT " +
                     "  (SELECT COUNT(*) FROM trip WHERE company_id = ?) AS total_trips, " +
                     "  (SELECT COUNT(DISTINCT driver_id) FROM trip WHERE company_id = ?) AS total_drivers, " +
                     "  (SELECT COUNT(DISTINCT vehicle_id) FROM trip WHERE company_id = ?) AS total_vehicles, " +
                     "  (SELECT COALESCE(SUM(amount), 0) FROM trip WHERE company_id = ? AND MONTH(trip_date) = MONTH(CURDATE()) AND YEAR(trip_date) = YEAR(CURDATE())) AS current_month_billing, " +
                     "  (SELECT COALESCE(SUM(amount_paid), 0) FROM invoice WHERE company_id = ?) AS total_paid_amount, " +
                     "  (SELECT COALESCE(SUM(remaining_amount), 0) FROM invoice WHERE company_id = ?) AS total_pending_amount, " +
                     "  (SELECT COUNT(*) FROM invoice WHERE company_id = ? AND status = 'Paid') AS paid_invoices_count, " +
                     "  (SELECT COUNT(*) FROM invoice WHERE company_id = ? AND status != 'Paid') AS pending_invoices_count";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 8; i++) {
                stmt.setInt(i, companyId);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("totalTrips",           rs.getInt("total_trips"));
                stats.put("totalDrivers",         rs.getInt("total_drivers"));
                stats.put("totalVehicles",        rs.getInt("total_vehicles"));
                stats.put("currentMonthBilling",  rs.getDouble("current_month_billing"));
                stats.put("totalPaidAmount",      rs.getDouble("total_paid_amount"));
                stats.put("totalPendingAmount",   rs.getDouble("total_pending_amount"));
                stats.put("paidInvoicesCount",    rs.getInt("paid_invoices_count"));
                stats.put("pendingInvoicesCount", rs.getInt("pending_invoices_count"));
            }

        } catch (SQLException e) {
            System.out.println("TripDAO.getCompanyStats error: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Compute statistics for a specific Driver.
     */
    public Map<String, Object> getDriverStats(int driverId) {
        Map<String, Object> stats = new HashMap<>();

        String sql = "SELECT " +
                     "  (SELECT COUNT(*) FROM trip WHERE driver_id = ?) AS total_trips, " +
                     "  (SELECT COUNT(DISTINCT company_id) FROM trip WHERE driver_id = ?) AS total_companies, " +
                     "  (SELECT COALESCE(SUM(amount), 0) FROM trip WHERE driver_id = ?) AS total_earnings, " +
                     "  (SELECT COUNT(*) FROM trip WHERE driver_id = ? AND MONTH(trip_date) = MONTH(CURDATE()) AND YEAR(trip_date) = YEAR(CURDATE())) AS current_month_trips, " +
                     "  (SELECT COALESCE(SUM(amount_paid), 0) FROM invoice WHERE driver_id = ?) AS paid_amount, " +
                     "  (SELECT COALESCE(SUM(remaining_amount), 0) FROM invoice WHERE driver_id = ?) AS pending_amount";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 6; i++) {
                stmt.setInt(i, driverId);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double totalEarnings = rs.getDouble("total_earnings");
                double paidAmount    = rs.getDouble("paid_amount");
                double pendingAmount = rs.getDouble("pending_amount");

                // If invoice-level pending is 0 but earnings exist without an invoice, fallback to clean calculation
                if (pendingAmount == 0 && totalEarnings > paidAmount) {
                    pendingAmount = totalEarnings - paidAmount;
                }

                stats.put("totalTrips",          rs.getInt("total_trips"));
                stats.put("totalCompanies",      rs.getInt("total_companies"));
                stats.put("totalEarnings",       totalEarnings);
                stats.put("currentMonthTrips",   rs.getInt("current_month_trips"));
                stats.put("paidAmount",          paidAmount);
                stats.put("pendingAmount",       pendingAmount);
            }

        } catch (SQLException e) {
            System.out.println("TripDAO.getDriverStats error: " + e.getMessage());
        }
        return stats;
    }

    /** Helper: convert ResultSet row into a Trip object */
    private Trip mapRow(ResultSet rs) throws SQLException {
        Trip t = new Trip();
        t.setId(rs.getInt("id"));
        t.setTripCode(rs.getString("trip_code"));
        t.setTripDate(rs.getDate("trip_date"));
        t.setCompanyId(rs.getInt("company_id"));
        t.setCompanyName(rs.getString("company_name"));
        t.setDriverId(rs.getInt("driver_id"));
        t.setDriverName(rs.getString("driver_name"));
        t.setVehicleId(rs.getInt("vehicle_id"));
        t.setVehicleNumber(rs.getString("registration_number"));
        t.setSourceLocation(rs.getString("source_location"));
        t.setDestination(rs.getString("destination"));
        t.setGoodsDescription(rs.getString("goods_description"));
        t.setAmount(rs.getDouble("amount"));
        t.setTripStatus(rs.getString("trip_status"));
        t.setRemarks(rs.getString("remarks"));
        return t;
    }
}
