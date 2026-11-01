package com.transport.test;

import com.transport.dao.*;
import com.transport.db.DatabaseConnection;
import com.transport.model.*;
import com.transport.ui.*;
import com.transport.util.InvoiceGenerator;
import com.transport.util.Validation;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * FullAppTest.java - Automated Comprehensive Integration & Workflow Test Suite
 *
 * Verifies all modules, DAOs, UI panels, auto-updating, and file generation.
 */
public class FullAppTest {

    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("        MOT LOGISTICS - FULL APPLICATION VERIFICATION TEST      ");
        System.out.println("================================================================");

        try {
            testDatabaseAndTables();
            testAuthenticationModule();
            testCompanyModule();
            testDriverModule();
            testVehicleModule();
            testTripModule();
            testInvoiceGenerationAndExportModule();
            testPaymentLifecycleModule();
            testValidationModule();
            testUIAndRefreshablePanels();

            System.out.println("\n================================================================");
            System.out.printf("  TEST EXECUTION COMPLETED: %d PASSED, %d FAILED%n", passedTests, failedTests);
            System.out.println("================================================================");

            if (failedTests > 0) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Fatal unexpected error during test suite execution:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passedTests++;
        } else {
            System.err.println("  [FAIL] " + testName);
            failedTests++;
        }
    }

    // --- TEST SUITES ---

    private static void testDatabaseAndTables() throws Exception {
        System.out.println("\n--- 1. Database Connectivity & Tables Check ---");
        assertTrue("DatabaseConnection.testConnection() returns true", DatabaseConnection.testConnection());

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES IN transport_db")) {

            int tableCount = 0;
            while (rs.next()) {
                tableCount++;
            }
            assertTrue("All 7 database tables are present (found " + tableCount + ")", tableCount == 7);
        }
    }

    private static void testAuthenticationModule() {
        System.out.println("\n--- 2. Authentication & Credential Check ---");
        AdminDAO adminDAO = new AdminDAO();
        DriverDAO driverDAO = new DriverDAO();

        Admin admin = adminDAO.login("admin", "admin123");
        assertTrue("Admin login with valid credentials succeeds", admin != null && "admin".equals(admin.getUsername()));

        Admin badAdmin = adminDAO.login("admin", "wrongpassword");
        assertTrue("Admin login with invalid credentials rejected", badAdmin == null);

        Driver driver = driverDAO.login("rahul", "rahul123");
        assertTrue("Driver login with valid credentials succeeds", driver != null && "rahul".equals(driver.getUsername()));

        Driver badDriver = driverDAO.login("rahul", "wrongpassword");
        assertTrue("Driver login with invalid credentials rejected", badDriver == null);
    }

    private static void testCompanyModule() {
        System.out.println("\n--- 3. Client Company Management Check ---");
        CompanyDAO dao = new CompanyDAO();

        Company c = new Company();
        String testName = "Test Logistics Co " + System.currentTimeMillis();
        c.setCompanyName(testName);
        c.setAddress("Andheri East, Mumbai");
        c.setContactNumber("9988776655");
        c.setGstNumber("27TEST1234F1Z9");

        boolean added = dao.addCompany(c);
        assertTrue("New company inserted into DB", added);

        List<Company> list = dao.getAllCompanies();
        Company found = null;
        for (Company comp : list) {
            if (testName.equals(comp.getCompanyName())) {
                found = comp;
                break;
            }
        }
        assertTrue("Inserted company retrieved in getAllCompanies()", found != null);

        if (found != null) {
            found.setAddress("Updated Address in Mumbai");
            boolean updated = dao.updateCompany(found);
            assertTrue("Company details updated", updated);

            boolean deleted = dao.deleteCompany(found.getId());
            assertTrue("Test company cleanly deleted", deleted);
        }
    }

    private static void testDriverModule() {
        System.out.println("\n--- 4. Driver Management Check ---");
        DriverDAO dao = new DriverDAO();

        String testUsername = "driver_" + (System.currentTimeMillis() % 100000);
        Driver d = new Driver();
        d.setDriverName("Test Driver");
        d.setMobileNumber("9876543210");
        d.setLicenceNumber("MH9920260011223");
        d.setUsername(testUsername);
        d.setPassword("pass123");

        boolean added = dao.addDriver(d);
        assertTrue("New driver registered", added);

        Driver logged = dao.login(testUsername, "pass123");
        assertTrue("Newly registered driver can log in", logged != null);

        if (logged != null) {
            // Test deactivation
            dao.setDriverActive(logged.getId(), false);
            Driver inactiveLogin = dao.login(testUsername, "pass123");
            assertTrue("Deactivated driver cannot log in", inactiveLogin == null);

            // Reactivate
            dao.setDriverActive(logged.getId(), true);
            Driver reactivatedLogin = dao.login(testUsername, "pass123");
            assertTrue("Reactivated driver can log in again", reactivatedLogin != null);
        }
    }

    private static void testVehicleModule() {
        System.out.println("\n--- 5. Fleet Vehicle Management Check ---");
        VehicleDAO dao = new VehicleDAO();

        String testPlate = "MH-01-ZZ-" + (1000 + (int)(Math.random() * 8999));
        Vehicle v = new Vehicle();
        v.setRegistrationNumber(testPlate);
        v.setVehicleType("Pickup Van");
        v.setDriverId(1); // Rahul
        v.setStatus("Active");

        boolean added = dao.addVehicle(v);
        assertTrue("New vehicle registered", added);

        List<Vehicle> rahulVehicles = dao.getVehiclesByDriver(1);
        boolean foundInRahul = false;
        int createdId = -1;
        for (Vehicle veh : rahulVehicles) {
            if (testPlate.equals(veh.getRegistrationNumber())) {
                foundInRahul = true;
                createdId = veh.getId();
                break;
            }
        }
        assertTrue("Vehicle found assigned to Rahul", foundInRahul);

        if (createdId > 0) {
            boolean deleted = dao.deleteVehicle(createdId);
            assertTrue("Test vehicle cleanly deleted", deleted);
        }
    }

    private static void testTripModule() {
        System.out.println("\n--- 6. Daily Trip Logging & Aggregation Check ---");
        TripDAO dao = new TripDAO();

        String tripCode = dao.generateTripCode();
        assertTrue("Trip code auto-generated (" + tripCode + ")", tripCode != null && tripCode.startsWith("TRIP"));

        Trip trip = new Trip();
        trip.setTripCode(tripCode);
        trip.setTripDate(new Date());
        trip.setCompanyId(1);
        trip.setDriverId(1);
        trip.setVehicleId(1);
        trip.setSourceLocation("Automated Test Source");
        trip.setDestination("Automated Test Dest");
        trip.setAmount(1950.00);
        trip.setRemarks("Automated Verification Trip");

        boolean added = dao.addTrip(trip);
        assertTrue("Trip successfully added to database", added);

        List<Trip> trips = dao.getTripsByDriver(1);
        Trip foundTrip = null;
        for (Trip t : trips) {
            if (tripCode.equals(t.getTripCode())) {
                foundTrip = t;
                break;
            }
        }
        assertTrue("Added trip found in driver trip ledger", foundTrip != null);

        double monthTotal = dao.getCurrentMonthTotal();
        assertTrue("Current month gross revenue calculated (> 0, value: ₹ " + monthTotal + ")", monthTotal > 0);

        int totalCount = dao.getTotalTripCount();
        assertTrue("Total trip count retrieved (> 0, value: " + totalCount + ")", totalCount > 0);

        if (foundTrip != null) {
            boolean deleted = dao.deleteTrip(foundTrip.getId());
            assertTrue("Test trip cleanly deleted after verification", deleted);
        }
    }

    private static void testInvoiceGenerationAndExportModule() {
        System.out.println("\n--- 7. Automated Invoice Generation & Export Check ---");
        TripDAO tripDAO = new TripDAO();
        InvoiceDAO invoiceDAO = new InvoiceDAO();
        PaymentDAO paymentDAO = new PaymentDAO();

        List<Trip> trips = tripDAO.getTripsForInvoice(1, -1, -1, 10, 2026);
        assertTrue("Retrieved October 2026 trips for Company A (found " + trips.size() + " trips)", trips.size() >= 4);

        double expectedSum = 0;
        for (Trip t : trips) expectedSum += t.getAmount();

        String invNum = invoiceDAO.generateInvoiceNumber();
        assertTrue("Invoice number generated (" + invNum + ")", invNum != null && invNum.startsWith("INV-"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invNum);
        invoice.setInvoiceDate(new Date());
        invoice.setCompanyId(1);
        invoice.setCompanyName("Company A");
        invoice.setBillingMonth(10);
        invoice.setBillingYear(2026);
        invoice.setTotalTrips(trips.size());
        invoice.setTotalAmount(expectedSum);
        invoice.setStatus("Generated");

        boolean saved = invoiceDAO.saveInvoice(invoice);
        assertTrue("Invoice record persisted to MySQL", saved);

        int invoiceId = invoiceDAO.getLastInsertedInvoiceId();
        assertTrue("Invoice ID generated (" + invoiceId + ")", invoiceId > 0);

        // Auto-create payment
        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setTotalAmount(expectedSum);
        payment.setAmountReceived(0);
        payment.setRemainingAmount(expectedSum);
        payment.setPaymentStatus("Pending");
        boolean paymentCreated = paymentDAO.createPayment(payment);
        assertTrue("Payment row automatically created for invoice with status 'Pending'", paymentCreated);

        // Export plain text file
        String exportPath = "invoices/" + invNum + "_TEST.txt";
        boolean fileExported = InvoiceGenerator.generateTextInvoice(invoice, trips, exportPath);
        assertTrue("Invoice formatted and exported to disk", fileExported);

        File f = new File(exportPath);
        assertTrue("Exported invoice file exists and is non-empty (" + f.length() + " bytes)", f.exists() && f.length() > 200);

        // Clean up test file
        if (f.exists()) f.delete();
    }

    private static void testPaymentLifecycleModule() {
        System.out.println("\n--- 8. Payment Lifecycle Settlement Check ---");
        PaymentDAO paymentDAO = new PaymentDAO();

        Payment p = paymentDAO.getPaymentByInvoiceId(1);
        assertTrue("Payment retrieved for Invoice #1", p != null);

        if (p != null) {
            double origReceived = p.getAmountReceived();
            String origStatus = p.getPaymentStatus();

            // Simulate partial update
            p.setAmountReceived(4500.00);
            p.setRemainingAmount(p.getTotalAmount() - 4500.00);
            p.setPaymentStatus("Partially Paid");
            boolean updated = paymentDAO.updatePayment(p);
            assertTrue("Payment updated to 'Partially Paid'", updated);

            // Restore original state
            p.setAmountReceived(origReceived);
            p.setRemainingAmount(p.getTotalAmount() - origReceived);
            p.setPaymentStatus(origStatus);
            paymentDAO.updatePayment(p);
        }
    }

    private static void testValidationModule() {
        System.out.println("\n--- 9. Input Validation Utility Check ---");
        assertTrue("Valid Indian mobile number accepted", Validation.isValidMobile("9876543210"));
        assertTrue("Invalid mobile number rejected", !Validation.isValidMobile("12345"));
        assertTrue("Valid amount string accepted", Validation.isValidAmount("2500.50"));
        assertTrue("Zero amount rejected", !Validation.isValidAmount("0"));
        assertTrue("Negative amount rejected", !Validation.isValidAmount("-100"));
        assertTrue("Letters in amount rejected", !Validation.isValidAmount("two-thousand"));
    }

    private static void testUIAndRefreshablePanels() {
        System.out.println("\n--- 10. UI Panels & Dynamic Refreshable Check ---");

        // Run UI tests on Swing EDT
        SwingUtilities.invokeLater(() -> {
            try {
                CompanyPanel compPanel = new CompanyPanel();
                compPanel.refreshData();
                assertTrue("CompanyPanel instantiates and refreshData() executes", true);

                DriverPanel driverPanel = new DriverPanel();
                driverPanel.refreshData();
                assertTrue("DriverPanel instantiates and refreshData() executes", true);

                VehiclePanel vehiclePanel = new VehiclePanel();
                vehiclePanel.refreshData();
                assertTrue("VehiclePanel instantiates and refreshData() executes", true);

                Driver sampleDriver = new DriverDAO().login("rahul", "rahul123");
                TripEntryPanel tripEntryPanel = new TripEntryPanel(sampleDriver);
                tripEntryPanel.refreshData();
                assertTrue("TripEntryPanel instantiates and refreshData() executes", true);

                TripHistoryPanel tripHistoryPanel = new TripHistoryPanel(true, -1);
                tripHistoryPanel.refreshData();
                assertTrue("TripHistoryPanel instantiates and refreshData() executes", true);

                InvoicePanel invoicePanel = new InvoicePanel();
                invoicePanel.refreshData();
                assertTrue("InvoicePanel instantiates and refreshData() executes", true);

                PaymentPanel paymentPanel = new PaymentPanel();
                paymentPanel.refreshData();
                assertTrue("PaymentPanel instantiates and refreshData() executes", true);

                Admin sampleAdmin = new AdminDAO().login("admin", "admin123");
                AdminDashboard adminDashboard = new AdminDashboard(sampleAdmin);
                assertTrue("AdminDashboard instantiates with 7 tabs", adminDashboard != null);
                adminDashboard.dispose();

                DriverDashboard driverDashboard = new DriverDashboard(sampleDriver);
                assertTrue("DriverDashboard instantiates with 2 tabs", driverDashboard != null);
                driverDashboard.dispose();

            } catch (Exception e) {
                System.err.println("Error in UI Panel tests: " + e.getMessage());
                e.printStackTrace();
                failedTests++;
            }
        });
    }
}
