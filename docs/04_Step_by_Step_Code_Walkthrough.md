# Document 04: Step-by-Step Code Walkthrough

This document explains every package, file, class, and core method across the codebase in clear, accessible Core Java terminology.

---

## 1. Package: `com.transport`

### `Main.java`
- **Purpose:** Entry point of the entire application (`public static void main(String[] args)`).
- **Execution Flow:**
  1. Sets modern cross-platform Look and Feel (`UIManager.setLookAndFeel`).
  2. Verifies MySQL database connectivity via `DatabaseConnection.testConnection()`.
  3. Launches `LoginFrame` on the Swing Event Dispatch Thread (`SwingUtilities.invokeLater`).

---

## 2. Package: `com.transport.model`
POJO (Plain Old Java Object) entity models using standard **Encapsulation** (private fields, public getters, and public setters).

| Class | Primary Responsibility |
|---|---|
| `Admin.java` | Represents a logged-in Company Administrator session, holding company ID, company name, admin contact, and credentials. |
| `Company.java` | Model representing a corporate client company profile. |
| `Driver.java` | Model representing an independent commercial driver or vehicle owner. |
| `DriverCompanySummary.java` | Data Transfer Object (DTO) capturing driver name, vehicle plates used, and trip count for a specific company. |
| `Vehicle.java` | Commercial fleet vehicle model with registration number, vehicle type, and driver assignment. |
| `Trip.java` | Daily operational activity model containing trip ID, date, company ID, driver ID, vehicle ID, route, goods description, amount, and status. |
| `Invoice.java` | Monthly transport invoice model supporting both Company-wide billing and Driver-wise statements. |
| `Payment.java` | Financial settlement model recording received amounts, remaining balances, payment dates, and settlement status. |

---

## 3. Package: `com.transport.dao` (Data Access Objects)
Handles all relational database transactions using parameterized JDBC `PreparedStatement` to ensure security against SQL injection.

### `AdminDAO.java`
- `login(String usernameOrEmail, String password)`: Authenticates against the `company` table using username or email and returns a fully populated `Admin` tenant instance.
- `registerCompany(Company c)`: Inserts a newly registered company into the database.
- `updateCompanyProfile(Admin a)`: Updates company contact details, address, and GST number.
- `resetPassword(String usernameOrEmail, String newPassword)`: Recovers and updates password for forgot-password workflows.

### `DriverDAO.java`
- `login(String usernameOrEmail, String password)`: Authenticates driver accounts against `driver` table where `is_active = 1`.
- `registerDriverWithVehicle(Driver d, String regNo, String type)`: Atomic multi-table database transaction inserting a driver and their primary vehicle.
- `getDriversForCompany(int companyId)`: SQL aggregation query returning all drivers who completed trips for that company along with their vehicle plates and total trips.
- `updateProfile(int driverId, String mobile, String email, String password)`: Self-service profile updates.

### `VehicleDAO.java`
- `getVehiclesByDriver(int driverId)`: Retrieves commercial vehicles owned or operated by a specific driver.
- `getVehiclesByCompany(int companyId)`: Queries distinct vehicles that have hauled cargo for a specific company.
- `addVehicle(Vehicle v)`: Registers a new vehicle under a driver's fleet.

### `TripDAO.java`
- `generateTripCode()`: Auto-generates sequential IDs (e.g., `TRIP001`, `TRIP002`).
- `addTrip(Trip t)`: Persists daily trips with cargo goods description and status.
- `getTripsByCompany(int companyId, int driverId, int vehicleId, int month, int year, String search)`: Strictly company-scoped query ensuring no cross-company data leakage.
- `getTripsByDriver(int driverId, int companyId, int vehicleId, int month, int year, String search)`: Driver-scoped query across all or specific companies.
- `getCompanyStats(int companyId)`: Aggregates trips, drivers, vehicles, monthly billing, paid amounts, and pending balances for that specific company.
- `getDriverStats(int driverId)`: Aggregates driver trips, companies worked with, total lifetime earnings, and pending balances.

