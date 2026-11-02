package com.transport.model;

/**
 * Company.java - Model class for Client Company
 *
 * Stores corporate client company profiles and their administrative credentials.
 */
public class Company {

    private int    id;
    private String companyName;
    private String adminName;
    private String email;
    private String username;
    private String password;
    private String contactNumber;
    private String address;
    private String gstNumber;

    public Company() {}

    public Company(int id, String companyName, String adminName, String email,
                   String username, String password, String contactNumber,
                   String address, String gstNumber) {
        this.id            = id;
        this.companyName   = companyName;
        this.adminName     = adminName;
        this.email         = email;
        this.username      = username;
        this.password      = password;
        this.contactNumber = contactNumber;
        this.address       = address;
        this.gstNumber     = gstNumber;
    }

    // --- Getters ---
    public int    getId()            { return id; }
    public String getCompanyName()   { return companyName; }
    public String getAdminName()     { return adminName; }
    public String getEmail()         { return email; }
    public String getUsername()      { return username; }
    public String getPassword()      { return password; }
    public String getContactNumber() { return contactNumber; }
    public String getAddress()       { return address; }
    public String getGstNumber()     { return gstNumber; }

    // --- Setters ---
    public void setId(int id)                   { this.id = id; }
    public void setCompanyName(String name)     { this.companyName = name; }
    public void setAdminName(String adminName)  { this.adminName = adminName; }
    public void setEmail(String email)          { this.email = email; }
    public void setUsername(String username)    { this.username = username; }
    public void setPassword(String password)    { this.password = password; }
    public void setContactNumber(String number) { this.contactNumber = number; }
    public void setAddress(String address)      { this.address = address; }
    public void setGstNumber(String gst)        { this.gstNumber = gst; }

    @Override
    public String toString() {
        return companyName;
    }
}
