package com.transport.ui;

import com.transport.dao.CompanyDAO;
import com.transport.dao.DriverDAO;
import com.transport.dao.InvoiceDAO;
import com.transport.dao.PaymentDAO;
import com.transport.dao.TripDAO;
import com.transport.dao.VehicleDAO;
import com.transport.model.Company;
import com.transport.model.Driver;
import com.transport.model.Invoice;
import com.transport.model.Payment;
import com.transport.model.Trip;
import com.transport.model.Vehicle;
import com.transport.util.InvoiceGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * InvoicePanel.java - Generate Monthly Invoices
 *
 * Admin selects:
 * - Company
 * - Optional: Driver, Vehicle
 * - Month and Year
 *
 * Then clicks "Generate Invoice" to:
 * 1. Fetch all matching trips from DB
 * 2. Calculate total amount
 * 3. Display trip table in preview
 * 4. Save invoice to DB
 * 5. Export PDF file
 *
 * This is the MOST IMPORTANT panel of the system.
 */
public class InvoicePanel extends JPanel {

    // Filter / selection fields
    private JComboBox<Company> companyCombo;
    private JComboBox<Driver>  driverCombo;
    private JComboBox<Vehicle> vehicleCombo;
    private JComboBox<String>  monthCombo;
    private JComboBox<String>  yearCombo;

    // Invoice header display labels
    private JLabel invoiceNumberLabel;
    private JLabel invoiceDateLabel;
    private JLabel totalTripsLabel;
    private JLabel totalAmountLabel;

    // Preview table
    private JTable            previewTable;
    private DefaultTableModel previewTableModel;

    // Buttons
    private JButton generateBtn;
    private JButton exportPDFBtn;
    private JButton viewHistoryBtn;

    // DAOs
    private TripDAO    tripDAO    = new TripDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private DriverDAO  driverDAO  = new DriverDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();

    // Stores last generated invoice for PDF export
    private Invoice lastGeneratedInvoice;
    private List<Trip> lastTrips;

    private static final String[] MONTH_NAMES = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public InvoicePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        // ---- TOP: Selection Panel ----
        JPanel selPanel = new JPanel(new GridBagLayout());
        selPanel.setBorder(BorderFactory.createTitledBorder("Generate Invoice - Select Filters"));
        selPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        companyCombo = new JComboBox<>();
        driverCombo  = new JComboBox<>();
        vehicleCombo = new JComboBox<>();
        monthCombo   = new JComboBox<>(MONTH_NAMES);
        yearCombo    = new JComboBox<>(new String[]{"2024","2025","2026","2027"});

        // Populate combos
        for (Company c : companyDAO.getAllCompanies()) companyCombo.addItem(c);

        Driver allDrivers = new Driver(); allDrivers.setDriverName("All Drivers"); allDrivers.setId(-1);
        driverCombo.addItem(allDrivers);
        for (Driver d : driverDAO.getActiveDrivers()) driverCombo.addItem(d);

        Vehicle allVehicles = new Vehicle(); allVehicles.setRegistrationNumber("All Vehicles"); allVehicles.setId(-1);
        vehicleCombo.addItem(allVehicles);
        for (Vehicle v : vehicleDAO.getAllVehicles()) vehicleCombo.addItem(v);

        // Set current month/year as default
        java.util.Calendar cal = java.util.Calendar.getInstance();
        monthCombo.setSelectedIndex(cal.get(java.util.Calendar.MONTH));
        yearCombo.setSelectedItem(String.valueOf(cal.get(java.util.Calendar.YEAR)));