### `InvoiceDAO.java`
- `generateInvoiceNumber()`: Generates sequential invoice identifiers (e.g., `INV-2026-001`).
- `saveInvoice(Invoice inv)`: Inserts company or driver monthly invoices.
- `recordPayment(int invoiceId, int companyId, double amount, String notes, Date date)`: Updates invoice `amount_paid`, recalculates `remaining_amount`, updates status (`Paid` / `Partially Paid` / `Pending`), and logs transaction in `payment` table.
- `getInvoicesByCompany(int companyId)`: Returns invoices for the company admin.
- `getInvoicesByDriver(int driverId)`: Returns invoices for the driver portal.

### `PaymentDAO.java`
- `getPaymentsByCompany(int companyId)`: Company-scoped payment audit records.
- `getPendingAmountByCompany(int companyId)`: Calculates total unpaid receivables.

---

## 4. Package: `com.transport.util`

### `InvoiceGenerator.java`
- **Pure Core Java PDF 1.4 Engine:** Uses native byte streaming (`ByteArrayOutputStream`) to construct standards-compliant PDF 1.4 binary objects, dictionaries, pages, fonts, xref cross-reference tables, and trailers. Zero external libraries required.
- `generatePDF(Invoice invoice, List<Trip> trips, String filePath)`: Multi-page monthly transport invoice with itemized table, cargo descriptions, totals, and signatures.
- `generateTripReceiptPDF(Trip trip, String filePath)`: Official single-trip delivery receipt and cash voucher.
- `generateTripsSummaryPDF(List<Trip> trips, String title, String subtitle, String filePath)`: Consolidated audit reports.

### `Validation.java`
- Static input sanitization routines: `isValidMobile`, `isValidEmail`, `isValidAmount`, `isValidVehicleNumber`, `isValidUsername`, `isValidPassword`, and `isValidGST`.

---

## 5. Package: `com.transport.ui` (Java Swing Interface)

### UI Foundations
- `UITheme.java`: Central design system defining color palettes (Deep Navy, Slate Gray, Emerald Green, Indigo Accent), fonts, table stylers, and card borders.
- `ModernButton.java`: Custom `JButton` overriding `paintComponent` with anti-aliasing, rounded borders, and state-aware hover effects.
- `Refreshable.java`: Interface triggering data reload across tabs when switched.

### Authentication Screen
- `LoginFrame.java`: Two distinct tabbed workflows: **Company Admin** and **Driver / Vehicle Owner**. Includes self-service registration and password reset dialogs.

### Company Admin Workstation (`AdminDashboard.java`)
- **Dashboard:** Company-specific metrics cards and recent operational trips table.
- `CompanyDriversPanel.java`: Roster of drivers for this company (`Driver Name | Vehicle Number | Total Trips`). Includes a prominent **"➕ Add Driver to Company"** button.
- `AddDriverToCompanyDialog.java`: Interactive dialog enabling Company Admins to register a brand new commercial driver and vehicle linked to their company, or assign an active fleet driver.
- `DriverTripsDetailDialog.java`: Displays ONLY trips completed by the selected driver for this company with a prominent **Download Invoice** button.
- `CompanyTripManagementPanel.java`: Company trips ledger with driver, vehicle, month, year, and location search filters.
- `CompanyMonthlyInvoicePanel.java`: Month/Year selector, real-time trip preview, monthly invoice generation, and PDF download.
- `CompanyPaymentManagementPanel.java`: Invoice receivables tracking and payment settlement dialog.
- `CompanyProfilePanel.java`: Company profile and password management.

### Driver & Vehicle Owner Portal (`DriverDashboard.java`)
- **Dashboard:** Lifetime earnings, current month trips, companies worked with, and payment statuses.
- `TripEntryPanel.java`: Daily trip entry with company dropdown, vehicle dropdown, route, cargo description, amount, and remarks.
- `DriverTripHistoryPanel.java`: Complete cross-company trips history with receipt PDF export.
- `DriverVehiclesPanel.java`: View registered fleet and add new commercial vehicles.
- `DriverMonthlyInvoicesPanel.java`: Company + Month selector, trip earnings preview, driver invoice generation, and PDF download.
- `DriverProfilePanel.java`: Self-service driver contact details and password updates.
