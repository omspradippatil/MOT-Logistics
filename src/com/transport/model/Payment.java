package com.transport.model;

import java.util.Date;

/**
 * Payment.java - Model class for Invoice Payment
 *
 * Tracks billing settlements, received payments, remaining balances, and payment statuses.
 */
public class Payment {

    private int    id;
    private int    invoiceId;
    private int    companyId;
    private String invoiceNumber;   // For display
    private String companyName;     // For display
    private double totalAmount;
    private double amountReceived;
    private double remainingAmount;
    private Date   paymentDate;
    private String paymentStatus;   // "Paid", "Partially Paid", "Pending"
    private String notes;

    public Payment() {}

    public Payment(int id, int invoiceId, int companyId, String invoiceNumber,
                   String companyName, double totalAmount, double amountReceived,
                   double remainingAmount, Date paymentDate, String paymentStatus, String notes) {
        this.id              = id;
        this.invoiceId       = invoiceId;
        this.companyId       = companyId;
        this.invoiceNumber   = invoiceNumber;
        this.companyName     = companyName;
        this.totalAmount     = totalAmount;
        this.amountReceived  = amountReceived;
        this.remainingAmount = remainingAmount;
        this.paymentDate     = paymentDate;
        this.paymentStatus   = paymentStatus;
        this.notes           = notes;
    }

    // --- Getters ---
    public int    getId()              { return id; }
    public int    getInvoiceId()       { return invoiceId; }
    public int    getCompanyId()       { return companyId; }
    public String getInvoiceNumber()   { return invoiceNumber; }
    public String getCompanyName()     { return companyName; }
    public double getTotalAmount()     { return totalAmount; }
    public double getAmountReceived()  { return amountReceived; }
    public double getRemainingAmount() { return remainingAmount; }
    public Date   getPaymentDate()     { return paymentDate; }
    public String getPaymentStatus()   { return paymentStatus; }
    public String getNotes()           { return notes; }

    // --- Setters ---
    public void setId(int id)                       { this.id = id; }
    public void setInvoiceId(int id)                { this.invoiceId = id; }
    public void setCompanyId(int companyId)         { this.companyId = companyId; }
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
        return invoiceNumber + " | " + paymentStatus + " | ₹" + totalAmount;
    }
}
