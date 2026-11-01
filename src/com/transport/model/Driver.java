package com.transport.model;

/**
 * Driver.java - Model class for Driver
 *
 * Represents a driver who logs trips in the system.
 * Uses ENCAPSULATION.
 */
public class Driver {

    private int     id;
    private String  driverName;
    private String  mobileNumber;
    private String  licenceNumber;
    private String  username;
    private String  password;
    private boolean isActive;

    // Default constructor
    public Driver() {}

    // Full constructor
    public Driver(int id, String driverName, String mobileNumber,
                  String licenceNumber, String username,
                  String password, boolean isActive) {
        this.id            = id;
        this.driverName    = driverName;
        this.mobileNumber  = mobileNumber;
        this.licenceNumber = licenceNumber;
        this.username      = username;
        this.password      = password;
        this.isActive      = isActive;
    }

    // --- Getters ---
    public int     getId()            { return id; }
    public String  getDriverName()    { return driverName; }
    public String  getMobileNumber()  { return mobileNumber; }
    public String  getLicenceNumber() { return licenceNumber; }
    public String  getUsername()      { return username; }
    public String  getPassword()      { return password; }
    public boolean isActive()         { return isActive; }

    // --- Setters ---
    public void setId(int id)                   { this.id = id; }
    public void setDriverName(String name)      { this.driverName = name; }
    public void setMobileNumber(String mobile)  { this.mobileNumber = mobile; }
    public void setLicenceNumber(String lic)    { this.licenceNumber = lic; }
    public void setUsername(String username)    { this.username = username; }
    public void setPassword(String password)    { this.password = password; }
    public void setActive(boolean active)       { this.isActive = active; }

    // Used by JComboBox to display driver name
    @Override
    public String toString() {
        return driverName;
    }
}
