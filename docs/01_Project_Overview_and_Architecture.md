# Document 01: Project Overview & Multi-Tenant Architecture

## 1. Project Introduction
**MOT LOGISTICS** is an enterprise-grade desktop Transport Trip Management and Automated Monthly Billing System built entirely in **Core Java** (Swing GUI + JDBC + MySQL). 

The software eliminates manual notebooks, paper registers, and handwritten billing for transport and logistics businesses that supply commercial fleets (tempos, pickup vans, trucks, heavy trailers) to various corporate client companies.

---

## 2. Actual Business Model & Multi-Tenancy

In the transport sector, independent drivers and vehicle owners provide freight transportation services across multiple client companies:
- **Om Sai** (Industrial & Manufacturing in Wada / MIDC)
- **Oracle** (Corporate IT & Data Center Infrastructure in Thane / Mumbai)
- **Company XYZ** (Logistics & Super Hub Distribution in Bhiwandi)

### Key Architectural Rule: Multi-Tenant Company Isolation
1. **Company Admin Login:** Each company is an independent tenant with its own credentials (`username` or `email` + `password`).
2. **Strict Isolation:** When Om Sai logs in, the dashboard and reports show **only** Om Sai records. Om Sai can **never** see Oracle or Company XYZ records.
3. **Cross-Company Driver Portability:** Independent drivers (Rahul, Amit, Sagar) can work for multiple companies. When Rahul logs in, he can see his trips for Om Sai as well as his trips for Oracle, but he cannot see Amit's or Sagar's records.

---

## 3. Two Types of User Roles

### Role A: Company Admin Login
- **Login Credentials:** Username or Email (`omsai`/`omsai123`, `oracle`/`oracle123`, `xyz`/`xyz123`).
- **Dashboard Sections:**
  1. **Dashboard:** Total trips completed for company, total drivers engaged, total vehicles employed, current month billing, paid invoices, pending receivables.
  2. **Drivers:** Roster of drivers who have hauled goods for this company with vehicle numbers and total trip counts. Double-clicking any driver opens their trip records for this company with a prominent **Download Invoice** button.
  3. **Trip Management:** Comprehensive company trips audit ledger with filters (driver, vehicle, month, year, search route) and PDF export.
  4. **Monthly Invoice:** Real-time billing calculation, trip preview, automated monthly invoice generation, and instant PDF download.
  5. **Payment Management:** Invoices settlement tracking (Total, Paid, Remaining, Status: Paid / Partially Paid / Pending) with a payment recording dialog.
  6. **Company Profile:** Manage company profile, address, GSTIN, contact details, and password.

### Role B: Driver / Vehicle Owner Login
- **Login Credentials:** Username or Email (`rahul`/`rahul123`, `amit`/`amit123`, `sagar`/`sagar123`).
- **Dashboard Sections:**
  1. **Dashboard:** Total trips completed, total companies worked with, lifetime earnings, current month trips, completed payments, pending balance.
  2. **Add New Trip:** Daily trip entry form (Company dropdown, Vehicle dropdown, date, route, goods description, amount, remarks) with auto-generated sequential Trip ID.
  3. **My Trip History:** Complete trip log across all companies with multi-filter search, individual trip receipt PDF export, and full statement PDF download.
  4. **My Vehicles:** View and register multiple commercial vehicles owned or operated by the driver.
  5. **My Monthly Invoices:** Select client company + billing period, view trips completed for that company, see total payable amount, generate driver-wise invoice, and download PDF.
  6. **Profile & Settings:** Self-service mobile, email, and password management.

---

## 4. Technology Stack & Design Constraints

| Component | Technology | Rationale |
|---|---|---|
| Programming Language | Core Java (JDK 8+) | Easiest to read, zero third-party framework overhead |
| User Interface | Java Swing (`javax.swing`, `java.awt`) | Native desktop application, responsive high-contrast UI |
| Database | MySQL 8.x | High-performance relational database with ACID compliance |
| DB Connectivity | JDBC (`java.sql.*`) | Native prepared statements preventing SQL injection |
| PDF Generation | Native PDF 1.4 Binary Stream Engine | Pure Core Java (zero third-party JAR dependencies) |
| Architecture Pattern | Layered MVC + DAO Pattern | Clean separation between Model, Data Access, and View |

> **Strict Architectural Constraint:** No Spring Boot, Hibernate, JSP, Servlets, or web frameworks are used. Everything is written using foundational Core Java concepts.
