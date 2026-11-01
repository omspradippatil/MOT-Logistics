# 🔍 Document 4: Step-by-Step Code Walkthrough

This document breaks down **every single Java file** in the project, explaining its exact purpose, imports, public methods, and internal logic.

---

## 1. Application Entry Point: `com.transport.Main`

- **Location:** [`src/com/transport/Main.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/Main.java)
- **Role:** Application launcher.
- **Detailed Execution Sequence:**
  1. **Prints banner:** Outputs application startup information to the console.
  2. **DB Pre-Flight Check:** Calls `DatabaseConnection.testConnection()`. If MySQL is offline or credentials fail, it halts execution and presents an informative `JOptionPane.showMessageDialog` with debugging instructions.
  3. **UI Look and Feel:** Invokes `UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())` so Swing components inherit native macOS/Windows rendering instead of default vintage Metal.
  4. **Thread Hand-off:** Invokes `SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true))` to ensure all GUI operations occur strictly on the Swing **Event Dispatch Thread (EDT)**.

---

## 2. Database Connection: `com.transport.db.DatabaseConnection`

- **Location:** [`src/com/transport/db/DatabaseConnection.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/db/DatabaseConnection.java)
- **Role:** Centralized MySQL connection manager.
- **Key Methods:**
  - `getConnection()`: Loads `com.mysql.cj.jdbc.Driver` dynamically and requests a connection using `DriverManager.getConnection(URL, USER, PASSWORD)`.
  - `testConnection()`: Opens a brief connection inside try-with-resources and checks `!conn.isClosed()`.
- **Design Pattern:** Singleton-like utility with a private constructor so it cannot be instantiated.

---

## 3. Data Models (`com.transport.model.*`)

Model classes encapsulate database records as clean Java objects.