        gbc.gridx = 0; gbc.gridy = 0; selPanel.add(new JLabel("Company *"), gbc);
        gbc.gridx = 1; selPanel.add(companyCombo, gbc);
        gbc.gridx = 2; selPanel.add(new JLabel("Driver"), gbc);
        gbc.gridx = 3; selPanel.add(driverCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; selPanel.add(new JLabel("Vehicle"), gbc);
        gbc.gridx = 1; selPanel.add(vehicleCombo, gbc);
        gbc.gridx = 2; selPanel.add(new JLabel("Month *"), gbc);
        gbc.gridx = 3; selPanel.add(monthCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; selPanel.add(new JLabel("Year *"), gbc);
        gbc.gridx = 1; selPanel.add(yearCombo, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnPanel.setOpaque(false);
        generateBtn    = createButton("Generate Invoice", new Color(0, 100, 180));
        exportPDFBtn   = createButton("Export PDF",       new Color(180, 80, 0));
        viewHistoryBtn = createButton("Invoice History",  Color.DARK_GRAY);
        exportPDFBtn.setEnabled(false); // Enable after generation
        btnPanel.add(generateBtn);
        btnPanel.add(exportPDFBtn);
        btnPanel.add(viewHistoryBtn);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        selPanel.add(btnPanel, gbc);

        // ---- MIDDLE: Invoice Header Info ----
        JPanel headerPanel = new JPanel(new GridLayout(2, 4, 10, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Invoice Details"));
        headerPanel.setBackground(new Color(245, 250, 255));

        invoiceNumberLabel = new JLabel("-");
        invoiceDateLabel   = new JLabel("-");
        totalTripsLabel    = new JLabel("-");
        totalAmountLabel   = new JLabel("-");
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalAmountLabel.setForeground(new Color(0, 130, 70));

        headerPanel.add(new JLabel("Invoice Number:")); headerPanel.add(invoiceNumberLabel);
        headerPanel.add(new JLabel("Invoice Date:"));   headerPanel.add(invoiceDateLabel);
        headerPanel.add(new JLabel("Total Trips:"));    headerPanel.add(totalTripsLabel);
        headerPanel.add(new JLabel("Total Amount:"));   headerPanel.add(totalAmountLabel);

        // ---- BOTTOM: Preview Table ----
        String[] cols = {"Sr.No.", "Date", "Source", "Destination", "Amount (Rs.)"};
        previewTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        previewTable = new JTable(previewTableModel);
        previewTable.setRowHeight(25);
        previewTable.getTableHeader().setBackground(new Color(0, 70, 130));
        previewTable.getTableHeader().setForeground(Color.WHITE);
        previewTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Trip Details Preview"));

        // Wire actions
        generateBtn.addActionListener(e    -> generateInvoice());
        exportPDFBtn.addActionListener(e   -> exportPDF());
        viewHistoryBtn.addActionListener(e -> showInvoiceHistory());

        // Combine north panels
        JPanel northPanel = new JPanel(new BorderLayout(0, 5));
        northPanel.setOpaque(false);
        northPanel.add(selPanel,    BorderLayout.NORTH);
        northPanel.add(headerPanel, BorderLayout.SOUTH);

        add(northPanel,   BorderLayout.NORTH);
        add(scrollPane,   BorderLayout.CENTER);
    }

    /** Main invoice generation logic */
    private void generateInvoice() {
        if (companyCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a company!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Company company = (Company) companyCombo.getSelectedItem();
        Driver  driver  = (Driver)  driverCombo.getSelectedItem();
        Vehicle vehicle = (Vehicle) vehicleCombo.getSelectedItem();

        int month = monthCombo.getSelectedIndex() + 1; // January = 1
        int year;
        try {
            year = Integer.parseInt((String) yearCombo.getSelectedItem());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid year!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int driverId  = (driver  != null && driver.getId()  > 0) ? driver.getId()  : -1;
        int vehicleId = (vehicle != null && vehicle.getId() > 0) ? vehicle.getId() : -1;

        // Fetch trips from DB
        lastTrips = tripDAO.getTripsForInvoice(company.getId(), driverId, vehicleId, month, year);

        if (lastTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips found for the selected period!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Calculate total
        double totalAmount = 0;
        for (Trip t : lastTrips) totalAmount += t.getAmount();

        // Build Invoice object
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceDAO.generateInvoiceNumber());
        invoice.setInvoiceDate(new Date());
        invoice.setCompanyId(company.getId());
        invoice.setCompanyName(company.getCompanyName());
        if (driverId > 0) { invoice.setDriverId(driverId); invoice.setDriverName(driver.getDriverName()); }
        if (vehicleId > 0) { invoice.setVehicleId(vehicleId); invoice.setVehicleNumber(vehicle.getRegistrationNumber()); }
        invoice.setBillingMonth(month);
        invoice.setBillingYear(year);
        invoice.setTotalTrips(lastTrips.size());
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus("Generated");

        // Save invoice to DB
        if (invoiceDAO.saveInvoice(invoice)) {
            int invoiceId = invoiceDAO.getLastInsertedInvoiceId();
            invoice.setId(invoiceId);

            // Create payment record (Pending by default)
            Payment payment = new Payment();
            payment.setInvoiceId(invoiceId);
            payment.setTotalAmount(totalAmount);
            payment.setAmountReceived(0);
            payment.setRemainingAmount(totalAmount);
            payment.setPaymentStatus("Pending");
            paymentDAO.createPayment(payment);

            lastGeneratedInvoice = invoice;
            exportPDFBtn.setEnabled(true);

            // Update header labels
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            invoiceNumberLabel.setText(invoice.getInvoiceNumber());
            invoiceDateLabel.setText(sdf.format(invoice.getInvoiceDate()));
            totalTripsLabel.setText(String.valueOf(lastTrips.size()));
            totalAmountLabel.setText("Rs. " + String.format("%.2f", totalAmount));

            // Populate preview table
            previewTableModel.setRowCount(0);
            int sr = 1;
            for (Trip t : lastTrips) {
                previewTableModel.addRow(new Object[]{
                    sr++, sdf.format(t.getTripDate()),
                    t.getSourceLocation(), t.getDestination(),
                    String.format("%.2f", t.getAmount())
                });
            }

            JOptionPane.showMessageDialog(this,
                "Invoice " + invoice.getInvoiceNumber() + " generated successfully!\n" +
                "Total Trips: " + lastTrips.size() + "\n" +
                "Total Amount: Rs. " + String.format("%.2f", totalAmount) + "\n\n" +
                "Click 'Export PDF' to save as PDF file.",
                "Invoice Generated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save invoice!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Export the last generated invoice as PDF */
    private void exportPDF() {
        if (lastGeneratedInvoice == null || lastTrips == null) {
            JOptionPane.showMessageDialog(this, "Please generate an invoice first!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Ask user where to save
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(lastGeneratedInvoice.getInvoiceNumber() + ".pdf"));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".pdf")) filePath += ".pdf";

            String company = (String) companyCombo.getSelectedItem() != null
                    ? ((Company) companyCombo.getSelectedItem()).getCompanyName() : "";

            boolean success = InvoiceGenerator.generatePDF(lastGeneratedInvoice, lastTrips, filePath);

            if (success) {
                JOptionPane.showMessageDialog(this, "PDF saved to:\n" + filePath, "PDF Exported", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "PDF export failed! Check that openpdf.jar is in the lib folder.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Show invoice history in a dialog */
    private void showInvoiceHistory() {
        List<Invoice> invoices = invoiceDAO.getAllInvoices();
        String[] cols = {"Invoice No.", "Date", "Company", "Month/Year", "Trips", "Total Amount", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Invoice inv : invoices) {
            model.addRow(new Object[]{
                inv.getInvoiceNumber(),
                sdf.format(inv.getInvoiceDate()),
                inv.getCompanyName(),
                MONTH_NAMES[inv.getBillingMonth()-1] + " " + inv.getBillingYear(),
                inv.getTotalTrips(),
                "Rs. " + String.format("%.2f", inv.getTotalAmount()),
                inv.getStatus()
            });
        }

        JTable histTable = new JTable(model);
        histTable.setRowHeight(25);
        JScrollPane sp = new JScrollPane(histTable);
        sp.setPreferredSize(new Dimension(750, 350));

        JOptionPane.showMessageDialog(this, sp, "Invoice History", JOptionPane.PLAIN_MESSAGE);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }
}
