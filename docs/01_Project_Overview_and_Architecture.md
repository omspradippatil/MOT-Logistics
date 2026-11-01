# 🚛 Document 1: Project Overview & Architecture

## Transport Trip Management & Automated Invoice Generation System
**Developed with Core Java, Java Swing, and MySQL**

---

## 1. Executive Summary

Transport businesses operating commercial vehicle fleets (such as tempos, pickup vans, trucks, and trailers) routinely face logistical headaches when tracking daily trips, managing multiple client companies, coordinating drivers, and calculating monthly billing.

Historically, small and medium transport enterprises manage their daily operations using:
- Physical notebooks, paper logbooks, and trip registers.
- Manual trip receipts and handwritten carbon-copy vouchers.
- Mental math or manual pocket calculators at month-end to generate invoices.

### Problems with the Manual Process
1. **Lost Records & Calculation Errors:** Loose sheets get misplaced, and human error in tallying amounts leads to billing disputes with clients.
2. **Slow Billing Cycles:** Manually compiling 30 days of trips for dozens of vehicles takes days, delaying payment collection.
3. **Lack of Central Visibility:** Business owners cannot readily see who has paid, who owes money, or which vehicles are generating the most revenue.

### The Digital Solution
This application computerizes the entire operational lifecycle:
- **Drivers** log in from any terminal or workstation, select their vehicle, specify source, destination, and fare amount, and save trip records in seconds.
- **Admin** has a single centralized dashboard displaying registered companies, active drivers, vehicles, monthly gross earnings, and outstanding payment balances.
- **One-Click Invoice Engine:** At month end, selecting a company, month, and year automatically aggregates all eligible trips, calculates the exact billing total, prepares an invoice ledger, and exports a print-ready formatted invoice.

---

## 2. System Architecture & High-Level Flow

The project follows a clean **Three-Tier Architecture** without relying on complex frameworks (no Spring Boot, Hibernate, JSP, or Servlets), making it simple to understand, modify, and maintain:

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│                       Java Swing                       │
│  (LoginFrame, AdminDashboard, DriverDashboard, Panels) │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                    BUSINESS & DAO                      │
│             Data Access Objects & Models               │
│  (AdminDAO, DriverDAO, CompanyDAO, VehicleDAO, TripDAO)│
│  (InvoiceDAO, PaymentDAO, InvoiceGenerator, Validation)│
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼ JDBC
┌────────────────────────────────────────────────────────┐
│                    PERSISTENCE LAYER                   │
│                     MySQL Database                     │
│    (admin, company, driver, vehicle, trip, invoice)    │
└────────────────────────────────────────────────────────┘
```

---

## 3. Technology Stack Breakdown

| Layer | Technology | Version / Specification | Justification |
|---|---|---|---|
| **Language** | Core Java | JDK 8 to JDK 27 compatible | Pure standard library, readable OOP code |
| **User Interface** | Java Swing & AWT | Standard Java Desktop GUI | Native cross-platform desktop UI without web server |
| **Database** | MySQL | MySQL 5.7+ / 8.0+ / 9.x | Robust relational database with ACID guarantees |
| **Connectivity** | JDBC | `mysql-connector-j` 8.x | Direct PreparedStatement interaction, prevents SQL injection |
| **Document Output** | Plain Text / OpenPDF | Stream-based File I/O | Instant text ledger generation with optional PDF support |
| **Build & Run** | Shell Scripts | `build.sh` & `run.sh` | One-command compilation and launch on macOS/Linux |

---

## 4. Key Functional Modules

```
                    ┌─────────────────────────┐
                    │      Login Screen       │
                    └────────────┬────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
       [Admin Authentication]          [Driver Authentication]
                 │                               │
                 ▼                               ▼
       ┌───────────────────┐           ┌───────────────────┐
       │  Admin Dashboard  │           │ Driver Dashboard  │
       └─────────┬─────────┘           └─────────┬─────────┘
                 │                               │
 ┌───────────────┼───────────────┐       ┌───────┴───────┐
 ▼               ▼               ▼       ▼               ▼
Company       Driver          Vehicle  Trip Entry    Trip History
Manager       Manager         Manager  (Realtime)    (Driver's Own)
 │               │               │
 └───────────────┼───────────────┘
                 ▼
       ┌───────────────────┐
       │   Trip History    │
       │ (Filter & Audit)  │
       └─────────┬─────────┘
                 ▼
       ┌───────────────────┐
       │ Invoice Generator │
       │ (Monthly Ledger)  │
       └─────────┬─────────┘
                 ▼
       ┌───────────────────┐
       │ Payment Tracking  │
       │(Paid/Part/Pending)│
       └───────────────────┘
```

1. **Authentication Module:**
   - Dual-tab entry: Admin credentials or Driver credentials.
   - Prevents unauthorized access and scopes permissions.
2. **Company Management Module:**
   - Full CRUD: Add client companies, edit addresses, phone numbers, and GSTIN.
3. **Driver Management Module:**
   - Registration of drivers, license numbers, phone numbers, and login credentials.
   - Ability to activate or suspend driver privileges.
4. **Vehicle Fleet Management Module:**
   - Registry of vehicle types (Tempo, Pickup, Truck, Trailer).
   - Dynamic assignment of vehicle to responsible driver.
5. **Daily Trip Entry Module:**
   - Auto-generated trip codes (e.g., `TRIP001`, `TRIP002`).
   - Auto-fills logged-in driver's information.
   - Restricts vehicle dropdown to the driver's assigned vehicles.
6. **Trip Audit & Search Module:**
   - Filter by Company, Driver, Vehicle, Month, and Year.
   - Admin can edit or delete invalid records.
7. **Automated Monthly Billing Engine:**
   - Automatic aggregation of all trips by company and billing cycle.
   - Total calculation, formatted preview, and file export.
8. **Payment Tracker:**
   - Automatically initializes a payment entry upon invoice generation.
   - Tracks received amount, balance due, payment date, and status.
