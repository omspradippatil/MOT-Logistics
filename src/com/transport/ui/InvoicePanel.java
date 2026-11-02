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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * InvoicePanel.java - Automated Monthly & Annual Invoice Engine for MOT Logistics
 */
public class InvoicePanel extends JPanel implements Refreshable {

    private JComboBox<Company> companyCombo;
    private JComboBox<Driver>  driverCombo;
    private JComboBox<Vehicle> vehicleCombo;
    private JComboBox<String>  monthCombo;
    private JComboBox<String>  yearCombo;

    private JLabel invoiceNumberLabel;
    private JLabel invoiceDateLabel;
    private JLabel totalTripsLabel;
    private JLabel totalAmountLabel;

    private JTable            previewTable;
    private DefaultTableModel previewTableModel;

    private ModernButton generateBtn;
    private ModernButton exportPDFBtn;
    private ModernButton exportTxtBtn;
    private ModernButton bulkExportBtn;
    private ModernButton viewHistoryBtn;

    private TripDAO    tripDAO    = new TripDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private DriverDAO  driverDAO  = new DriverDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();

    private Invoice    lastGeneratedInvoice;
    private List<Trip> lastTrips;

    public static final String[] MONTH_NAMES = {
        "-- All Months (Annual / Cumulative) --",
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public InvoicePanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);
        initUI();
    }

    private void initUI() {
        // Filter Selection Panel
        JPanel selPanel = new JPanel(new GridBagLayout());
        selPanel.setBorder(UITheme.createCardBorder("1. Select Billing Period & Client"));
        selPanel.setBackground(UITheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        companyCombo = new JComboBox<>();
        driverCombo  = new JComboBox<>();
        vehicleCombo = new JComboBox<>();
        monthCombo   = new JComboBox<>(MONTH_NAMES);
        yearCombo    = new JComboBox<>(new String[]{"2024","2025","2026","2027"});

        styleCombo(companyCombo);
        styleCombo(driverCombo);
        styleCombo(vehicleCombo);
        styleCombo(monthCombo);
        styleCombo(yearCombo);

        for (Company c : companyDAO.getAllCompanies()) companyCombo.addItem(c);

        Driver allDrivers = new Driver(); allDrivers.setDriverName("-- All Drivers --"); allDrivers.setId(-1);
        driverCombo.addItem(allDrivers);
        for (Driver d : driverDAO.getActiveDrivers()) driverCombo.addItem(d);

        Vehicle allVehicles = new Vehicle(); allVehicles.setRegistrationNumber("-- All Vehicles --"); allVehicles.setId(-1);
        vehicleCombo.addItem(allVehicles);
        for (Vehicle v : vehicleDAO.getAllVehicles()) vehicleCombo.addItem(v);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        monthCombo.setSelectedIndex(cal.get(java.util.Calendar.MONTH) + 1);
        yearCombo.setSelectedItem(String.valueOf(cal.get(java.util.Calendar.YEAR)));

        gbc.gridx = 0; gbc.gridy = 0; selPanel.add(new JLabel("Client Company: *"), gbc);
        gbc.gridx = 1; selPanel.add(companyCombo, gbc);
        gbc.gridx = 2; selPanel.add(new JLabel("Filter Driver:"), gbc);
        gbc.gridx = 3; selPanel.add(driverCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; selPanel.add(new JLabel("Filter Vehicle:"), gbc);
        gbc.gridx = 1; selPanel.add(vehicleCombo, gbc);
        gbc.gridx = 2; selPanel.add(new JLabel("Billing Month: *"), gbc);
        gbc.gridx = 3; selPanel.add(monthCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; selPanel.add(new JLabel("Billing Year: *"), gbc);
        gbc.gridx = 1; selPanel.add(yearCombo, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnPanel.setOpaque(false);
        generateBtn    = new ModernButton("⚡ Generate Invoice", UITheme.BRAND_BLUE);
        exportPDFBtn   = new ModernButton("📥 Export PDF", UITheme.AMBER_ORANGE);
        exportTxtBtn   = new ModernButton("📄 Export Text", UITheme.SECONDARY_GRAY);
        bulkExportBtn  = new ModernButton("📦 Bulk Export All", UITheme.SUCCESS_GREEN);
        viewHistoryBtn = new ModernButton("📜 Invoice Archive", UITheme.SECONDARY_GRAY);
        exportPDFBtn.setEnabled(false);
        exportTxtBtn.setEnabled(false);

        btnPanel.add(generateBtn);
        btnPanel.add(exportPDFBtn);
        btnPanel.add(exportTxtBtn);
        btnPanel.add(bulkExportBtn);
        btnPanel.add(viewHistoryBtn);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        selPanel.add(btnPanel, gbc);

        // Header Metrics Box
        JPanel headerPanel = new JPanel(new GridLayout(2, 4, 15, 8));
        headerPanel.setBorder(UITheme.createCardBorder("2. Generated Invoice Summary"));
        headerPanel.setBackground(new Color(241, 245, 249));

        invoiceNumberLabel = new JLabel("-");
        invoiceDateLabel   = new JLabel("-");
        totalTripsLabel    = new JLabel("-");
        totalAmountLabel   = new JLabel("-");

        invoiceNumberLabel.setFont(UITheme.FONT_BODY_BOLD);
        invoiceDateLabel.setFont(UITheme.FONT_BODY_BOLD);
        totalTripsLabel.setFont(UITheme.FONT_BODY_BOLD);
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalAmountLabel.setForeground(UITheme.SUCCESS_GREEN);

        headerPanel.add(new JLabel("Invoice Number:")); headerPanel.add(invoiceNumberLabel);
        headerPanel.add(new JLabel("Invoice Date:"));   headerPanel.add(invoiceDateLabel);
        headerPanel.add(new JLabel("Eligible Trips:")); headerPanel.add(totalTripsLabel);
        headerPanel.add(new JLabel("Total Payable:"));  headerPanel.add(totalAmountLabel);

        // Preview Table
        String[] cols = {"Sr.No.", "Date", "Source (From)", "Destination (To)", "Trip Amount (₹)"};
        previewTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        previewTable = new JTable(previewTableModel);
        UITheme.styleTable(previewTable);

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setBorder(UITheme.createCardBorder("3. Itemized Trip Ledger Preview"));

        generateBtn.addActionListener(e    -> generateInvoice());
        exportPDFBtn.addActionListener(e   -> exportPDF());
        exportTxtBtn.addActionListener(e   -> exportText());
        bulkExportBtn.addActionListener(e  -> bulkExportAllInvoices());
        viewHistoryBtn.addActionListener(e -> showInvoiceHistory());

        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setOpaque(false);
        northPanel.add(selPanel,    BorderLayout.NORTH);
        northPanel.add(headerPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(UITheme.FONT_BODY);
        combo.setPreferredSize(new Dimension(190, 32));
    }

    @Override
    public void refreshData() {
        Company selectedComp = (Company) companyCombo.getSelectedItem();
        companyCombo.removeAllItems();
        for (Company c : companyDAO.getAllCompanies()) {
            companyCombo.addItem(c);
            if (selectedComp != null && c.getId() == selectedComp.getId()) {
                companyCombo.setSelectedItem(c);
            }
        }

        Driver selectedDriver = (Driver) driverCombo.getSelectedItem();
        driverCombo.removeAllItems();
        Driver allDrivers = new Driver();
        allDrivers.setDriverName("-- All Drivers --");
        allDrivers.setId(-1);
        driverCombo.addItem(allDrivers);
        for (Driver d : driverDAO.getActiveDrivers()) {
            driverCombo.addItem(d);
            if (selectedDriver != null && d.getId() == selectedDriver.getId()) {
                driverCombo.setSelectedItem(d);
            }
        }

        Vehicle selectedVeh = (Vehicle) vehicleCombo.getSelectedItem();
        vehicleCombo.removeAllItems();
        Vehicle allVehicles = new Vehicle();
        allVehicles.setRegistrationNumber("-- All Vehicles --");
        allVehicles.setId(-1);
        vehicleCombo.addItem(allVehicles);
        for (Vehicle v : vehicleDAO.getAllVehicles()) {
            vehicleCombo.addItem(v);
            if (selectedVeh != null && v.getId() == selectedVeh.getId()) {
                vehicleCombo.setSelectedItem(v);
            }
        }
    }

    private void generateInvoice() {
        if (companyCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a client company!", "Company Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Company company = (Company) companyCombo.getSelectedItem();
        Driver  driver  = (Driver)  driverCombo.getSelectedItem();
        Vehicle vehicle = (Vehicle) vehicleCombo.getSelectedItem();

        int selectedMonthIdx = monthCombo.getSelectedIndex();
        int month = (selectedMonthIdx == 0) ? -1 : selectedMonthIdx; // -1 indicates All Months
        int year;
        try {
            year = Integer.parseInt((String) yearCombo.getSelectedItem());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid billing year selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int driverId  = (driver  != null && driver.getId()  > 0) ? driver.getId()  : -1;
        int vehicleId = (vehicle != null && vehicle.getId() > 0) ? vehicle.getId() : -1;

        lastTrips = tripDAO.getTripsForInvoice(company.getId(), driverId, vehicleId, month, year);

        if (lastTrips == null || lastTrips.isEmpty()) {
            String periodName = (month <= 0) ? "Year " + year + " (All Months)" : MONTH_NAMES[month] + " " + year;
            JOptionPane.showMessageDialog(this,
                "No completed trips found for " + company.getCompanyName() +
                " in " + periodName + "!",
                "No Records", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double totalAmount = 0;
        for (Trip t : lastTrips) totalAmount += t.getAmount();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceDAO.generateInvoiceNumber());
        invoice.setInvoiceDate(new Date());
        invoice.setCompanyId(company.getId());
        invoice.setCompanyName(company.getCompanyName());
        if (driverId > 0) { invoice.setDriverId(driverId); invoice.setDriverName(driver.getDriverName()); }
        if (vehicleId > 0) { invoice.setVehicleId(vehicleId); invoice.setVehicleNumber(vehicle.getRegistrationNumber()); }
        invoice.setBillingMonth(month <= 0 ? 0 : month);
        invoice.setBillingYear(year);
        invoice.setTotalTrips(lastTrips.size());
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus("Generated");

        if (invoiceDAO.saveInvoice(invoice)) {
            int invoiceId = invoiceDAO.getLastInsertedInvoiceId();
            invoice.setId(invoiceId);

            Payment payment = new Payment();
            payment.setInvoiceId(invoiceId);
            payment.setTotalAmount(totalAmount);
            payment.setAmountReceived(0);
            payment.setRemainingAmount(totalAmount);
            payment.setPaymentStatus("Pending");
            paymentDAO.createPayment(payment);

            lastGeneratedInvoice = invoice;
            loadInvoiceIntoPreview(invoice, lastTrips);

            // Auto-save copies to invoices/
            InvoiceGenerator.autoSaveInvoice(invoice, lastTrips);

            String periodStr = InvoiceGenerator.getBillingPeriodText(invoice.getBillingMonth(), invoice.getBillingYear());

            JOptionPane.showMessageDialog(this,
                "Invoice " + invoice.getInvoiceNumber() + " successfully generated!\n\n" +
                "Client        : " + invoice.getCompanyName() + "\n" +
                "Period        : " + periodStr + "\n" +
                "Trips Billed  : " + lastTrips.size() + "\n" +
                "Total Payable : ₹ " + String.format("%,.2f", totalAmount) + "\n\n" +
                "✓ Automatically archived in ./invoices/ directory.\n" +
                "Click 'Export PDF' to save anywhere or inspect.",
                "Invoice Finalized", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save invoice record in database!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadInvoiceIntoPreview(Invoice invoice, List<Trip> trips) {
        lastGeneratedInvoice = invoice;
        lastTrips = trips;

        exportPDFBtn.setEnabled(true);
        exportTxtBtn.setEnabled(true);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        invoiceNumberLabel.setText(invoice.getInvoiceNumber());
        invoiceDateLabel.setText(sdf.format(invoice.getInvoiceDate()));
        totalTripsLabel.setText((trips != null ? trips.size() : invoice.getTotalTrips()) + " Trips");
        totalAmountLabel.setText("₹ " + String.format("%,.2f", invoice.getTotalAmount()));

        previewTableModel.setRowCount(0);
        if (trips != null) {
            int sr = 1;
            for (Trip t : trips) {
                previewTableModel.addRow(new Object[]{
                    sr++, sdf.format(t.getTripDate()),
                    t.getSourceLocation(), t.getDestination(),
                    String.format("%.2f", t.getAmount())
                });
            }
        }
    }

    private void exportPDF() {
        if (lastGeneratedInvoice == null || lastTrips == null) {
            JOptionPane.showMessageDialog(this, "Please generate or select an invoice first!", "No Invoice Active", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File defaultDir = new File("invoices");
        if (!defaultDir.exists()) defaultDir.mkdirs();

        String fileName = lastGeneratedInvoice.getInvoiceNumber() + "_MOT_Logistics.pdf";
        JFileChooser fileChooser = new JFileChooser(defaultDir);
        fileChooser.setSelectedFile(new File(defaultDir, fileName));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            boolean success = InvoiceGenerator.generatePDF(lastGeneratedInvoice, lastTrips, filePath);

            if (success) {
                // Also ensure saved in invoices/
                InvoiceGenerator.autoSaveInvoice(lastGeneratedInvoice, lastTrips);

                int openChoice = JOptionPane.showConfirmDialog(this,
                    "PDF Invoice generated successfully!\n\n" +
                    "File: " + new File(filePath).getName() + "\n" +
                    "Path: " + filePath + "\n\n" +
                    "✓ Also archived in ./invoices/ directory.\n\n" +
                    "Would you like to open the PDF file now?",
                    "PDF Export Successful", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (openChoice == JOptionPane.YES_OPTION) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(new File(filePath));
                        }
                    } catch (Exception ex) {
                        System.out.println("Could not launch PDF viewer: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export PDF invoice file!", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportText() {
        if (lastGeneratedInvoice == null || lastTrips == null) {
            JOptionPane.showMessageDialog(this, "Please generate or select an invoice first!", "No Invoice Active", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File defaultDir = new File("invoices");
        if (!defaultDir.exists()) defaultDir.mkdirs();

        String fileName = lastGeneratedInvoice.getInvoiceNumber() + "_MOT_Logistics.txt";
        JFileChooser fileChooser = new JFileChooser(defaultDir);
        fileChooser.setSelectedFile(new File(defaultDir, fileName));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".txt")) {
                filePath += ".txt";
            }

            boolean success = InvoiceGenerator.generateTextInvoice(lastGeneratedInvoice, lastTrips, filePath);

            if (success) {
                InvoiceGenerator.autoSaveInvoice(lastGeneratedInvoice, lastTrips);
                JOptionPane.showMessageDialog(this,
                    "Text Invoice voucher saved successfully!\n\n" +
                    "File: " + new File(filePath).getName() + "\n" +
                    "Path: " + filePath + "\n\n" +
                    "✓ Also archived in ./invoices/ directory.",
                    "File Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export text invoice file!", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Bulk exports all generated invoices in the database to the invoices/ folder */
    private void bulkExportAllInvoices() {
        List<Invoice> invoices = invoiceDAO.getAllInvoices();
        if (invoices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No invoices found to export!", "Empty Archive", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int count = 0;
        File invoicesDir = new File("invoices");
        if (!invoicesDir.exists()) invoicesDir.mkdirs();

        for (Invoice inv : invoices) {
            List<Trip> trips = tripDAO.getTripsForInvoice(
                inv.getCompanyId(),
                inv.getDriverId(),
                inv.getVehicleId(),
                inv.getBillingMonth(),
                inv.getBillingYear()
            );
            InvoiceGenerator.autoSaveInvoice(inv, trips);
            count++;
        }

        JOptionPane.showMessageDialog(this,
            "Bulk Export Completed Successfully!\n\n" +
            "Total Invoices Exported: " + count + "\n" +
            "Formats: PDF documents (.pdf) & Text vouchers (.txt)\n" +
            "Saved Location: " + invoicesDir.getAbsolutePath(),
            "Bulk Export Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Interactive Historical Archive Dialog with Download capabilities */
    private void showInvoiceHistory() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "MOT Logistics - Historical Invoice Archive", true);
        dialog.setSize(920, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = new JPanel(new BorderLayout(8, 8));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contentPanel.setBackground(UITheme.BG_LIGHT);

        List<Invoice> invoices = invoiceDAO.getAllInvoices();
        String[] cols = {"ID", "Invoice No.", "Date", "Company", "Driver", "Period", "Trips", "Amount (₹)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Invoice inv : invoices) {
            String period = InvoiceGenerator.getBillingPeriodText(inv.getBillingMonth(), inv.getBillingYear());
            String driver = inv.getDriverName() != null ? inv.getDriverName() : "Fleet Operations";
            model.addRow(new Object[]{
                inv.getId(),
                inv.getInvoiceNumber(),
                sdf.format(inv.getInvoiceDate()),
                inv.getCompanyName(),
                driver,
                period,
                inv.getTotalTrips(),
                "₹ " + String.format("%,.2f", inv.getTotalAmount()),
                inv.getStatus()
            });
        }

        JTable histTable = new JTable(model);
        UITheme.styleTable(histTable);
        JScrollPane sp = new JScrollPane(histTable);

        // Action Toolbar
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actionPanel.setBackground(UITheme.CARD_BG);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        ModernButton dlPdfBtn = new ModernButton("📥 Download PDF", UITheme.BRAND_BLUE);
        ModernButton dlTxtBtn = new ModernButton("📄 Download Text", UITheme.SECONDARY_GRAY);
        ModernButton viewBtn  = new ModernButton("👁️ View in Ledger", UITheme.AMBER_ORANGE);
        ModernButton bulkBtn  = new ModernButton("📦 Export All to invoices/", UITheme.SUCCESS_GREEN);
        ModernButton closeBtn = new ModernButton("Close", UITheme.DANGER_RED);

        actionPanel.add(dlPdfBtn);
        actionPanel.add(dlTxtBtn);
        actionPanel.add(viewBtn);
        actionPanel.add(bulkBtn);
        actionPanel.add(closeBtn);

        dlPdfBtn.addActionListener(e -> {
            int row = histTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select an invoice from the list!", "Select Invoice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int invId = (int) model.getValueAt(row, 0);
            Invoice inv = invoiceDAO.getInvoiceById(invId);
            if (inv != null) {
                List<Trip> trips = tripDAO.getTripsForInvoice(inv.getCompanyId(), inv.getDriverId(), inv.getVehicleId(), inv.getBillingMonth(), inv.getBillingYear());
                File dir = new File("invoices");
                if (!dir.exists()) dir.mkdirs();
                String path = "invoices/" + inv.getInvoiceNumber() + "_MOT_Logistics.pdf";
                if (InvoiceGenerator.generatePDF(inv, trips, path)) {
                    JOptionPane.showMessageDialog(dialog, "PDF Invoice downloaded successfully to:\n" + new File(path).getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to download PDF invoice!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dlTxtBtn.addActionListener(e -> {
            int row = histTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select an invoice from the list!", "Select Invoice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int invId = (int) model.getValueAt(row, 0);
            Invoice inv = invoiceDAO.getInvoiceById(invId);
            if (inv != null) {
                List<Trip> trips = tripDAO.getTripsForInvoice(inv.getCompanyId(), inv.getDriverId(), inv.getVehicleId(), inv.getBillingMonth(), inv.getBillingYear());
                File dir = new File("invoices");
                if (!dir.exists()) dir.mkdirs();
                String path = "invoices/" + inv.getInvoiceNumber() + "_MOT_Logistics.txt";
                if (InvoiceGenerator.generateTextInvoice(inv, trips, path)) {
                    JOptionPane.showMessageDialog(dialog, "Text Invoice voucher downloaded successfully to:\n" + new File(path).getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to download Text invoice!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        viewBtn.addActionListener(e -> {
            int row = histTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select an invoice from the list!", "Select Invoice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int invId = (int) model.getValueAt(row, 0);
            Invoice inv = invoiceDAO.getInvoiceById(invId);
            if (inv != null) {
                List<Trip> trips = tripDAO.getTripsForInvoice(inv.getCompanyId(), inv.getDriverId(), inv.getVehicleId(), inv.getBillingMonth(), inv.getBillingYear());
                loadInvoiceIntoPreview(inv, trips);
                dialog.dispose();
            }
        });

        bulkBtn.addActionListener(e -> {
            bulkExportAllInvoices();
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(actionPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
}
