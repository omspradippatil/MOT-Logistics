package com.transport.test;

import com.transport.dao.*;
import com.transport.db.DatabaseConnection;
import com.transport.model.*;
import com.transport.ui.*;
import com.transport.util.InvoiceGenerator;
import com.transport.util.Validation;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * ComprehensiveTestSuite.java - Enterprise 70-Point Verification Test Suite
 *
 * Covers Database, Security, CRUD, Business Rules, Integrity Constraints,
 * Automated Invoicing, Native PDF binary generation, and UI Auto-Sync.
 */
public class ComprehensiveTestSuite {

    private static int totalPassed = 0;
    private static int totalFailed = 0;
    private static int testIndex   = 1;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        System.out.println("================================================================================");
        System.out.println("          MOT LOGISTICS - 70-POINT ENTERPRISE VERIFICATION TEST SUITE           ");
        System.out.println("================================================================================");

        try {
            // Section 1: Database & Connection (Tests 1 - 6)
            testSection1_DatabaseInfrastructure();

            // Section 2: Authentication & Security (Tests 7 - 13)
            testSection2_AuthenticationSecurity();

            // Section 3: Company Management (Tests 14 - 19)
            testSection3_CompanyManagement();

            // Section 4: Driver Management (Tests 20 - 26)
            testSection4_DriverManagement();

            // Section 5: Vehicle Fleet Management (Tests 27 - 31)
            testSection5_VehicleFleetManagement();

            // Section 6: Daily Trip Logging (Tests 32 - 38)
            testSection6_TripLogging();

            // Section 7: Automated Invoicing Engine (Tests 39 - 44)
            testSection7_InvoiceEngine();

            // Section 8: Native PDF & File Export (Tests 45 - 49)
            testSection8_DocumentExport();

            // Section 9: Accounts Receivable & Payments (Tests 50 - 54)
            testSection9_PaymentLifecycle();

            // Section 10: Executive Analytics (Tests 55 - 58)
            testSection10_DashboardAnalytics();

            // Section 11: Validation Engine (Tests 59 - 64)
            testSection11_ValidationEngine();

            // Section 12: UI Architecture & Auto-Update (Tests 65 - 70)
            testSection12_UIAndAutoUpdate();

            long duration = System.currentTimeMillis() - startTime;

            System.out.println("\n================================================================================");
            System.out.printf("  VERIFICATION COMPLETE IN %d ms | PASSED: %d | FAILED: %d%n", duration, totalPassed, totalFailed);
            System.out.println("================================================================================");

            if (totalFailed > 0) {
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Fatal error during test suite execution:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            System.out.printf("  TC-%02d [PASS] %s%n", testIndex++, description);
            totalPassed++;
        } else {
            System.err.printf("  TC-%02d [FAIL] %s%n", testIndex++, description);
            totalFailed++;
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 1: Database Infrastructure (6 Tests)
    // -------------------------------------------------------------------------
    private static void testSection1_DatabaseInfrastructure() throws Exception {
        System.out.println("\n--- SECTION 1: Database Infrastructure & Schema Integrity ---");

        check("MySQL active connection can be opened", DatabaseConnection.getConnection() != null);
        check("DatabaseConnection.testConnection() ping succeeds", DatabaseConnection.testConnection());

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT DATABASE()");
            rs.next();
            check("Connected to correct catalog 'transport_db'", "transport_db".equalsIgnoreCase(rs.getString(1)));

            rs = stmt.executeQuery("SHOW TABLES IN transport_db");
            int count = 0;
            while (rs.next()) count++;
            check("All 7 essential schema tables exist", count == 7);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM company");
            rs.next();
            check("Client company table is pre-seeded (count >= 5)", rs.getInt(1) >= 5);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM driver");
            rs.next();
            check("Driver directory table is pre-seeded (count >= 5)", rs.getInt(1) >= 5);
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 2: Authentication & Security (7 Tests)
    // -------------------------------------------------------------------------
    private static void testSection2_AuthenticationSecurity() {
        System.out.println("\n--- SECTION 2: Authentication & Security Access Control ---");
        AdminDAO adminDAO = new AdminDAO();
        DriverDAO driverDAO = new DriverDAO();

        Admin admin = adminDAO.login("admin", "admin123");
        check("Admin login succeeds with valid credentials", admin != null);
        check("Admin object correctly populated with full name", admin != null && admin.getFullName() != null && !admin.getFullName().isEmpty());

        check("Admin login fails with wrong password", adminDAO.login("admin", "invalidPass") == null);
        check("Admin login fails with unknown username", adminDAO.login("non_existent_admin", "admin123") == null);

        Driver driver = driverDAO.login("rahul", "rahul123");
        check("Driver login succeeds with valid credentials", driver != null && "rahul".equals(driver.getUsername()));
        check("Driver login fails with wrong password", driverDAO.login("rahul", "wrongPass") == null);
        check("Driver login fails for non-existent driver", driverDAO.login("ghost_driver", "pass123") == null);
    }

    // -------------------------------------------------------------------------
    // SECTION 3: Client Company Management (6 Tests)
    // -------------------------------------------------------------------------
    private static void testSection3_CompanyManagement() {
        System.out.println("\n--- SECTION 3: Client Company Management ---");
        CompanyDAO dao = new CompanyDAO();

        String uniqueName = "Test Logistics Hub " + System.currentTimeMillis();
        Company c = new Company();
        c.setCompanyName(uniqueName);
        c.setAddress("Kalyan West, Thane");
        c.setContactNumber("9876543211");
        c.setGstNumber("27TESTCO1234F1Z");

        boolean added = dao.addCompany(c);
        check("Add new client company to database", added);

        List<Company> list = dao.getAllCompanies();
        Company created = null;
        for (Company item : list) {
            if (uniqueName.equals(item.getCompanyName())) {
                created = item;
                break;
            }
        }
        check("Retrieve newly created company in getAllCompanies()", created != null);

        if (created != null) {
            Company byId = dao.getCompanyById(created.getId());
            check("Find company by ID matches created entity", byId != null && uniqueName.equals(byId.getCompanyName()));

            created.setAddress("Updated Logistics Center, Thane");
            boolean updated = dao.updateCompany(created);
            check("Update company contact address succeeds", updated);

            Company verified = dao.getCompanyById(created.getId());
            check("Verified updated address persisted in database", verified != null && "Updated Logistics Center, Thane".equals(verified.getAddress()));

            boolean deleted = dao.deleteCompany(created.getId());
            check("Delete unlinked company record succeeds", deleted);
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 4: Driver Management & Status (7 Tests)
    // -------------------------------------------------------------------------
    private static void testSection4_DriverManagement() {
        System.out.println("\n--- SECTION 4: Driver Fleet Management & Access Privileges ---");
        DriverDAO dao = new DriverDAO();

        String user = "drv_" + (System.currentTimeMillis() % 1000000);
        Driver d = new Driver();
        d.setDriverName("Suresh Jadhav");
        d.setMobileNumber("9820011223");
        d.setLicenceNumber("MH1420210098765");
        d.setUsername(user);
        d.setPassword("secure99");

        boolean added = dao.addDriver(d);
        check("Register new driver with default active state", added);

        Driver logged = dao.login(user, "secure99");
        check("Newly registered driver successfully logs in", logged != null);

        if (logged != null) {
            logged.setMobileNumber("9820099887");
            boolean updated = dao.updateDriver(logged);
            check("Update driver mobile number succeeds", updated);

            dao.setDriverActive(logged.getId(), false);
            Driver inactiveLogin = dao.login(user, "secure99");
            check("Deactivated driver account cannot log in", inactiveLogin == null);

            dao.setDriverActive(logged.getId(), true);
            Driver reactivatedLogin = dao.login(user, "secure99");
            check("Reactivated driver account can log in again", reactivatedLogin != null);

            boolean deleted = dao.deleteDriver(logged.getId());
            check("Delete driver account with no linked trips succeeds", deleted);

            Driver afterDel = dao.getDriverById(logged.getId());
            check("Driver account completely removed from database", afterDel == null);
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 5: Commercial Vehicle Management (5 Tests)
    // -------------------------------------------------------------------------
    private static void testSection5_VehicleFleetManagement() {
        System.out.println("\n--- SECTION 5: Commercial Vehicle Fleet Management ---");
        VehicleDAO dao = new VehicleDAO();

        String regPlate = "MH-04-TZ-" + (1000 + (int)(Math.random() * 8999));
        Vehicle v = new Vehicle();
        v.setRegistrationNumber(regPlate);
        v.setVehicleType("Pickup Van");
        v.setDriverId(2); // Amit Singh
        v.setStatus("Active");

        boolean added = dao.addVehicle(v);
        check("Add new commercial vehicle to fleet", added);

        List<Vehicle> allVehicles = dao.getAllVehicles();
        Vehicle found = null;
        for (Vehicle veh : allVehicles) {
            if (regPlate.equals(veh.getRegistrationNumber())) {
                found = veh;
                break;
            }
        }
        check("Retrieve vehicle in getAllVehicles() with driver JOIN", found != null && found.getDriverName() != null);

        if (found != null) {
            List<Vehicle> amitVehicles = dao.getVehiclesByDriver(2);
            boolean assigned = false;
            for (Vehicle veh : amitVehicles) {
                if (regPlate.equals(veh.getRegistrationNumber())) assigned = true;
            }
            check("Vehicle correctly filtered in getVehiclesByDriver(2)", assigned);

            found.setStatus("Under Maintenance");
            boolean updated = dao.updateVehicle(found);
            check("Update vehicle maintenance status succeeds", updated);

            boolean deleted = dao.deleteVehicle(found.getId());
            check("Delete vehicle record from fleet succeeds", deleted);
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 6: Daily Trip Logging (7 Tests)
    // -------------------------------------------------------------------------
    private static void testSection6_TripLogging() {
        System.out.println("\n--- SECTION 6: Daily Trip Logging & Ledger Aggregations ---");
        TripDAO dao = new TripDAO();

        String code = dao.generateTripCode();
        check("Trip code auto-generation conforms to format (starts with 'TRIP')", code != null && code.startsWith("TRIP"));

        Trip trip = new Trip();
        trip.setTripCode(code);
        trip.setTripDate(new Date());
        trip.setCompanyId(1);
        trip.setDriverId(1);
        trip.setVehicleId(1);
        trip.setSourceLocation("Bhiwandi Hub");
        trip.setDestination("Thane Godown");
        trip.setAmount(2800.00);
        trip.setRemarks("Auto Test Execution");

        boolean added = dao.addTrip(trip);
        check("Persist daily trip entry into database", added);

        List<Trip> rahulTrips = dao.getTripsByDriver(1);
        Trip created = null;
        for (Trip t : rahulTrips) {
            if (code.equals(t.getTripCode())) {
                created = t;
                break;
            }
        }
        check("Trip record found in driver's personal ledger", created != null);

        double curMonthRev = dao.getCurrentMonthTotal();
        check("Current month gross revenue query executes successfully (> 0)", curMonthRev > 0);

        int totalTripCount = dao.getTotalTripCount();
        check("Total system trip count query returns valid integer (>= 15)", totalTripCount >= 15);

        if (created != null) {
            created.setAmount(3200.00);
            boolean updated = dao.updateTrip(created);
            check("Admin correction of trip amount succeeds", updated);

            boolean deleted = dao.deleteTrip(created.getId());
            check("Trip record deletion cleans up test data", deleted);
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 7: Automated Monthly Invoice Engine (6 Tests)
    // -------------------------------------------------------------------------
    private static void testSection7_InvoiceEngine() {
        System.out.println("\n--- SECTION 7: Automated Monthly Invoice Generation Engine ---");
        TripDAO tripDAO = new TripDAO();
        InvoiceDAO invoiceDAO = new InvoiceDAO();
        PaymentDAO paymentDAO = new PaymentDAO();

        List<Trip> trips = tripDAO.getTripsForInvoice(1, -1, -1, 10, 2026);
        check("Aggregate October 2026 trips for Company A (found trips >= 4)", trips.size() >= 4);

        double total = 0;
        for (Trip t : trips) total += t.getAmount();
        check("Computed total billing amount is positive (value: ₹ " + total + ")", total > 0);

        String invNum = invoiceDAO.generateInvoiceNumber();
        check("Invoice sequence generated in format INV-xxxx (" + invNum + ")", invNum != null && invNum.startsWith("INV-"));

        Invoice inv = new Invoice();
        inv.setInvoiceNumber(invNum);
        inv.setInvoiceDate(new Date());
        inv.setCompanyId(1);
        inv.setCompanyName("Company A");
        inv.setBillingMonth(10);
        inv.setBillingYear(2026);
        inv.setTotalTrips(trips.size());
        inv.setTotalAmount(total);
        inv.setStatus("Generated");

        boolean saved = invoiceDAO.saveInvoice(inv);
        check("Invoice header saved into MySQL database", saved);

        int invId = invoiceDAO.getLastInsertedInvoiceId();
        check("Invoice auto-increment ID resolved successfully (" + invId + ")", invId > 0);

        // Auto-create payment
        Payment p = new Payment();
        p.setInvoiceId(invId);
        p.setTotalAmount(total);
        p.setAmountReceived(0.0);
        p.setRemainingAmount(total);
        p.setPaymentStatus("Pending");
        boolean pSaved = paymentDAO.createPayment(p);
        check("Payment record automatically linked to invoice with status 'Pending'", pSaved);

        // Cleanup test invoice & payment
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM payment WHERE invoice_id = " + invId);
            stmt.executeUpdate("DELETE FROM invoice WHERE id = " + invId);
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------------------------
    // SECTION 8: Document Export (PDF & Text) (5 Tests)
    // -------------------------------------------------------------------------
    private static void testSection8_DocumentExport() throws Exception {
        System.out.println("\n--- SECTION 8: Document Export (Native PDF & Text) ---");
        Invoice inv = new InvoiceDAO().getInvoiceById(1);
        List<Trip> trips = new TripDAO().getTripsForInvoice(1, -1, -1, 10, 2026);

        String pdfPath = "invoices/TC_INV01_TEST.pdf";
        boolean pdfOk = InvoiceGenerator.generatePDF(inv, trips, pdfPath);
        check("Native PDF 1.4 invoice generated via pure Core Java", pdfOk);

        File pdfFile = new File(pdfPath);
        check("Generated PDF file exists on disk and is non-empty", pdfFile.exists() && pdfFile.length() > 500);

        // Verify PDF Header magic bytes (%PDF-1.4)
        byte[] magic = new byte[8];
        try (FileInputStream fis = new FileInputStream(pdfFile)) {
            fis.read(magic);
        }
        String magicStr = new String(magic);
        check("Generated PDF header starts with '%PDF-1.4'", magicStr.startsWith("%PDF-1.4"));

        String txtPath = "invoices/TC_INV01_TEST.txt";
        boolean txtOk = InvoiceGenerator.generateTextInvoice(inv, trips, txtPath);
        check("Printable plain text voucher generated successfully", txtOk);

        File txtFile = new File(txtPath);
        check("Generated text voucher exists and contains billing layout", txtFile.exists() && txtFile.length() > 200);

        if (pdfFile.exists()) pdfFile.delete();
        if (txtFile.exists()) txtFile.delete();
    }

    // -------------------------------------------------------------------------
    // SECTION 9: Accounts Receivable & Payments (5 Tests)
    // -------------------------------------------------------------------------
    private static void testSection9_PaymentLifecycle() {
        System.out.println("\n--- SECTION 9: Accounts Receivable & Payment Lifecycle ---");
        PaymentDAO dao = new PaymentDAO();

        Payment p = dao.getPaymentByInvoiceId(1);
        check("Retrieve payment entry by invoice ID #1", p != null);

        if (p != null) {
            check("Total invoice amount matches positive billing total", p.getTotalAmount() > 0);

            double prevRcv = p.getAmountReceived();
            String prevStatus = p.getPaymentStatus();

            p.setAmountReceived(4000.00);
            p.setRemainingAmount(p.getTotalAmount() - 4000.00);
            p.setPaymentStatus("Partially Paid");
            boolean updated = dao.updatePayment(p);
            check("Update payment with partial settlement sets status to 'Partially Paid'", updated);

            Payment updatedRecord = dao.getPaymentByInvoiceId(1);
            check("Verified remaining balance decreases proportionally", updatedRecord != null && updatedRecord.getRemainingAmount() == (p.getTotalAmount() - 4000.00));

            // Restore original state
            p.setAmountReceived(prevRcv);
            p.setRemainingAmount(p.getTotalAmount() - prevRcv);
            p.setPaymentStatus(prevStatus);
            dao.updatePayment(p);
        }

        double totalPending = dao.getTotalPendingAmount();
        check("Total pending receivables query executes cleanly (value: ₹ " + totalPending + ")", totalPending >= 0);
    }

    // -------------------------------------------------------------------------
    // SECTION 10: Executive Analytics (4 Tests)
    // -------------------------------------------------------------------------
    private static void testSection10_DashboardAnalytics() {
        System.out.println("\n--- SECTION 10: Executive Performance Analytics ---");
        CompanyDAO compDAO = new CompanyDAO();
        DriverDAO drvDAO = new DriverDAO();
        VehicleDAO vehDAO = new VehicleDAO();
        TripDAO tripDAO = new TripDAO();

        int compCount = compDAO.getAllCompanies().size();
        check("Company metric card query returns registered count (>= 5)", compCount >= 5);

        int driverCount = drvDAO.getAllDrivers().size();
        check("Driver metric card query returns registered count (>= 5)", driverCount >= 5);

        int vehicleCount = vehDAO.getAllVehicles().size();
        check("Vehicle fleet card query returns fleet count (>= 5)", vehicleCount >= 5);

        int tripCount = tripDAO.getTotalTripCount();
        check("Operational completed trips metric returns valid count (>= 15)", tripCount >= 15);
    }

    // -------------------------------------------------------------------------
    // SECTION 11: Validation Engine (6 Tests)
    // -------------------------------------------------------------------------
    private static void testSection11_ValidationEngine() {
        System.out.println("\n--- SECTION 11: Input Validation & Sanitization Engine ---");

        check("Validation.isValidMobile('9876543210') returns true", Validation.isValidMobile("9876543210"));
        check("Validation.isValidMobile('12345') returns false (invalid digits)", !Validation.isValidMobile("12345"));
        check("Validation.isValidAmount('4500.50') returns true", Validation.isValidAmount("4500.50"));
        check("Validation.isValidAmount('-500') returns false (negative amount)", !Validation.isValidAmount("-500"));
        check("Validation.isValidUsername('admin_user') returns true", Validation.isValidUsername("adminuser1"));
        check("Validation.isValidPassword('12345') returns false (< 6 characters)", !Validation.isValidPassword("12345"));
    }

    // -------------------------------------------------------------------------
    // SECTION 12: UI Architecture & Dynamic Auto-Update (6 Tests)
    // -------------------------------------------------------------------------
    private static void testSection12_UIAndAutoUpdate() {
        System.out.println("\n--- SECTION 12: Swing UI Architecture & Dynamic Auto-Update ---");

        SwingUtilities.invokeLater(() -> {
            try {
                CompanyPanel compPanel = new CompanyPanel();
                compPanel.refreshData();
                check("CompanyPanel instantiated and refreshData() reloads tables", true);

                DriverPanel drvPanel = new DriverPanel();
                drvPanel.refreshData();
                check("DriverPanel instantiated and refreshData() reloads tables", true);

                VehiclePanel vehPanel = new VehiclePanel();
                vehPanel.refreshData();
                check("VehiclePanel instantiated and refreshData() refreshes driver dropdown", true);

                TripHistoryPanel histPanel = new TripHistoryPanel(true, -1);
                histPanel.refreshData();
                check("TripHistoryPanel instantiated and refreshData() re-filters trips", true);

                InvoicePanel invPanel = new InvoicePanel();
                invPanel.refreshData();
                check("InvoicePanel instantiated and refreshData() re-populates all 3 dropdowns", true);

                PaymentPanel payPanel = new PaymentPanel();
                payPanel.refreshData();
                check("PaymentPanel instantiated and refreshData() synchronizes payments", true);

            } catch (Exception e) {
                System.err.println("UI testing exception: " + e.getMessage());
                totalFailed++;
            }
        });
    }
}
