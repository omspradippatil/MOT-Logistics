package com.transport.model;

/**
 * Driver.java - Model class for Driver / Vehicle Owner
 *
 * Represents an individual driver or vehicle owner in MOT Logistics.
 * Drivers can own/operate multiple vehicles and work across multiple companies.
 */
public class Driver {

    private int     id;
    private String  driverName;
    private String  mobileNumber;
    private String  email;
    private String  licenceNumber;
    private String  username;
    private String  password;
    private boolean isActive;
    private int     companyId; // Optional link to registered/primary company

    public Driver() {}

    public Driver(int id, String driverName, String mobileNumber, String email,
                  String licenceNumber, String username, String password, boolean isActive) {
        this.id            = id;
        this.driverName    = driverName;
        this.mobileNumber  = mobileNumber;
        this.email         = email;
        this.licenceNumber = licenceNumber;
        this.username      = username;
        this.password      = password;
        this.isActive      = isActive;
    }

    public Driver(int id, String driverName, String mobileNumber, String email,
                  String licenceNumber, String username, String password, boolean isActive, int companyId) {
        this.id            = id;
        this.driverName    = driverName;
        this.mobileNumber  = mobileNumber;
        this.email         = email;
        this.licenceNumber = licenceNumber;
        this.username      = username;
        this.password      = password;
        this.isActive      = isActive;
        this.companyId     = companyId;
    }

    // --- Getters ---
    public int     getId()            { return id; }
    public String  getDriverName()    { return driverName; }
    public String  getMobileNumber()  { return mobileNumber; }
    public String  getEmail()         { return email; }
    public String  getLicenceNumber() { return licenceNumber; }
    public String  getUsername()      { return username; }
    public String  getPassword()      { return password; }
    public boolean isActive()         { return isActive; }
    public int     getCompanyId()     { return companyId; }

    // --- Setters ---
    public void setId(int id)                   { this.id = id; }
    public void setDriverName(String name)      { this.driverName = name; }
    public void setMobileNumber(String mobile)  { this.mobileNumber = mobile; }
    public void setEmail(String email)          { this.email = email; }
    public void setLicenceNumber(String lic)    { this.licenceNumber = lic; }
    public void setUsername(String username)    { this.username = username; }
    public void setPassword(String password)    { this.password = password; }
    public void setActive(boolean active)       { this.isActive = active; }
    public void setCompanyId(int companyId)     { this.companyId = companyId; }

    @Override
    public String toString() {
        return driverName;
    }
}
