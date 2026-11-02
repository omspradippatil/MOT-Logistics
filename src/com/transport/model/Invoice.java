package com.transport.model;

import java.util.Date;

/**
 * Invoice.java - Model class for Monthly Transport Invoice
 *
 * Represents both Company-wide monthly invoices and Driver-wise monthly invoices.
 */
public class Invoice {

    private int    id;
    private String invoiceNumber;      // e.g., INV-2026-001
    private Date   invoiceDate;
    private int    companyId;
    private String companyName;        // Joined
    private int    driverId;           // Optional (for driver-wise invoice)
    private String driverName;         // Joined
    private int    vehicleId;          // Optional
    private String vehicleNumber;      // Joined
    private int    billingMonth;       // 1 - 12
    private int    billingYear;        // e.g. 2026
    private int    totalTrips;
    private double totalAmount;
    private double amountPaid;
    private double remainingAmount;
    private String invoiceType;        // "Company" or "Driver"
    private String status;             // "Generated", "Paid", "Partially Paid", "Pending"

    public Invoice() {
        this.invoiceType = "Company";
        this.status      = "Generated";
    }

    public Invoice(int id, String invoiceNumber, Date invoiceDate,
                   int companyId, String companyName,
                   int driverId, String driverName,
                   int vehicleId, String vehicleNumber,
                   int billingMonth, int billingYear,
                   int totalTrips, double totalAmount,
                   double amountPaid, double remainingAmount,
                   String invoiceType, String status) {
        this.id              = id;
        this.invoiceNumber   = invoiceNumber;
        this.invoiceDate     = invoiceDate;
        this.companyId       = companyId;
        this.companyName     = companyName;
        this.driverId        = driverId;
        this.driverName      = driverName;
        this.vehicleId       = vehicleId;
        this.vehicleNumber   = vehicleNumber;
        this.billingMonth    = billingMonth;
        this.billingYear     = billingYear;
        this.totalTrips      = totalTrips;
        this.totalAmount     = totalAmount;
        this.amountPaid      = amountPaid;
        this.remainingAmount = remainingAmount;
        this.invoiceType     = invoiceType != null ? invoiceType : "Company";
        this.status          = status != null ? status : "Generated";
    }

    // --- Getters ---
    public int    getId()              { return id; }
    public String getInvoiceNumber()   { return invoiceNumber; }
    public Date   getInvoiceDate()     { return invoiceDate; }
    public int    getCompanyId()       { return companyId; }
    public String getCompanyName()     { return companyName; }
    public int    getDriverId()        { return driverId; }
    public String getDriverName()      { return driverName; }
    public int    getVehicleId()       { return vehicleId; }
    public String getVehicleNumber()   { return vehicleNumber; }
    public int    getBillingMonth()    { return billingMonth; }
    public int    getBillingYear()     { return billingYear; }
    public int    getTotalTrips()      { return totalTrips; }
    public double getTotalAmount()     { return totalAmount; }
    public double getAmountPaid()      { return amountPaid; }
    public double getRemainingAmount() { return remainingAmount; }
    public String getInvoiceType()     { return invoiceType; }
    public String getStatus()          { return status; }

    // --- Setters ---
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
    public void setAmountPaid(double amountPaid)    { this.amountPaid = amountPaid; }
    public void setRemainingAmount(double rem)      { this.remainingAmount = rem; }
    public void setInvoiceType(String invoiceType)  { this.invoiceType = invoiceType; }
    public void setStatus(String status)            { this.status = status; }

    @Override
    public String toString() {
        return invoiceNumber + " | " + companyName + " (" + billingMonth + "/" + billingYear + ") - ₹" + totalAmount;
    }
}
