package com.transport.model;

/**
 * Admin.java - Model class for Company Administrator
 *
 * Represents a Company Admin session in MOT TRANS.
 * In this multi-tenant architecture, each company has its own independent admin.
 */
public class Admin {

    private int    id;              // Company ID
    private int    companyId;       // Explicit alias for company ID
    private String companyName;     // e.g., "Om Sai", "Oracle"
    private String adminName;       // e.g., "Rajesh Patil"
    private String email;           // Login email
    private String username;        // Login username
    private String password;
    private String contactNumber;
    private String address;
    private String gstNumber;

    public Admin() {}

    public Admin(int id, String companyName, String adminName, String email,
                 String username, String password, String contactNumber,
                 String address, String gstNumber) {
        this.id            = id;
        this.companyId     = id;
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
    public int    getCompanyId()     { return companyId > 0 ? companyId : id; }
    public String getCompanyName()   { return companyName; }
    public String getAdminName()     { return adminName; }
    public String getFullName()      { return adminName != null ? adminName : companyName; }
    public String getEmail()         { return email; }
    public String getUsername()      { return username; }
    public String getPassword()      { return password; }
    public String getContactNumber() { return contactNumber; }
    public String getAddress()       { return address; }
    public String getGstNumber()     { return gstNumber; }

    // --- Setters ---
    public void setId(int id)                       { this.id = id; this.companyId = id; }
    public void setCompanyId(int companyId)         { this.companyId = companyId; this.id = companyId; }
    public void setCompanyName(String companyName)  { this.companyName = companyName; }
    public void setAdminName(String adminName)      { this.adminName = adminName; }
    public void setFullName(String name)            { this.adminName = name; }
    public void setEmail(String email)              { this.email = email; }
    public void setUsername(String username)        { this.username = username; }
    public void setPassword(String password)        { this.password = password; }
    public void setContactNumber(String contact)    { this.contactNumber = contact; }
    public void setAddress(String address)          { this.address = address; }
    public void setGstNumber(String gstNumber)      { this.gstNumber = gstNumber; }

    @Override
    public String toString() {
        return companyName + " Admin (" + adminName + ")";
    }
}
