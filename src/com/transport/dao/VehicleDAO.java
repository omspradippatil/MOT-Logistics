package com.transport.dao;

import com.transport.db.DatabaseConnection;
import com.transport.model.Vehicle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VehicleDAO.java - Data Access Object for Vehicle
 *
 * Handles database operations for the Vehicle table.
 * Uses SQL JOINs with the driver table to include the driver's name.
 */
public class VehicleDAO {

    /** Add a new vehicle to the database */
    public boolean addVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO vehicle (registration_number, vehicle_type, driver_id, status) VALUES (?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getRegistrationNumber());
            stmt.setString(2, vehicle.getVehicleType());
            if (vehicle.getDriverId() > 0) {
                stmt.setInt(3, vehicle.getDriverId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, vehicle.getStatus());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("VehicleDAO.addVehicle error: " + e.getMessage());
            return false;
        }
    }

    /** Update existing vehicle details */
    public boolean updateVehicle(Vehicle vehicle) {
        String sql = "UPDATE vehicle SET registration_number=?, vehicle_type=?, driver_id=?, status=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getRegistrationNumber());
            stmt.setString(2, vehicle.getVehicleType());
            if (vehicle.getDriverId() > 0) {
                stmt.setInt(3, vehicle.getDriverId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, vehicle.getStatus());
            stmt.setInt(5, vehicle.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("VehicleDAO.updateVehicle error: " + e.getMessage());
            return false;
        }
    }

    /** Retrieve all vehicles with assigned driver name (using LEFT JOIN) */
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.*, d.driver_name FROM vehicle v LEFT JOIN driver d ON v.driver_id = d.id ORDER BY v.registration_number";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                vehicles.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("VehicleDAO.getAllVehicles error: " + e.getMessage());
        }
        return vehicles;
    }

    /** Retrieve a vehicle by its ID */
    public Vehicle getVehicleById(int id) {
        String sql = "SELECT v.*, d.driver_name FROM vehicle v LEFT JOIN driver d ON v.driver_id = d.id WHERE v.id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.out.println("VehicleDAO.getVehicleById error: " + e.getMessage());
        }
        return null;
    }

    /** Retrieve vehicles assigned to a specific driver */
    public List<Vehicle> getVehiclesByDriver(int driverId) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.*, d.driver_name FROM vehicle v LEFT JOIN driver d ON v.driver_id = d.id WHERE v.driver_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, driverId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                vehicles.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("VehicleDAO.getVehiclesByDriver error: " + e.getMessage());
        }
        return vehicles;
    }

    /** Delete a vehicle record by ID */
    public boolean deleteVehicle(int id) {
        String sql = "DELETE FROM vehicle WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("VehicleDAO.deleteVehicle error: " + e.getMessage());
            return false;
        }
    }

    /** Helper: convert ResultSet row into a Vehicle object */
    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getInt("id"));
        v.setRegistrationNumber(rs.getString("registration_number"));
        v.setVehicleType(rs.getString("vehicle_type"));
        v.setDriverId(rs.getInt("driver_id"));
        v.setDriverName(rs.getString("driver_name"));
        v.setStatus(rs.getString("status"));
        return v;
    }
}
