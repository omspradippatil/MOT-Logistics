package com.transport.model;

/**
 * DriverCompanySummary.java - Summary model for Company Admin's Drivers view
 *
 * Encapsulates driver details, assigned/used vehicles, and total trips completed
 * for a specific client company.
 */
public class DriverCompanySummary {

    private int    driverId;
    private String driverName;
    private String mobileNumber;
    private String email;
    private String vehicleNumbers;
    private int    totalTrips;

    public DriverCompanySummary() {}

    public DriverCompanySummary(int driverId, String driverName, String mobileNumber,
                                String email, String vehicleNumbers, int totalTrips) {
        this.driverId       = driverId;
        this.driverName     = driverName;
        this.mobileNumber   = mobileNumber;
        this.email          = email;
        this.vehicleNumbers = vehicleNumbers;
        this.totalTrips     = totalTrips;
    }

    // --- Getters ---
    public int    getDriverId()       { return driverId; }
    public String getDriverName()     { return driverName; }
    public String getMobileNumber()   { return mobileNumber; }
    public String getEmail()          { return email; }
    public String getVehicleNumbers() { return vehicleNumbers; }
    public int    getTotalTrips()     { return totalTrips; }

    // --- Setters ---
    public void setDriverId(int driverId)             { this.driverId = driverId; }
    public void setDriverName(String driverName)       { this.driverName = driverName; }
    public void setMobileNumber(String mobileNumber)   { this.mobileNumber = mobileNumber; }
    public void setEmail(String email)                { this.email = email; }
    public void setVehicleNumbers(String vehicleNumbers) { this.vehicleNumbers = vehicleNumbers; }
    public void setTotalTrips(int totalTrips)         { this.totalTrips = totalTrips; }
}
