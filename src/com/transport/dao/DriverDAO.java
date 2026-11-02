package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Driver;
import com.transport.model.DriverCompanySummary;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DriverDAO.java - Data Access Object for Driver
 *
 * Handles database operations for Driver accounts:
 * - Driver authentication (Username or Email)
 * - Driver registration & profile updates
 * - Company-specific driver roster queries and driver onboarding by company
 * - Vehicle assignments and deactivation
 */
public class DriverDAO {

    /**
     * Authenticate driver login using username OR email and password.
     * Only active accounts are permitted.
     */
    public Driver login(String usernameOrEmail, String password) {
        String sql = "SELECT * FROM driver WHERE (username=? OR email=?) AND password=? AND is_active=1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            stmt.setString(3, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.out.println("DriverDAO.login error: " + e.getMessage());
        }
        return null;
    }

    /** Add a new driver to the database */
    public boolean addDriver(Driver driver) {
        String sql = "INSERT INTO driver (driver_name, mobile_number, email, licence_number, username, password, is_active, company_id) " +
                     "VALUES (?,?,?,?,?,?,1,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, driver.getDriverName());
            stmt.setString(2, driver.getMobileNumber());
            stmt.setString(3, driver.getEmail());
            stmt.setString(4, driver.getLicenceNumber());
            stmt.setString(5, driver.getUsername());
            stmt.setString(6, driver.getPassword());
            if (driver.getCompanyId() > 0) {
                stmt.setInt(7, driver.getCompanyId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("DriverDAO.addDriver error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Register a new driver along with their primary vehicle in an atomic transaction.
     */
    public boolean registerDriverWithVehicle(Driver driver, String vehicleReg, String vehicleType) {
        return registerDriverWithVehicle(driver, vehicleReg, vehicleType, driver.getCompanyId());
    }

    /**
     * Register a new driver along with their primary vehicle linked to a specific company.
     */
    public boolean registerDriverWithVehicle(Driver driver, String vehicleReg, String vehicleType, int companyId) {
        String driverSql  = "INSERT INTO driver (driver_name, mobile_number, email, licence_number, username, password, is_active, company_id) " +
                            "VALUES (?,?,?,?,?,?,1,?)";
        String vehicleSql = "INSERT INTO vehicle (registration_number, vehicle_type, driver_id, status) VALUES (?,?,?, 'Active')";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int driverId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(driverSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, driver.getDriverName());
                stmt.setString(2, driver.getMobileNumber());
                stmt.setString(3, driver.getEmail());
                stmt.setString(4, driver.getLicenceNumber());
                stmt.setString(5, driver.getUsername());
                stmt.setString(6, driver.getPassword());
                if (companyId > 0) {
                    stmt.setInt(7, companyId);
                } else {
                    stmt.setNull(7, Types.INTEGER);
                }
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    driverId = rs.getInt(1);
                }
            }

            if (driverId > 0 && vehicleReg != null && !vehicleReg.trim().isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(vehicleSql)) {
                    stmt.setString(1, vehicleReg.trim().toUpperCase());
                    stmt.setString(2, vehicleType != null ? vehicleType : "Truck");
                    stmt.setInt(3, driverId);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("DriverDAO.registerDriverWithVehicle error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    /**
     * Add a driver directly from Company Admin panel.
     */
    public boolean addDriverForCompany(Driver driver, String vehicleReg, String vehicleType, int companyId) {
        driver.setCompanyId(companyId);
        return registerDriverWithVehicle(driver, vehicleReg, vehicleType, companyId);
    }

    /**
     * Assign or link an existing driver to a company.
     */
    public boolean assignDriverToCompany(int driverId, int companyId) {
        String sql = "UPDATE driver SET company_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            stmt.setInt(2, driverId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("DriverDAO.assignDriverToCompany error: " + e.getMessage());
            return false;
        }
    }

    /** Update driver information */
    public boolean updateDriver(Driver driver) {
        String sql = "UPDATE driver SET driver_name=?, mobile_number=?, email=?, licence_number=?, username=?, password=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, driver.getDriverName());
            stmt.setString(2, driver.getMobileNumber());
            stmt.setString(3, driver.getEmail());
            stmt.setString(4, driver.getLicenceNumber());
            stmt.setString(5, driver.getUsername());
            stmt.setString(6, driver.getPassword());
            stmt.setInt(7, driver.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("DriverDAO.updateDriver error: " + e.getMessage());
            return false;
        }
    }

    /** Update driver self-profile */
    public boolean updateProfile(int driverId, String mobile, String email, String password) {
        String sql;
        boolean hasPassword = password != null && !password.trim().isEmpty();

        if (hasPassword) {
            sql = "UPDATE driver SET mobile_number=?, email=?, password=? WHERE id=?";
        } else {
            sql = "UPDATE driver SET mobile_number=?, email=? WHERE id=?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mobile);
            stmt.setString(2, email);
            if (hasPassword) {
                stmt.setString(3, password);
                stmt.setInt(4, driverId);
            } else {
                stmt.setInt(3, driverId);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("DriverDAO.updateProfile error: " + e.getMessage());
            return false;
        }
    }

    /** Reset password using username or email */
    public boolean resetPassword(String usernameOrEmail, String newPassword) {
        String sql = "UPDATE driver SET password=? WHERE username=? OR email=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, usernameOrEmail);
            stmt.setString(3, usernameOrEmail);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("DriverDAO.resetPassword error: " + e.getMessage());
            return false;
        }
    }

    /** Activate or deactivate a driver account */
    public boolean setDriverActive(int driverId, boolean active) {
        String sql = "UPDATE driver SET is_active=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, active ? 1 : 0);
            stmt.setInt(2, driverId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("DriverDAO.setDriverActive error: " + e.getMessage());
            return false;
        }
    }

    /** Retrieve all drivers (both active and inactive) */
    public List<Driver> getAllDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM driver ORDER BY driver_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                drivers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("DriverDAO.getAllDrivers error: " + e.getMessage());
        }
        return drivers;
    }

    /** Retrieve only active drivers for assignment in dropdowns */
    public List<Driver> getActiveDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM driver WHERE is_active=1 ORDER BY driver_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                drivers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("DriverDAO.getActiveDrivers error: " + e.getMessage());
        }
        return drivers;
    }

    /** Retrieve a driver by their primary key ID */
    public Driver getDriverById(int id) {
        String sql = "SELECT * FROM driver WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.out.println("DriverDAO.getDriverById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves summary of drivers associated with a specific company:
     * - Drivers who have completed trips for this company, OR
     * - Drivers registered directly by this company.
     * Returned columns: Driver Name, Vehicle Number, Total Trips.
     */
    public List<DriverCompanySummary> getDriversForCompany(int companyId) {
        List<DriverCompanySummary> list = new ArrayList<>();
        String sql = "SELECT d.id AS driver_id, d.driver_name, d.mobile_number, d.email, " +
                     "       COALESCE(GROUP_CONCAT(DISTINCT v.registration_number ORDER BY v.registration_number SEPARATOR ', '), 'N/A') AS vehicle_numbers, " +
                     "       COUNT(DISTINCT t.id) AS total_trips " +
                     "FROM driver d " +
                     "LEFT JOIN vehicle v ON v.driver_id = d.id " +
                     "LEFT JOIN trip t ON t.driver_id = d.id AND t.company_id = ? " +
                     "WHERE d.company_id = ? OR t.company_id = ? " +
                     "GROUP BY d.id, d.driver_name, d.mobile_number, d.email " +
                     "ORDER BY total_trips DESC, d.driver_name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            stmt.setInt(2, companyId);
            stmt.setInt(3, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DriverCompanySummary s = new DriverCompanySummary();
                s.setDriverId(rs.getInt("driver_id"));
                s.setDriverName(rs.getString("driver_name"));
                s.setMobileNumber(rs.getString("mobile_number"));
                s.setEmail(rs.getString("email"));
                s.setVehicleNumbers(rs.getString("vehicle_numbers"));
                s.setTotalTrips(rs.getInt("total_trips"));
                list.add(s);
            }

        } catch (SQLException e) {
            System.out.println("DriverDAO.getDriversForCompany error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Delete a driver by ID safely.
     */
    public boolean deleteDriver(int id) {
        String unassignSql = "UPDATE vehicle SET driver_id = NULL WHERE driver_id = ?";
        String deleteSql   = "DELETE FROM driver WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement unassignStmt = conn.prepareStatement(unassignSql);
                 PreparedStatement deleteStmt   = conn.prepareStatement(deleteSql)) {

                unassignStmt.setInt(1, id);
                unassignStmt.executeUpdate();

                deleteStmt.setInt(1, id);
                int rows = deleteStmt.executeUpdate();

                conn.commit();
                return rows > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            System.out.println("DriverDAO.deleteDriver error: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /** Helper: convert a ResultSet row into a Driver object */
    private Driver mapRow(ResultSet rs) throws SQLException {
        Driver d = new Driver();
        d.setId(rs.getInt("id"));
        d.setDriverName(rs.getString("driver_name"));
        d.setMobileNumber(rs.getString("mobile_number"));
        d.setEmail(rs.getString("email"));
        d.setLicenceNumber(rs.getString("licence_number"));
        d.setUsername(rs.getString("username"));
        d.setPassword(rs.getString("password"));
        d.setActive(rs.getInt("is_active") == 1);
        try {
            d.setCompanyId(rs.getInt("company_id"));
        } catch (SQLException ignored) {}
        return d;
    }
}
