package com.transport.ui;

import com.transport.dao.CompanyDAO;
import com.transport.dao.InvoiceDAO;
import com.transport.dao.TripDAO;
import com.transport.model.Company;
import com.transport.model.Driver;
import com.transport.model.Invoice;
import com.transport.model.Trip;
import com.transport.util.InvoiceGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DriverMonthlyInvoicesPanel.java - Driver-Wise Monthly Invoice Generation & Download
 *
 * Implements Module 5 for Drivers:
 * 1. Select Company (e.g. Om Sai, Oracle)
 * 2. Select Billing Month & Year
 * 3. View all trips completed by that driver for that company in that month
 * 4. See total payable amount
 * 5. Generate Driver-wise Invoice
 * 6. Download Invoice as PDF
 * 7. View previously generated driver invoices
 */
public class DriverMonthlyInvoicesPanel extends JPanel implements Refreshable {

    private Driver driver;

    private JComboBox<Company> companyCombo;
    private JComboBox<String>  monthCombo;
    private JComboBox<String>  yearCombo;

    private JTable            previewTable;
    private DefaultTableModel previewTableModel;
    private JLabel            previewSummaryLabel;

    private JTable            archiveTable;
    private DefaultTableModel archiveTableModel;

    private TripDAO    tripDAO    = new TripDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private InvoiceDAO invoiceDAO = new InvoiceDAO();

    private List<Trip>    currentTrips   = new ArrayList<>();
    private List<Invoice> driverInvoices = new ArrayList<>();

    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public DriverMonthlyInvoicesPanel(Driver driver) {
        this.driver = driver;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadCompanies();
        loadTrips();
        loadInvoiceArchive();
    }

    private void initUI() {
        // Selection Toolbar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topBar.setBackground(UITheme.CARD_BG);
        topBar.setBorder(UITheme.createCardBorder("Driver Monthly Billing Statement Generator"));

        topBar.add(new JLabel("Select Company:"));
        companyCombo = new JComboBox<>();
        companyCombo.setFont(UITheme.FONT_BODY_BOLD);
        companyCombo.setPreferredSize(new Dimension(170, 32));
        topBar.add(companyCombo);

        topBar.add(new JLabel("Billing Month:"));
        monthCombo = new JComboBox<>(MONTHS);
        monthCombo.setSelectedIndex(10); // November
        monthCombo.setFont(UITheme.FONT_BODY);
        monthCombo.setPreferredSize(new Dimension(130, 32));
        topBar.add(monthCombo);

        topBar.add(new JLabel("Year:"));
        String[] years = {"2024", "2025", "2026", "2027"};
        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem("2026");
        yearCombo.setFont(UITheme.FONT_BODY);
        yearCombo.setPreferredSize(new Dimension(90, 32));
        topBar.add(yearCombo);

        ModernButton viewBtn = new ModernButton("🔍 View Trips", UITheme.BRAND_BLUE);
        viewBtn.setPreferredSize(new Dimension(130, 34));
        viewBtn.addActionListener(e -> loadTrips());
        topBar.add(viewBtn);

        // Center Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);

        tabs.addTab("  📄 Monthly Trips Preview & Invoicing  ", createPreviewTab());
        tabs.addTab("  🗄️ My Invoices Archive  ",               createArchiveTab());

        add(topBar, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
    }

    private JPanel createPreviewTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Trip Code", "Date", "Vehicle No", "From (Source)", "To (Destination)", "Cargo / Goods", "Amount (₹)", "Status"};
        previewTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        previewTable = new JTable(previewTableModel);
        UITheme.styleTable(previewTable);

        // Set column widths so content and headers never truncate
        previewTable.getColumnModel().getColumn(0).setPreferredWidth(85);   // Trip Code
        previewTable.getColumnModel().getColumn(1).setPreferredWidth(95);   // Date
        previewTable.getColumnModel().getColumn(2).setPreferredWidth(125);  // Vehicle No
        previewTable.getColumnModel().getColumn(3).setPreferredWidth(110);  // From
        previewTable.getColumnModel().getColumn(4).setPreferredWidth(110);  // To
        previewTable.getColumnModel().getColumn(5).setPreferredWidth(170);  // Cargo
        previewTable.getColumnModel().getColumn(6).setPreferredWidth(120);  // Amount
        previewTable.getColumnModel().getColumn(7).setPreferredWidth(90);   // Status

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setBorder(UITheme.createCardBorder("Trips Completed by " + driver.getDriverName() + " for Selected Company"));

        // Bottom Bar - 2-tier layout so summary and buttons NEVER overlap
        JPanel bottomBar = new JPanel(new BorderLayout(8, 8));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Tier 1: Summary Banner
        JPanel summaryCard = new JPanel(new BorderLayout());
        summaryCard.setBackground(new Color(241, 245, 249));
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.BRAND_BLUE),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        previewSummaryLabel = new JLabel("Total Trips: 0  |  Total Payable Amount: ₹ 0.00");
        previewSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        previewSummaryLabel.setForeground(UITheme.BRAND_BLUE);
        summaryCard.add(previewSummaryLabel, BorderLayout.CENTER);

