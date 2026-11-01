package com.transport.model;

import java.util.Date;

/**
 * Trip.java - Model class for a Trip record
 *
 * Represents one daily trip entered by a driver.
 * This is the CORE data entity of the system.
 */
public class Trip {

    private int    id;
    private String tripCode;           // e.g., TRIP001
    private Date   tripDate;
    private int    companyId;
    private String companyName;        // For display (joined)
    private int    driverId;
    private String driverName;         // For display (joined)
    private int    vehicleId;
    private String vehicleNumber;      // For display (joined)
    private String sourceLocation;
    private String destination;
    private double amount;
    private String remarks;

    // Default constructor
    public Trip() {}

    // Full constructor
    public Trip(int id, String tripCode, Date tripDate,
                int companyId, String companyName,
                int driverId, String driverName,
                int vehicleId, String vehicleNumber,
                String sourceLocation, String destination,
                double amount, String remarks) {
        this.id             = id;
        this.tripCode       = tripCode;
        this.tripDate       = tripDate;
        this.companyId      = companyId;
        this.companyName    = companyName;
        this.driverId       = driverId;
        this.driverName     = driverName;
        this.vehicleId      = vehicleId;
        this.vehicleNumber  = vehicleNumber;
        this.sourceLocation = sourceLocation;
        this.destination    = destination;
        this.amount         = amount;
        this.remarks        = remarks;
    }

    // --- Getters ---
    public int    getId()              { return id; }
    public String getTripCode()        { return tripCode; }
    public Date   getTripDate()        { return tripDate; }
    public int    getCompanyId()       { return companyId; }
    public String getCompanyName()     { return companyName; }
    public int    getDriverId()        { return driverId; }
    public String getDriverName()      { return driverName; }
    public int    getVehicleId()       { return vehicleId; }
    public String getVehicleNumber()   { return vehicleNumber; }
    public String getSourceLocation()  { return sourceLocation; }
    public String getDestination()     { return destination; }
    public double getAmount()          { return amount; }
    public String getRemarks()         { return remarks; }

    // --- Setters ---
    public void setId(int id)                       { this.id = id; }
    public void setTripCode(String tripCode)        { this.tripCode = tripCode; }
    public void setTripDate(Date tripDate)          { this.tripDate = tripDate; }
    public void setCompanyId(int companyId)         { this.companyId = companyId; }
    public void setCompanyName(String name)         { this.companyName = name; }
    public void setDriverId(int driverId)           { this.driverId = driverId; }
    public void setDriverName(String name)          { this.driverName = name; }
    public void setVehicleId(int vehicleId)         { this.vehicleId = vehicleId; }
    public void setVehicleNumber(String number)     { this.vehicleNumber = number; }
    public void setSourceLocation(String source)    { this.sourceLocation = source; }
    public void setDestination(String dest)         { this.destination = dest; }
    public void setAmount(double amount)            { this.amount = amount; }
    public void setRemarks(String remarks)          { this.remarks = remarks; }

    @Override
    public String toString() {
        return "Trip{tripCode='" + tripCode + "', from='" + sourceLocation
                + "', to='" + destination + "', amount=" + amount + "}";
    }
}