| Class | Database Table | Key Attributes | Notes |
|---|---|---|---|
| [`Admin.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Admin.java) | `admin` | `id`, `username`, `password`, `fullName` | Stores admin session |
| [`Company.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Company.java) | `company` | `id`, `companyName`, `address`, `contactNumber`, `gstNumber` | `toString()` returns `companyName` for JComboBox |
| [`Driver.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Driver.java) | `driver` | `id`, `driverName`, `mobileNumber`, `licenceNumber`, `username`, `password`, `isActive` | `toString()` returns `driverName` for JComboBox |
| [`Vehicle.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Vehicle.java) | `vehicle` | `id`, `registrationNumber`, `vehicleType`, `driverId`, `driverName`, `status` | Contains `driverName` fetched via JOIN |
| [`Trip.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Trip.java) | `trip` | `id`, `tripCode`, `tripDate`, `companyId`, `companyName`, `driverId`, `driverName`, `vehicleId`, `vehicleNumber`, `sourceLocation`, `destination`, `amount`, `remarks` | Full composite trip entity with resolved foreign names |
| [`Invoice.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Invoice.java) | `invoice` | `id`, `invoiceNumber`, `invoiceDate`, `companyId`, `companyName`, `driverId`, `vehicleId`, `billingMonth`, `billingYear`, `totalTrips`, `totalAmount`, `status` | Aggregated invoice representation |
| [`Payment.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/model/Payment.java) | `payment` | `id`, `invoiceId`, `invoiceNumber`, `companyName`, `totalAmount`, `amountReceived`, `remainingAmount`, `paymentDate`, `paymentStatus`, `notes` | Tracks settlement status (`Pending`, `Partially Paid`, `Paid`) |

---

## 4. Data Access Objects (`com.transport.dao.*`)

DAOs encapsulate all SQL statements and bridge MySQL tables with Java models.

### `AdminDAO.java`
- `login(String username, String password)`: Authenticates against `admin` table; returns hydrated `Admin` object or `null`.

### `CompanyDAO.java`
- `addCompany(Company company)`: Executes `INSERT INTO company ...`.
- `updateCompany(Company company)`: Updates address, contact, and GST.
- `deleteCompany(int id)`: Removes company by ID.
- `getAllCompanies()`: Returns `List<Company>` sorted by name.
- `getCompanyById(int id)`: Fetches a single company.

### `DriverDAO.java`
- `login(String username, String password)`: Checks password and ensures `is_active = 1`.
- `addDriver(Driver driver)`: Registers a driver with default `is_active = 1`.
- `updateDriver(Driver driver)`: Edits profile, phone number, and password.
- `setDriverActive(int driverId, boolean active)`: Enables/disables access without deleting trip audit history.
- `getAllDrivers()`: Lists all drivers for the admin management table.
- `getActiveDrivers()`: Filters drivers eligible for vehicle assignment.

### `VehicleDAO.java`
- `addVehicle(Vehicle vehicle)`: Registers new commercial vehicle (Tempo, Pickup, etc.).
- `updateVehicle(Vehicle vehicle)`: Reassigns vehicle or updates registration.
- `deleteVehicle(int id)`: Deletes unlinked vehicle records.
- `getAllVehicles()`: Executes `LEFT JOIN driver` to retrieve both vehicle and driver name in a single pass.
- `getVehiclesByDriver(int driverId)`: Pulls only vehicles assigned to the logged-in driver for the trip form.

### `TripDAO.java` ⭐
- `generateTripCode()`: Uses `COUNT(*) + 1` formatted as `TRIP%03d` (e.g. `TRIP001`, `TRIP002`).
- `addTrip(Trip trip)`: Saves daily trip submitted by driver.
- `updateTrip(Trip trip)`: Admin correction of date, destination, or amount.
- `deleteTrip(int id)`: Removes accidental duplicate trips.
- `getTripsForInvoice(...)`: Dynamic query fetching trips for a company in a specified month and year.
- `getTripsByFilter(...)`: Multi-field filter query supporting driver, company, month, and year.
- `getCurrentMonthTotal()`: Computes gross revenue for current month using `MONTH(CURDATE())`.
- `getTotalTripCount()`: Total trips recorded since inception.

### `InvoiceDAO.java`
- `generateInvoiceNumber()`: Auto-increments formatted sequence like `INV-0001`.
- `saveInvoice(Invoice invoice)`: Records monthly billing invoice.
- `getLastInsertedInvoiceId()`: Returns the auto-increment ID to link into `payment`.
- `getAllInvoices()`: Historical invoice log with company and driver metadata.

### `PaymentDAO.java`
- `createPayment(Payment payment)`: Auto-creates a `Pending` payment row when an invoice is generated.
- `updatePayment(Payment payment)`: Updates amount collected, updates remaining balance, and sets status to `Partially Paid` or `Paid`.
- `getAllPayments()`: Full financial receivable ledger.
- `getTotalPendingAmount()`: Sum of all outstanding receivables for the dashboard.

---

## 5. User Interface Classes (`com.transport.ui.*`)

### `LoginFrame.java`
- Dual-tab authentication (`JTabbedPane`).
- Redirects authenticated admins to `AdminDashboard` and drivers to `DriverDashboard`.

### `AdminDashboard.java`
- Houses 7 navigation tabs:
  1. **Dashboard:** Displays 6 real-time stat cards (Companies, Drivers, Vehicles, Total Trips, Month Earnings, Pending Balance).
  2. **Companies:** Embedded `CompanyPanel`.
  3. **Drivers:** Embedded `DriverPanel`.
  4. **Vehicles:** Embedded `VehiclePanel`.
  5. **Trip History:** Embedded `TripHistoryPanel(isAdmin = true, driverId = -1)`.
  6. **Invoice:** Embedded `InvoicePanel`.
  7. **Payments:** Embedded `PaymentPanel`.

### `DriverDashboard.java`
- Focused environment for field drivers:
  1. **New Trip Entry:** Embedded `TripEntryPanel(driver)`.
  2. **My Trip History:** Embedded `TripHistoryPanel(isAdmin = false, driverId = driver.getId())`.

### `CompanyPanel.java`, `DriverPanel.java`, `VehiclePanel.java`
- Form area at top (`GridBagLayout`) for Add/Update/Delete operations.
- Data table at center (`JTable` inside `JScrollPane`) with selection listeners that populate form fields on row click.

### `TripEntryPanel.java`
- Auto-generates `tripCode` and locks the field.
- Pulls assigned vehicles for the logged-in driver.
- Validates source, destination, and amount before persisting.

### `TripHistoryPanel.java`
- Dynamic search bar with Month, Year, Driver, and Company filters.
- Reused by both Admin (who sees all drivers and can delete) and Driver (who only sees their own trips).

### `InvoicePanel.java`
- Generates monthly billing.
- Aggregates trip amounts, previews trip rows in a `JTable`, saves to the database, and enables one-click text/PDF export.

### `PaymentPanel.java`
- Color-coded JTable: Green for Paid, Yellow for Partially Paid, Red for Pending.
- Form to record payments and recalculate remaining balance automatically.

---

## 6. Utilities (`com.transport.util.*`)

### `Validation.java`
- `isEmpty(String text)`
- `isValidMobile(String mobile)`: Validates 10-digit Indian numbers starting with 6, 7, 8, or 9.
- `isValidAmount(String amount)`: Validates positive numeric amounts.
- `isValidVehicleNumber(String regNo)`: Checks vehicle plate formatting.
- `isValidGST(String gst)`: Validates 15-character GST numbers.

### `InvoiceGenerator.java`
- Generates print-ready text invoices and formatted files directly to the `invoices/` directory.
- Includes header, client info, billing period, tabulated trip ledger, total calculations, and dual signature blocks.