        // Tier 2: Buttons Bar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        btnPanel.setOpaque(false);

        ModernButton generateBtn = new ModernButton("✓  Generate Driver Invoice", UITheme.SUCCESS_GREEN);
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generateBtn.setPreferredSize(new Dimension(220, 38));
        generateBtn.addActionListener(e -> generateDriverInvoice());

        ModernButton downloadPdfBtn = new ModernButton("📥 Download Invoice (PDF)", UITheme.BRAND_BLUE);
        downloadPdfBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        downloadPdfBtn.setPreferredSize(new Dimension(210, 38));
        downloadPdfBtn.addActionListener(e -> downloadPreviewInvoicePDF());

        btnPanel.add(generateBtn);
        btnPanel.add(downloadPdfBtn);

        bottomBar.add(summaryCard, BorderLayout.NORTH);
        bottomBar.add(btnPanel,    BorderLayout.SOUTH);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomBar,  BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createArchiveTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Invoice Number", "Date", "Company", "Period", "Trips", "Total Amount (₹)", "Paid (₹)", "Status"};
        archiveTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        archiveTable = new JTable(archiveTableModel);
        UITheme.styleTable(archiveTable);

        archiveTable.getColumnModel().getColumn(0).setMinWidth(0);
        archiveTable.getColumnModel().getColumn(0).setMaxWidth(0);
        archiveTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(archiveTable);
        scrollPane.setBorder(UITheme.createCardBorder("My Historical Invoices"));

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        ModernButton downloadArchiveBtn = new ModernButton("📥 Download Selected Invoice (PDF)", UITheme.BRAND_BLUE);
        downloadArchiveBtn.setPreferredSize(new Dimension(260, 36));
        downloadArchiveBtn.addActionListener(e -> downloadArchiveInvoicePDF());

        ModernButton refreshArchiveBtn = new ModernButton("↺ Refresh Archive", UITheme.SECONDARY_GRAY);
        refreshArchiveBtn.setPreferredSize(new Dimension(140, 36));
        refreshArchiveBtn.addActionListener(e -> loadInvoiceArchive());

