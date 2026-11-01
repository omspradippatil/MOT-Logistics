package com.transport.model;

import java.util.Date;

/**
 * Invoice.java - Model class for Invoice
 *
 * Represents a generated monthly invoice.
 */
public class Invoice {

    private int    id;
    private String invoiceNumber;
    private Date   invoiceDate;
    private int    companyId;
    private String companyName;
    private int    driverId;
    private String driverName;
    private int    vehicleId;
    private String vehicleNumber;
    private int    billingMonth;
    private int    billingYear;
    private int    totalTrips;
    private double totalAmount;
    private String status;

    // Default constructor
    public Invoice() {}

    // Getters and Setters
    public int    getId()             { return id; }
    public String getInvoiceNumber()  { return invoiceNumber; }
    public Date   getInvoiceDate()    { return invoiceDate; }
    public int    getCompanyId()      { return companyId; }
    public String getCompanyName()    { return companyName; }
    public int    getDriverId()       { return driverId; }
    public String getDriverName()     { return driverName; }
    public int    getVehicleId()      { return vehicleId; }
    public String getVehicleNumber()  { return vehicleNumber; }
    public int    getBillingMonth()   { return billingMonth; }
    public int    getBillingYear()    { return billingYear; }
    public int    getTotalTrips()     { return totalTrips; }
    public double getTotalAmount()    { return totalAmount; }
    public String getStatus()         { return status; }

    public void setId(int id)                       { this.id = id; }
    public void setInvoiceNumber(String num)        { this.invoiceNumber = num; }
    public void setInvoiceDate(Date date)           { this.invoiceDate = date; }
    public void setCompanyId(int id)                { this.companyId = id; }
    public void setCompanyName(String name)         { this.companyName = name; }
    public void setDriverId(int id)                 { this.driverId = id; }
    public void setDriverName(String name)          { this.driverName = name; }
    public void setVehicleId(int id)                { this.vehicleId = id; }
    public void setVehicleNumber(String num)        { this.vehicleNumber = num; }
    public void setBillingMonth(int month)          { this.billingMonth = month; }
    public void setBillingYear(int year)            { this.billingYear = year; }
    public void setTotalTrips(int trips)            { this.totalTrips = trips; }
    public void setTotalAmount(double amount)       { this.totalAmount = amount; }
    public void setStatus(String status)            { this.status = status; }

    @Override
    public String toString() {
        return invoiceNumber + " | " + companyName + " | " + billingMonth + "/" + billingYear;
    }
}
