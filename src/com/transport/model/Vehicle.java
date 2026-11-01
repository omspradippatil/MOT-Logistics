package com.transport.model;

/**
 * Vehicle.java - Model class for Vehicle
 *
 * Represents a commercial vehicle (tempo, truck, etc.)
 * assigned to a driver.
 */
public class Vehicle {

    private int    id;
    private String registrationNumber;
    private String vehicleType;
    private int    driverId;
    private String driverName;  // For display purposes (joined from driver table)
    private String status;

    // Default constructor
    public Vehicle() {}

    // Parameterized constructor
    public Vehicle(int id, String registrationNumber, String vehicleType,
                   int driverId, String driverName, String status) {
        this.id                 = id;
        this.registrationNumber = registrationNumber;
        this.vehicleType        = vehicleType;
        this.driverId           = driverId;
        this.driverName         = driverName;
        this.status             = status;
    }

    // --- Getters ---
    public int    getId()                 { return id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getVehicleType()        { return vehicleType; }
    public int    getDriverId()           { return driverId; }
    public String getDriverName()         { return driverName; }
    public String getStatus()             { return status; }

    // --- Setters ---
    public void setId(int id)                           { this.id = id; }
    public void setRegistrationNumber(String regNo)     { this.registrationNumber = regNo; }
    public void setVehicleType(String type)             { this.vehicleType = type; }
    public void setDriverId(int driverId)               { this.driverId = driverId; }
    public void setDriverName(String name)              { this.driverName = name; }
    public void setStatus(String status)                { this.status = status; }

    // Used by JComboBox to display vehicle number
    @Override
    public String toString() {
        return registrationNumber;
    }
}
