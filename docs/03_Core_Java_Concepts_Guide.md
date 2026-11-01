# ☕ Document 3: Core Java Concepts Guide

This document explains all the **Core Java Concepts** implemented in this application, complete with real code snippets directly from the project source files.

---

## 1. Classes and Objects

- **Concept:** A **class** is a blueprint/template that defines the state (fields) and behavior (methods). An **object** is an actual instance created in memory using the `new` keyword.
- **Where Used:**
  - `Admin.java`, `Driver.java`, `Company.java`, `Vehicle.java`, `Trip.java`, `Invoice.java`, `Payment.java`
- **Code Example from [`Trip.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Trip.java):**
  ```java
  public class Trip {
      private int id;
      private String tripCode;
      private Date tripDate;
      private double amount;
      // ...
  }
  ```
  And when creating a new trip instance in [`TripEntryPanel.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/ui/TripEntryPanel.java):
  ```java
  Trip trip = new Trip();
  trip.setTripCode(tripCodeField.getText());
  trip.setAmount(amount);
  ```

---

## 2. Constructors (Default and Parameterized)

- **Concept:** A constructor is a special block of code called when an object is created to initialize fields.
  - **Default Constructor:** Accepts no arguments.
  - **Parameterized Constructor:** Accepts arguments to initialize fields directly upon instantiation.
- **Code Example from [`Company.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Company.java):**
  ```java
  // Default constructor
  public Company() {}

  // Parameterized constructor
  public Company(int id, String companyName, String address,
                 String contactNumber, String gstNumber) {
      this.id = id;
      this.companyName = companyName;
      this.address = address;
      this.contactNumber = contactNumber;
      this.gstNumber = gstNumber;
  }
  ```

---

## 3. Encapsulation

- **Concept:** Bundling data (variables) and methods that operate on the data into a single unit, and hiding internal object state using `private` fields with public `getters` and `setters`.
- **Why It Matters:** Prevents external classes from directly corrupting data (e.g., negative amounts or invalid usernames).
- **Code Example from [`Payment.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Payment.java):**
  ```java
  private double totalAmount;
  private double amountReceived;
  private double remainingAmount;

  public double getTotalAmount() {
      return totalAmount;
  }

  public void setTotalAmount(double totalAmount) {
      this.totalAmount = totalAmount;
  }
  ```

---

## 4. Method Overriding (Polymorphism)

- **Concept:** A subclass or child implementation replaces the behavior of a method defined in its superclass (`java.lang.Object` or Swing classes).
- **Where Used:**
  - `@Override toString()` in `Company.java`, `Driver.java`, `Vehicle.java`
  - In Java Swing, `JComboBox<Company>` calls `toString()` on every element to render the dropdown text!
- **Code Example from [`Company.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Company.java):**
  ```java
  @Override
  public String toString() {
      return companyName; // Used by JComboBox to display the company name cleanly
  }
  ```
  - Also used in anonymous inner classes for table cell editing in [`CompanyPanel.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/ui/CompanyPanel.java):
  ```java
  tableModel = new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int col) {
          return false; // Makes all table cells non-editable
      }
  };
  ```

---

## 5. Method Overloading (Compile-Time Polymorphism)

- **Concept:** Defining multiple methods with the **same name** but **different parameter lists** in the same class.
- **Code Example from [`TripDAO.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/dao/TripDAO.java):**
  ```java
  // Overloaded method 1: No arguments, returns all trips
  public List<Trip> getAllTrips() {
      return getTripsByFilter(-1, -1, -1, -1, -1);
  }

  // Overloaded method 2: Filter by a specific driver ID
  public List<Trip> getTripsByDriver(int driverId) {
      return getTripsByFilter(driverId, -1, -1, -1, -1);
  }

  // Overloaded method 3: Full 5-parameter filter
  public List<Trip> getTripsByFilter(int driverId, int companyId, int vehicleId, int month, int year) {
      // dynamic query execution...
  }
  ```

---

## 6. Static Methods & Variables

- **Concept:** The `static` keyword associates a member with the **class itself** rather than with any individual object instance.
- **Where Used:**
  - In [`DatabaseConnection.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/db/DatabaseConnection.java):
    ```java
    public static Connection getConnection() throws SQLException
    public static boolean testConnection()
    ```
  - In [`Validation.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/util/Validation.java):
    ```java
    public static boolean isValidMobile(String mobile)
    public static boolean isValidAmount(String amount)
    public static boolean isValidUsername(String username)
    ```
  - Called anywhere with no instantiation: `Validation.isValidAmount("2500");`

---

## 7. Java Collections Framework (`ArrayList` and `List`)

- **Concept:** `ArrayList<T>` provides resizable, dynamically growing arrays that store objects in insertion order.
- **Where Used:**
  - In every DAO class when returning multiple rows from the database.
- **Code Example from [`DriverDAO.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/dao/DriverDAO.java):**
  ```java
  List<Driver> drivers = new ArrayList<>();
  while (rs.next()) {
      drivers.add(mapRow(rs));
  }
  return drivers;
  ```

---

## 8. Exception Handling (`try-catch-finally` & `try-with-resources`)

- **Concept:** Gracefully detecting and handling runtime failures (such as database connectivity issues or number formatting errors) without crashing the application.
- **Modern Auto-Closable Resource Management (`try-with-resources`):**
  - All DAOs open `Connection` and `PreparedStatement` inside the `try (...)` parentheses.
  - Java automatically closes both resources even if an SQL exception is thrown, preventing connection pool exhaustion and memory leaks.
- **Code Example from [`AdminDAO.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/dao/AdminDAO.java):**
  ```java
  try (Connection conn = DatabaseConnection.getConnection();
       PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);
      stmt.setString(2, password);
      ResultSet rs = stmt.executeQuery();
      // ...
  } catch (SQLException e) {
      System.out.println("AdminDAO.login error: " + e.getMessage());
  }
  ```

---

## 9. JDBC (Java Database Connectivity)

- **Concept:** Standard Java API (`java.sql.*`) to connect to relational databases, execute queries, and process result sets.
- **Components Used:**
  - `DriverManager`: Loads driver and establishes connections.
  - `Connection`: Physical session with MySQL.
  - `PreparedStatement`: Pre-compiled SQL with `?` parameters that protect against SQL Injection.
  - `ResultSet`: Cursor pointing to rows returned by queries.

---

## 10. File Handling and Stream I/O

- **Concept:** Writing generated data directly to the local disk file system using `File`, `FileOutputStream`, and `PrintWriter`.
- **Where Used:** In [`InvoiceGenerator.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/util/InvoiceGenerator.java) to create printable, formatted billing invoices:
  ```java
  try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(filePath)))) {
      writer.println("============================================================");
      writer.println("              SBP KUBRE TEMPO SERVICE");
      writer.println("               TRANSPORT SERVICE INVOICE");
      // ...
      writer.printf("%-6d %-12s %-18s %-18s %12.2f%n", sr++, date, src, dest, amt);
  }
  ```

---

## 11. Date and Time Handling

- **Concept:** Managing calendar dates for trip entries, monthly aggregations, and invoice timestamps using `java.util.Date`, `java.sql.Date`, and `SimpleDateFormat`.
- **Where Used:**
  - Formatting dates for tables: `new SimpleDateFormat("dd/MM/yyyy").format(date)`
  - SQL Date Conversion: `new java.sql.Date(trip.getTripDate().getTime())`
