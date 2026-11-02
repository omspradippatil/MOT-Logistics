package com.transport.ui;

import com.transport.dao.InvoiceDAO;
import com.transport.dao.TripDAO;
import com.transport.model.Admin;
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
 * CompanyMonthlyInvoicePanel.java - Monthly Billing Engine for Company Admin
 *
 * Implements:
 * 1. Month and Year selection
 * 2. Real-time preview of completed trips for that month
 * 3. Total trips and total payable amount calculation
 * 4. Invoice generation and persistence in database
 * 5. Direct PDF download
 * 6. Historical invoice archive for the company with instant PDF export
 */
public class CompanyMonthlyInvoicePanel extends JPanel implements Refreshable {

    private Admin companyAdmin;

    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;

    private JTable            previewTable;
    private DefaultTableModel previewTableModel;
    private JLabel            previewSummaryLabel;

    private JTable            archiveTable;
    private DefaultTableModel archiveTableModel;

    private TripDAO    tripDAO    = new TripDAO();
    private InvoiceDAO invoiceDAO = new InvoiceDAO();

    private List<Trip>    currentPreviewTrips = new ArrayList<>();
    private List<Invoice> companyInvoices     = new ArrayList<>();

    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public CompanyMonthlyInvoicePanel(Admin companyAdmin) {
        this.companyAdmin = companyAdmin;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadPreviewTrips();
        loadInvoiceArchive();
    }

    private void initUI() {
        // Selection Controls Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(UITheme.CARD_BG);
        topBar.setBorder(UITheme.createCardBorder("Monthly Invoice Generator - " + companyAdmin.getCompanyName()));

        topBar.add(new JLabel("Select Billing Month:"));
        monthCombo = new JComboBox<>(MONTHS);
        monthCombo.setSelectedIndex(10); // Default to November
        monthCombo.setFont(UITheme.FONT_BODY_BOLD);
        monthCombo.setPreferredSize(new Dimension(140, 32));
        topBar.add(monthCombo);

        topBar.add(new JLabel("Select Year:"));
        String[] years = {"2024", "2025", "2026", "2027", "2028"};
        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem("2026");
        yearCombo.setFont(UITheme.FONT_BODY_BOLD);
        yearCombo.setPreferredSize(new Dimension(100, 32));
        topBar.add(yearCombo);

        ModernButton previewBtn = new ModernButton("🔍 Preview Trips & Calculate", UITheme.BRAND_BLUE);
        previewBtn.setPreferredSize(new Dimension(220, 34));
        previewBtn.addActionListener(e -> loadPreviewTrips());
        topBar.add(previewBtn);

        // Center Tabs: Tab 1 = Current Month Preview & Generation; Tab 2 = Previous Invoices Archive
        JTabbedPane innerTabs = new JTabbedPane();
        innerTabs.setFont(UITheme.FONT_BODY_BOLD);

        innerTabs.addTab("  📄 Monthly Trips Preview & Generate  ", createPreviewTab());
        innerTabs.addTab("  🗄️ Generated Invoices Archive  ",      createArchiveTab());

        add(topBar,    BorderLayout.NORTH);
        add(innerTabs, BorderLayout.CENTER);
    }

    private JPanel createPreviewTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Preview Table
        String[] cols = {
            "Trip ID", "Trip Date", "Driver (Created By)", "Vehicle Reg.", "From (Source)",
            "To (Destination)", "Cargo / Goods", "Trip Amount (₹)", "Status"
        };
        previewTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        previewTable = new JTable(previewTableModel);
        UITheme.styleTable(previewTable);

        // Set column widths so content and headers never truncate
        previewTable.getColumnModel().getColumn(0).setPreferredWidth(85);   // Trip ID
        previewTable.getColumnModel().getColumn(1).setPreferredWidth(95);   // Trip Date
        previewTable.getColumnModel().getColumn(2).setPreferredWidth(140);  // Driver (Created By)
        previewTable.getColumnModel().getColumn(3).setPreferredWidth(125);  // Vehicle Reg.
        previewTable.getColumnModel().getColumn(4).setPreferredWidth(110);  // From (Source)
        previewTable.getColumnModel().getColumn(5).setPreferredWidth(110);  // To (Destination)
        previewTable.getColumnModel().getColumn(6).setPreferredWidth(170);  // Cargo / Goods
        previewTable.getColumnModel().getColumn(7).setPreferredWidth(120);  // Trip Amount (₹)
        previewTable.getColumnModel().getColumn(8).setPreferredWidth(90);   // Status

        previewTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && previewTable.getSelectedRow() >= 0) {
                    showPreviewTripDetails(previewTable.getSelectedRow());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setBorder(UITheme.createCardBorder("Completed Trips for Selected Month"));

        // Bottom Action Bar - 2-tier layout so summary and buttons NEVER overlap
        JPanel bottomBar = new JPanel(new BorderLayout(8, 8));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Tier 1: Summary Card Banner (occupies full width above buttons)
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

        // Tier 2: Action Buttons (right-aligned on their own dedicated row)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        btnPanel.setOpaque(false);

        ModernButton generateBtn = new ModernButton("✓  Generate Monthly Invoice", UITheme.SUCCESS_GREEN);
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generateBtn.setPreferredSize(new Dimension(230, 38));
        generateBtn.addActionListener(e -> generateMonthlyInvoice());

        ModernButton downloadPdfBtn = new ModernButton("📥 Download Invoice (PDF)", UITheme.BRAND_BLUE);
        downloadPdfBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        downloadPdfBtn.setPreferredSize(new Dimension(210, 38));
        downloadPdfBtn.addActionListener(e -> downloadCurrentInvoicePDF());

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

        String[] cols = {
            "ID", "Invoice Number", "Invoice Date", "Billing Period",
            "Total Trips", "Total Amount (₹)", "Amount Paid (₹)", "Remaining (₹)", "Status"
        };
        archiveTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        archiveTable = new JTable(archiveTableModel);
        UITheme.styleTable(archiveTable);

        // Hide internal ID
        archiveTable.getColumnModel().getColumn(0).setMinWidth(0);
        archiveTable.getColumnModel().getColumn(0).setMaxWidth(0);
        archiveTable.getColumnModel().getColumn(0).setWidth(0);

        archiveTable.getColumnModel().getColumn(1).setPreferredWidth(130); // Invoice No
        archiveTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Date
        archiveTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Period
        archiveTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Trips
        archiveTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Total
        archiveTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Paid
        archiveTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Remaining
        archiveTable.getColumnModel().getColumn(8).setPreferredWidth(110); // Status

        JScrollPane scrollPane = new JScrollPane(archiveTable);
        scrollPane.setBorder(UITheme.createCardBorder("Previously Generated Company Invoices"));

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        ModernButton downloadArchiveBtn = new ModernButton("📥 Download Selected Invoice (PDF)", UITheme.BRAND_BLUE);
        downloadArchiveBtn.setPreferredSize(new Dimension(260, 36));
        downloadArchiveBtn.addActionListener(e -> downloadSelectedArchiveInvoicePDF());

        ModernButton refreshArchiveBtn = new ModernButton("↺ Refresh Archive", UITheme.SECONDARY_GRAY);
        refreshArchiveBtn.setPreferredSize(new Dimension(140, 36));
        refreshArchiveBtn.addActionListener(e -> loadInvoiceArchive());

        bottomBar.add(downloadArchiveBtn);
        bottomBar.add(refreshArchiveBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomBar,  BorderLayout.SOUTH);
        return panel;
    }

    private void loadPreviewTrips() {
        previewTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        int month = monthCombo.getSelectedIndex() + 1; // 1 to 12
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());

        // STRICT ISOLATION: company_id is fixed to companyAdmin.getCompanyId()
        currentPreviewTrips = tripDAO.getTripsForInvoice(companyAdmin.getCompanyId(), -1, -1, month, year);

        double totalAmount = 0.0;
        java.util.Set<String> driversInvolved = new java.util.HashSet<>();
        for (Trip t : currentPreviewTrips) {
            totalAmount += t.getAmount();
            if (t.getDriverName() != null) driversInvolved.add(t.getDriverName());
            previewTableModel.addRow(new Object[]{
                t.getTripCode(),
                sdf.format(t.getTripDate()),
                t.getDriverName(),
                t.getVehicleNumber(),
                t.getSourceLocation(),
                t.getDestination(),
                t.getGoodsDescription() != null ? t.getGoodsDescription() : "-",
                String.format("₹ %,.2f", t.getAmount()),
                t.getTripStatus()
            });
        }

        previewSummaryLabel.setText("Billing Month: " + MONTHS[month - 1] + " " + year +
                "   |   Completed Trips: " + currentPreviewTrips.size() +
                "   |   Drivers Engaged: " + driversInvolved.size() +
                "   |   Total Payable: ₹ " + String.format("%,.2f", totalAmount));
    }

    private void showPreviewTripDetails(int row) {
        String tripCode = (String) previewTableModel.getValueAt(row, 0);
        String tripDate = (String) previewTableModel.getValueAt(row, 1);
        String driver   = (String) previewTableModel.getValueAt(row, 2);
        String vehicle  = (String) previewTableModel.getValueAt(row, 3);
        String from     = (String) previewTableModel.getValueAt(row, 4);
        String to       = (String) previewTableModel.getValueAt(row, 5);
        String goods    = (String) previewTableModel.getValueAt(row, 6);
        String amount   = (String) previewTableModel.getValueAt(row, 7);
        String status   = (String) previewTableModel.getValueAt(row, 8);

        String msg =
            "====================================================\n" +
            "       MONTHLY INVOICE TRIP & DRIVER VERIFICATION   \n" +
            "====================================================\n\n" +
            "Trip Reference ID    : " + tripCode + "\n" +
            "Trip Date            : " + tripDate + "\n" +
            "Client Company       : " + companyAdmin.getCompanyName() + "\n\n" +
            "DRIVER ATTRIBUTION & CONNECTION:\n" +
            "Created By Driver    : " + driver + "\n" +
            "Assigned Vehicle Reg.: " + vehicle + "\n\n" +
            "ROUTE & BILLING:\n" +
            "Pickup Location      : " + from + "\n" +
            "Drop Destination     : " + to + "\n" +
            "Cargo / Goods        : " + goods + "\n" +
            "Trip Fare Amount     : " + amount + "\n" +
            "Status               : " + status + "\n\n" +
            "====================================================";

        JOptionPane.showMessageDialog(this, msg, "Trip Details - " + tripCode, JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadInvoiceArchive() {
        archiveTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // STRICT ISOLATION: company_id = companyAdmin.getCompanyId()
        companyInvoices = invoiceDAO.getInvoicesByCompany(companyAdmin.getCompanyId());

        for (Invoice inv : companyInvoices) {
            archiveTableModel.addRow(new Object[]{
                inv.getId(),
                inv.getInvoiceNumber(),
                sdf.format(inv.getInvoiceDate()),
                MONTHS[Math.max(0, inv.getBillingMonth() - 1)] + " " + inv.getBillingYear(),
                inv.getTotalTrips(),
                String.format("₹ %,.2f", inv.getTotalAmount()),
                String.format("₹ %,.2f", inv.getAmountPaid()),
                String.format("₹ %,.2f", inv.getRemainingAmount()),
                inv.getStatus()
            });
        }
    }

    private void generateMonthlyInvoice() {
        if (currentPreviewTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips found for the selected month to generate an invoice.", "No Trips", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int month = monthCombo.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());

        double totalAmount = 0.0;
        for (Trip t : currentPreviewTrips) totalAmount += t.getAmount();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceDAO.generateInvoiceNumber());
        invoice.setInvoiceDate(new Date());
        invoice.setCompanyId(companyAdmin.getCompanyId());
        invoice.setCompanyName(companyAdmin.getCompanyName());
        invoice.setBillingMonth(month);
        invoice.setBillingYear(year);
        invoice.setTotalTrips(currentPreviewTrips.size());
        invoice.setTotalAmount(totalAmount);
        invoice.setAmountPaid(0.0);
        invoice.setRemainingAmount(totalAmount);
        invoice.setInvoiceType("Company");
        invoice.setStatus("Pending");

        if (invoiceDAO.saveInvoice(invoice)) {
            JOptionPane.showMessageDialog(this,
                "Monthly Invoice Generated Successfully!\n\n" +
                "Invoice Number  : " + invoice.getInvoiceNumber() + "\n" +
                "Company         : " + companyAdmin.getCompanyName() + "\n" +
                "Billing Period  : " + MONTHS[month - 1] + " " + year + "\n" +
                "Total Trips     : " + invoice.getTotalTrips() + "\n" +
                "Total Payable   : ₹ " + String.format("%,.2f", totalAmount),
                "Invoice Created", JOptionPane.INFORMATION_MESSAGE);

            loadInvoiceArchive();
        } else {
            JOptionPane.showMessageDialog(this, "Database error: Failed to save invoice.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadCurrentInvoicePDF() {
        if (currentPreviewTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips to generate PDF for the selected month.", "No Trips", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int month = monthCombo.getSelectedIndex() + 1;
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());
        double totalAmount = 0.0;
        for (Trip t : currentPreviewTrips) totalAmount += t.getAmount();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + year + "-" + String.format("%02d", month));
        invoice.setInvoiceDate(new Date());
        invoice.setCompanyId(companyAdmin.getCompanyId());
        invoice.setCompanyName(companyAdmin.getCompanyName());
        invoice.setBillingMonth(month);
        invoice.setBillingYear(year);
        invoice.setTotalTrips(currentPreviewTrips.size());
        invoice.setTotalAmount(totalAmount);
        invoice.setInvoiceType("Company");
        invoice.setStatus("Generated");

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Invoice_" + companyAdmin.getCompanyName().replaceAll("\\s+", "_") + "_" + MONTHS[month - 1] + "_" + year + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (InvoiceGenerator.generatePDF(invoice, currentPreviewTrips, path)) {
                JOptionPane.showMessageDialog(this, "Invoice PDF saved successfully!\n" + path, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate PDF.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void downloadSelectedArchiveInvoicePDF() {
        int row = archiveTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the archive table first.", "Select Invoice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int invoiceId = (int) archiveTableModel.getValueAt(row, 0);
        Invoice inv = invoiceDAO.getInvoiceById(invoiceId);
        if (inv == null) return;

        List<Trip> trips = tripDAO.getTripsForInvoice(companyAdmin.getCompanyId(), -1, -1, inv.getBillingMonth(), inv.getBillingYear());

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Invoice_" + inv.getInvoiceNumber() + "_" + companyAdmin.getCompanyName().replaceAll("\\s+", "_") + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (InvoiceGenerator.generatePDF(inv, trips, path)) {
                JOptionPane.showMessageDialog(this, "Archived Invoice PDF saved successfully!\n" + path, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate PDF.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refreshData() {
        loadPreviewTrips();
        loadInvoiceArchive();
    }
}
