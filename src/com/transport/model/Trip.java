package com.transport.model;

import java.util.Date;

/**
 * Trip.java - Model class for Daily Operational Trip
 *
 * Represents a single transport trip logged by a driver.
 * Includes company, driver, vehicle, route, cargo goods description, amount, and status.
 */
public class Trip {

    private int    id;
    private String tripCode;           // e.g., TRIP001
    private Date   tripDate;
    private int    companyId;
    private String companyName;        // Joined from company
    private int    driverId;
    private String driverName;         // Joined from driver
    private int    vehicleId;
    private String vehicleNumber;      // Joined from vehicle
    private String sourceLocation;
    private String destination;
    private String goodsDescription;   // Cargo / Container / Material description
    private double amount;
    private String tripStatus;         // e.g., Completed, In Transit, Cancelled
    private String remarks;

    public Trip() {
        this.tripStatus = "Completed";
    }

    public Trip(int id, String tripCode, Date tripDate,
                int companyId, String companyName,
                int driverId, String driverName,
                int vehicleId, String vehicleNumber,
                String sourceLocation, String destination,
                String goodsDescription, double amount,
                String tripStatus, String remarks) {
        this.id               = id;
        this.tripCode         = tripCode;
        this.tripDate         = tripDate;
        this.companyId        = companyId;
        this.companyName      = companyName;
        this.driverId         = driverId;
        this.driverName       = driverName;
        this.vehicleId        = vehicleId;
        this.vehicleNumber    = vehicleNumber;
        this.sourceLocation   = sourceLocation;
        this.destination      = destination;
        this.goodsDescription = goodsDescription;
        this.amount           = amount;
        this.tripStatus       = tripStatus != null ? tripStatus : "Completed";
        this.remarks          = remarks;
    }

    // --- Getters ---
    public int    getId()               { return id; }
    public String getTripCode()         { return tripCode; }
    public Date   getTripDate()         { return tripDate; }
    public int    getCompanyId()        { return companyId; }
    public String getCompanyName()      { return companyName; }
    public int    getDriverId()         { return driverId; }
    public String getDriverName()       { return driverName; }
    public int    getVehicleId()        { return vehicleId; }
    public String getVehicleNumber()    { return vehicleNumber; }
    public String getSourceLocation()   { return sourceLocation; }
    public String getDestination()      { return destination; }
    public String getGoodsDescription() { return goodsDescription; }
    public double getAmount()           { return amount; }
    public String getTripStatus()       { return tripStatus; }
    public String getRemarks()          { return remarks; }

    // --- Setters ---
    public void setId(int id)                               { this.id = id; }
    public void setTripCode(String tripCode)                 { this.tripCode = tripCode; }
    public void setTripDate(Date tripDate)                   { this.tripDate = tripDate; }
    public void setCompanyId(int companyId)                  { this.companyId = companyId; }
    public void setCompanyName(String name)                  { this.companyName = name; }
    public void setDriverId(int driverId)                    { this.driverId = driverId; }
    public void setDriverName(String name)                   { this.driverName = name; }
    public void setVehicleId(int vehicleId)                  { this.vehicleId = vehicleId; }
    public void setVehicleNumber(String number)              { this.vehicleNumber = number; }
    public void setSourceLocation(String source)             { this.sourceLocation = source; }
    public void setDestination(String dest)                  { this.destination = dest; }
    public void setGoodsDescription(String goodsDescription) { this.goodsDescription = goodsDescription; }
    public void setAmount(double amount)                     { this.amount = amount; }
    public void setTripStatus(String tripStatus)             { this.tripStatus = tripStatus; }
    public void setRemarks(String remarks)                   { this.remarks = remarks; }

    @Override
    public String toString() {
        return tripCode + " | " + sourceLocation + " -> " + destination + " (₹" + amount + ")";
    }
}
