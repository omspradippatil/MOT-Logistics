package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Trip;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TripDAO.java - Data Access Object for Trip
 *
 * This is the central DAO for daily trip records.
 * Handles adding trips, retrieving trip history with filters,
 * generating trip codes, and monthly invoice trip aggregations.
 */
public class TripDAO {

    /** Generate next trip code like TRIP001, TRIP002, ... */
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
        String sql = "INSERT INTO trip (trip_code, trip_date, company_id, driver_id, vehicle_id, source_location, destination, amount, remarks) VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trip.getTripCode());
            stmt.setDate(2, new java.sql.Date(trip.getTripDate().getTime()));
            stmt.setInt(3, trip.getCompanyId());
            stmt.setInt(4, trip.getDriverId());
            stmt.setInt(5, trip.getVehicleId());
            stmt.setString(6, trip.getSourceLocation());
            stmt.setString(7, trip.getDestination());
            stmt.setDouble(8, trip.getAmount());
            stmt.setString(9, trip.getRemarks());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("TripDAO.addTrip error: " + e.getMessage());
            return false;
        }
    }

    /** Update an existing trip record */
    public boolean updateTrip(Trip trip) {
        String sql = "UPDATE trip SET trip_date=?, company_id=?, driver_id=?, vehicle_id=?, source_location=?, destination=?, amount=?, remarks=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(trip.getTripDate().getTime()));
            stmt.setInt(2, trip.getCompanyId());
            stmt.setInt(3, trip.getDriverId());
            stmt.setInt(4, trip.getVehicleId());
            stmt.setString(5, trip.getSourceLocation());
            stmt.setString(6, trip.getDestination());
            stmt.setDouble(7, trip.getAmount());
            stmt.setString(8, trip.getRemarks());
            stmt.setInt(9, trip.getId());

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

    /** Retrieve all trips */
    public List<Trip> getAllTrips() {
        return getTripsByFilter(-1, -1, -1, -1, -1);
    }

    /** Retrieve trips for a specific driver */
    public List<Trip> getTripsByDriver(int driverId) {
        return getTripsByFilter(driverId, -1, -1, -1, -1);
    }

    /** Retrieve matching trips for invoice generation (by company, month, year, driver, vehicle) */
    public List<Trip> getTripsForInvoice(int companyId, int driverId, int vehicleId, int month, int year) {
        List<Trip> trips = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT t.*, c.company_name, d.driver_name, v.registration_number " +
            "FROM trip t " +
            "JOIN company c ON t.company_id = c.id " +
            "JOIN driver d ON t.driver_id = d.id " +
            "JOIN vehicle v ON t.vehicle_id = v.id " +
            "WHERE t.company_id=? AND MONTH(t.trip_date)=? AND YEAR(t.trip_date)=?"
        );

        if (driverId > 0) sql.append(" AND t.driver_id=?");
        if (vehicleId > 0) sql.append(" AND t.vehicle_id=?");
        sql.append(" ORDER BY t.trip_date ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            stmt.setInt(idx++, companyId);
            stmt.setInt(idx++, month);
            stmt.setInt(idx++, year);
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

    /** Sum of earnings for the current month (used for dashboard) */
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

    /**
     * Flexible filter for trips by driver, company, vehicle, month, and year.
     * Pass -1 to ignore any filter parameter.
     */
    public List<Trip> getTripsByFilter(int driverId, int companyId, int vehicleId, int month, int year) {
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
        sql.append(" ORDER BY t.trip_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (driverId > 0)  stmt.setInt(idx++, driverId);
            if (companyId > 0) stmt.setInt(idx++, companyId);
            if (vehicleId > 0) stmt.setInt(idx++, vehicleId);
            if (month > 0)     stmt.setInt(idx++, month);
            if (year > 0)      stmt.setInt(idx++, year);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trips.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("TripDAO.getTripsByFilter error: " + e.getMessage());
        }
        return trips;
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
        t.setAmount(rs.getDouble("amount"));
        t.setRemarks(rs.getString("remarks"));
        return t;
    }
}
