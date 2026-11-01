package com.transport.model;

/**
 * Admin.java - Model class for Admin user
 *
 * This class represents an admin in the system.
 * It uses ENCAPSULATION - all fields are private,
 * and we use getters/setters to access them.
 */
public class Admin {

    // Private fields (encapsulation)
    private int id;
    private String username;
    private String password;
    private String fullName;

    // Default constructor
    public Admin() {}

    // Parameterized constructor
    public Admin(int id, String username, String password, String fullName) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    // --- Getters ---
    public int    getId()       { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }

    // --- Setters ---
    public void setId(int id)             { this.id = id; }
    public void setUsername(String u)     { this.username = u; }
    public void setPassword(String p)     { this.password = p; }
    public void setFullName(String name)  { this.fullName = name; }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", username='" + username + "', fullName='" + fullName + "'}";
    }
}
