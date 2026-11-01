# MOT Logistics - Transport Trip Management & Automated Invoice Generation System

A Core Java + Java Swing desktop application for managing transport fleet logistics, recording daily trips, and generating computerized monthly invoices.

## 🚀 Quick Start

### Prerequisites
- Java JDK 8 or higher
- MySQL 5.7 or higher
- MySQL Connector/J (JDBC driver)

### Setup Steps

**1. Database Setup**
```sql
-- Open MySQL and run:
source database/schema.sql
```

**2. Add MySQL JDBC Driver**
- Download `mysql-connector-j-*.jar` from https://dev.mysql.com/downloads/connector/j/
- Copy the JAR file to the `lib/` folder
- Rename it to `mysql-connector-j.jar`

**3. Update Database Password**
Open `src/com/transport/db/DatabaseConnection.java` and change:
```java
private static final String PASSWORD = "";  // ← Enter your MySQL password here
```

**4. Build and Run**
```bash
chmod +x build.sh run.sh
./build.sh
./run.sh
```

## 🔑 Default Login

| Type  | Username | Password |
|-------|----------|----------|
| Admin | admin    | admin123 |
| Driver| rahul    | rahul123 |

## 📁 Project Structure

```
MOT-Logistics/
├── src/
│   └── com/transport/
│       ├── Main.java              ← Start here
│       ├── db/
│       │   └── DatabaseConnection.java
│       ├── model/                 ← Data classes
│       │   ├── Admin.java
│       │   ├── Company.java
│       │   ├── Driver.java
│       │   ├── Vehicle.java
│       │   ├── Trip.java
│       │   ├── Invoice.java
│       │   └── Payment.java
│       ├── dao/                   ← Database operations
│       │   ├── AdminDAO.java
│       │   ├── CompanyDAO.java
│       │   ├── DriverDAO.java
│       │   ├── VehicleDAO.java
│       │   ├── TripDAO.java
│       │   ├── InvoiceDAO.java
│       │   └── PaymentDAO.java
│       ├── ui/                    ← Swing UI windows
│       │   ├── LoginFrame.java
│       │   ├── AdminDashboard.java
│       │   ├── DriverDashboard.java
│       │   ├── CompanyPanel.java
│       │   ├── DriverPanel.java
│       │   ├── VehiclePanel.java
│       │   ├── TripEntryPanel.java
│       │   ├── TripHistoryPanel.java
│       │   ├── InvoicePanel.java
│       │   └── PaymentPanel.java
│       └── util/
│           ├── Validation.java
│           └── InvoiceGenerator.java
├── lib/
│   └── mysql-connector-j.jar     ← Add this yourself
├── database/
│   └── schema.sql                ← Run this in MySQL first
├── docs/                         ← Documentation
├── invoices/                     ← Generated invoices saved here
├── build.sh                      ← Compile script
└── run.sh                        ← Run script
```

## 🎯 Java Concepts Used

| Concept | Where Used |
|---------|-----------|
| Classes & Objects | All model classes |
| Encapsulation | Private fields + getters/setters in models |
| Inheritance | (Extensible DAO structure) |
| Interfaces | (DAO pattern) |
| Exception Handling | All DAO try-catch blocks |
| JDBC | DatabaseConnection + all DAOs |
| Collections (ArrayList) | getAllXxx() methods in DAOs |
| File Handling | InvoiceGenerator |
| Date/Time API | Trip dates, Invoice dates |
| Java Swing | All UI classes |
| Static Methods | Validation, DatabaseConnection |
