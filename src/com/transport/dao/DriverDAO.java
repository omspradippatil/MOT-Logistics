package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Driver;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DriverDAO.java - Data Access Object for Driver
 *
 * Handles all database operations for the Driver table:
 * - Driver login check
 * - Add new driver
 * - Update driver details
 * - Deactivate or activate driver
 * - List all drivers or active drivers
 * - Get driver by ID
 */
public class DriverDAO {

    /**
     * Authenticate driver login using username and password.
     * Only active drivers can log in.
     */
    public Driver login(String username, String password) {
        String sql = "SELECT * FROM driver WHERE username=? AND password=? AND is_active=1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
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
        String sql = "INSERT INTO driver (driver_name, mobile_number, licence_number, username, password, is_active) VALUES (?,?,?,?,?,1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, driver.getDriverName());
            stmt.setString(2, driver.getMobileNumber());
            stmt.setString(3, driver.getLicenceNumber());
            stmt.setString(4, driver.getUsername());
            stmt.setString(5, driver.getPassword());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("DriverDAO.addDriver error: " + e.getMessage());
            return false;
        }
    }

    /** Update driver information */
    public boolean updateDriver(Driver driver) {
        String sql = "UPDATE driver SET driver_name=?, mobile_number=?, licence_number=?, username=?, password=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, driver.getDriverName());
            stmt.setString(2, driver.getMobileNumber());
            stmt.setString(3, driver.getLicenceNumber());
            stmt.setString(4, driver.getUsername());
            stmt.setString(5, driver.getPassword());
            stmt.setInt(6, driver.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("DriverDAO.updateDriver error: " + e.getMessage());
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
     * Delete a driver by ID.
     * Safely unassigns vehicles first. If existing trips are linked,
     * foreign key prevents deletion to protect financial audit history.
     */
    public boolean deleteDriver(int id) {
        String unassignSql = "UPDATE vehicle SET driver_id = NULL WHERE driver_id = ?";
        String deleteSql   = "DELETE FROM driver WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
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
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("DriverDAO.deleteDriver error: " + e.getMessage());
            return false;
        }
    }

    /** Helper: convert a ResultSet row into a Driver object */
    private Driver mapRow(ResultSet rs) throws SQLException {
        Driver d = new Driver();
        d.setId(rs.getInt("id"));
        d.setDriverName(rs.getString("driver_name"));
        d.setMobileNumber(rs.getString("mobile_number"));
        d.setLicenceNumber(rs.getString("licence_number"));
        d.setUsername(rs.getString("username"));
        d.setPassword(rs.getString("password"));
        d.setActive(rs.getInt("is_active") == 1);
        return d;
    }
}
