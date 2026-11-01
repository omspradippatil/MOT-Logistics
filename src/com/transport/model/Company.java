package com.transport.model;

/**
 * Company.java - Model class for Company
 *
 * Represents a company that uses our transport service.
 * Uses ENCAPSULATION with private fields and public getters/setters.
 */
public class Company {

    private int    id;
    private String companyName;
    private String address;
    private String contactNumber;
    private String gstNumber;

    // Default constructor
    public Company() {}

    // Parameterized constructor
    public Company(int id, String companyName, String address,
                   String contactNumber, String gstNumber) {
        this.id            = id;
        this.companyName   = companyName;
        this.address       = address;
        this.contactNumber = contactNumber;
        this.gstNumber     = gstNumber;
    }

    // --- Getters ---
    public int    getId()            { return id; }
    public String getCompanyName()   { return companyName; }
    public String getAddress()       { return address; }
    public String getContactNumber() { return contactNumber; }
    public String getGstNumber()     { return gstNumber; }

    // --- Setters ---
    public void setId(int id)                   { this.id = id; }
    public void setCompanyName(String name)     { this.companyName = name; }
    public void setAddress(String address)      { this.address = address; }
    public void setContactNumber(String number) { this.contactNumber = number; }
    public void setGstNumber(String gst)        { this.gstNumber = gst; }

    // Used by JComboBox to display company name
    @Override
    public String toString() {
        return companyName;
    }
}
