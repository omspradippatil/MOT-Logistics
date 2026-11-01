package com.transport.model;

import java.util.Date;

/**
 * Payment.java - Model class for Payment
 *
 * Tracks payment status for each invoice.
 */
public class Payment {

    private int    id;
    private int    invoiceId;
    private String invoiceNumber;   // For display
    private String companyName;     // For display
    private double totalAmount;
    private double amountReceived;
    private double remainingAmount;
    private Date   paymentDate;
    private String paymentStatus;   // Pending / Paid / Partially Paid
    private String notes;

    // Default constructor
    public Payment() {}

    // Getters and Setters
    public int    getId()              { return id; }
    public int    getInvoiceId()       { return invoiceId; }
    public String getInvoiceNumber()   { return invoiceNumber; }
    public String getCompanyName()     { return companyName; }
    public double getTotalAmount()     { return totalAmount; }
    public double getAmountReceived()  { return amountReceived; }
    public double getRemainingAmount() { return remainingAmount; }
    public Date   getPaymentDate()     { return paymentDate; }
    public String getPaymentStatus()   { return paymentStatus; }
    public String getNotes()           { return notes; }

    public void setId(int id)                       { this.id = id; }
    public void setInvoiceId(int id)                { this.invoiceId = id; }
    public void setInvoiceNumber(String num)        { this.invoiceNumber = num; }
    public void setCompanyName(String name)         { this.companyName = name; }
    public void setTotalAmount(double amt)          { this.totalAmount = amt; }
    public void setAmountReceived(double amt)       { this.amountReceived = amt; }
    public void setRemainingAmount(double amt)      { this.remainingAmount = amt; }
    public void setPaymentDate(Date date)           { this.paymentDate = date; }
    public void setPaymentStatus(String status)     { this.paymentStatus = status; }
    public void setNotes(String notes)              { this.notes = notes; }

    @Override
    public String toString() {
        return invoiceNumber + " | " + paymentStatus + " | Rs." + totalAmount;
    }
}