        bottomBar.add(downloadArchiveBtn);
        bottomBar.add(refreshArchiveBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomBar,  BorderLayout.SOUTH);
        return panel;
    }

    private void loadCompanies() {
        companyCombo.removeAllItems();
        List<Company> list = companyDAO.getAllCompanies();
        for (Company c : list) {
            companyCombo.addItem(c);
        }
    }

    public void loadTrips() {
        previewTableModel.setRowCount(0);
        if (companyCombo.getSelectedItem() == null) return;

        Company comp = (Company) companyCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());

        // Get trips for this driver and this company
        currentTrips = tripDAO.getTripsForInvoice(comp.getId(), driver.getId(), -1, month, year);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        double totalAmount = 0.0;
        for (Trip t : currentTrips) {
            totalAmount += t.getAmount();
            previewTableModel.addRow(new Object[]{
                t.getTripCode(),
                sdf.format(t.getTripDate()),
                t.getVehicleNumber(),
                t.getSourceLocation(),
                t.getDestination(),
                t.getGoodsDescription() != null ? t.getGoodsDescription() : "-",
                String.format("₹ %,.2f", t.getAmount()),
                t.getTripStatus()
            });
        }

        previewSummaryLabel.setText(comp.getCompanyName() + " (" + MONTHS[month - 1] + " " + year + ")  |  Total Trips: " +
                currentTrips.size() + "  |  Total Payable Amount: ₹ " + String.format("%,.2f", totalAmount));
    }

    public void loadInvoiceArchive() {
        archiveTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        driverInvoices = invoiceDAO.getInvoicesByDriver(driver.getId());
        for (Invoice inv : driverInvoices) {
            archiveTableModel.addRow(new Object[]{
                inv.getId(),
                inv.getInvoiceNumber(),
                sdf.format(inv.getInvoiceDate()),
                inv.getCompanyName(),
                MONTHS[Math.max(0, inv.getBillingMonth() - 1)] + " " + inv.getBillingYear(),
                inv.getTotalTrips(),
                String.format("₹ %,.2f", inv.getTotalAmount()),
                String.format("₹ %,.2f", inv.getAmountPaid()),
                inv.getStatus()
            });
        }
    }

    private void generateDriverInvoice() {
        if (currentTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips completed for the selected company and month.", "No Trips", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Company comp = (Company) companyCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());

        double total = 0.0;
        for (Trip t : currentTrips) total += t.getAmount();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-DRV-" + driver.getId() + "-" + String.format("%02d", month));
        invoice.setInvoiceDate(new Date());
        invoice.setCompanyId(comp.getId());
        invoice.setCompanyName(comp.getCompanyName());
        invoice.setDriverId(driver.getId());
        invoice.setDriverName(driver.getDriverName());
        invoice.setBillingMonth(month);
        invoice.setBillingYear(year);
        invoice.setTotalTrips(currentTrips.size());
        invoice.setTotalAmount(total);
        invoice.setAmountPaid(0.0);
        invoice.setRemainingAmount(total);
        invoice.setInvoiceType("Driver");
        invoice.setStatus("Generated");

        if (invoiceDAO.saveInvoice(invoice)) {
            JOptionPane.showMessageDialog(this,
                "Driver Monthly Invoice Generated Successfully!\n\n" +
                "Invoice Number  : " + invoice.getInvoiceNumber() + "\n" +
                "Company         : " + comp.getCompanyName() + "\n" +
                "Billing Period  : " + MONTHS[month - 1] + " " + year + "\n" +
                "Total Trips     : " + invoice.getTotalTrips() + "\n" +
                "Total Earnings  : ₹ " + String.format("%,.2f", total),
                "Invoice Created", JOptionPane.INFORMATION_MESSAGE);

            loadInvoiceArchive();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save driver invoice.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadPreviewInvoicePDF() {
        if (currentTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips to generate PDF for.", "No Trips", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Company comp = (Company) companyCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());

        double total = 0.0;
        for (Trip t : currentTrips) total += t.getAmount();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + driver.getDriverName().toUpperCase() + "-" + year + "-" + String.format("%02d", month));
        invoice.setInvoiceDate(new Date());
        invoice.setCompanyId(comp.getId());
        invoice.setCompanyName(comp.getCompanyName());
        invoice.setDriverId(driver.getId());
        invoice.setDriverName(driver.getDriverName());
        invoice.setBillingMonth(month);
        invoice.setBillingYear(year);
        invoice.setTotalTrips(currentTrips.size());
        invoice.setTotalAmount(total);
        invoice.setInvoiceType("Driver");
        invoice.setStatus("Generated");

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Invoice_" + driver.getDriverName() + "_" + comp.getCompanyName().replaceAll("\\s+", "_") + "_" + MONTHS[month - 1] + "_" + year + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (InvoiceGenerator.generatePDF(invoice, currentTrips, path)) {
                JOptionPane.showMessageDialog(this, "Invoice PDF saved successfully!\n" + path, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate PDF.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void downloadArchiveInvoicePDF() {
        int row = archiveTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the archive table first.", "Select Invoice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int invoiceId = (int) archiveTableModel.getValueAt(row, 0);
        Invoice inv = invoiceDAO.getInvoiceById(invoiceId);
        if (inv == null) return;

        List<Trip> trips = tripDAO.getTripsForInvoice(inv.getCompanyId(), driver.getId(), -1, inv.getBillingMonth(), inv.getBillingYear());

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Invoice_" + inv.getInvoiceNumber() + "_" + driver.getDriverName() + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (InvoiceGenerator.generatePDF(inv, trips, path)) {
                JOptionPane.showMessageDialog(this, "Invoice PDF saved successfully!\n" + path, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate PDF.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refreshData() {
        loadCompanies();
        loadTrips();
        loadInvoiceArchive();
    }
}
