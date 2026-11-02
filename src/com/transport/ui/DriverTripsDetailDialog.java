package com.transport.ui;

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
import java.util.Date;
import java.util.List;

/**
 * DriverTripsDetailDialog.java - Company Admin's view of a specific driver's trips
 *
 * Displays ONLY trips completed by this driver for this company.
 * Includes a prominent "Download Invoice" button as required.
 */
public class DriverTripsDetailDialog extends JDialog {

    private Admin companyAdmin;
    private int   driverId;
    private String driverName;
    private String vehicleNumbers;

    private JTable            table;
    private DefaultTableModel tableModel;
    private JLabel            totalTripsLabel;
    private JLabel            totalAmountLabel;

    private TripDAO    tripDAO = new TripDAO();
    private List<Trip> trips;

    public DriverTripsDetailDialog(Frame owner, Admin companyAdmin, int driverId, String driverName, String vehicleNumbers) {
        super(owner, "Trips by Driver: " + driverName + " (" + companyAdmin.getCompanyName() + ")", true);
        this.companyAdmin   = companyAdmin;
        this.driverId       = driverId;
        this.driverName     = driverName;
        this.vehicleNumbers = vehicleNumbers;

        setSize(950, 560);
        setLocationRelativeTo(owner);

        initUI();
        loadTrips();
    }

    private void initUI() {
        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        contentPanel.setBackground(UITheme.BG_LIGHT);

        // Header Info Card
        JPanel headerPanel = new JPanel(new GridLayout(2, 2, 8, 4));
        headerPanel.setBackground(UITheme.CARD_BG);
        headerPanel.setBorder(UITheme.createCardBorder("Driver Profile & Fleet Engagement"));

        JLabel dLabel = new JLabel("Driver Name: " + driverName);
        dLabel.setFont(UITheme.FONT_BODY_BOLD);

        JLabel vLabel = new JLabel("Vehicles Used: " + (vehicleNumbers != null ? vehicleNumbers : "N/A"));
        vLabel.setFont(UITheme.FONT_BODY);

        JLabel cLabel = new JLabel("Client Company: " + companyAdmin.getCompanyName());
        cLabel.setFont(UITheme.FONT_BODY_BOLD);
        cLabel.setForeground(UITheme.BRAND_BLUE);

        totalTripsLabel = new JLabel("Total Trips: Loading...");
        totalTripsLabel.setFont(UITheme.FONT_BODY_BOLD);

        headerPanel.add(dLabel);
        headerPanel.add(cLabel);
        headerPanel.add(vLabel);
        headerPanel.add(totalTripsLabel);

        // Table
        String[] cols = {
            "Trip ID", "Vehicle No", "Trip Date", "Source", "Destination",
            "Cargo / Goods", "Amount (₹)", "Trip Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(85);   // Trip ID
        table.getColumnModel().getColumn(1).setPreferredWidth(125);  // Vehicle No
        table.getColumnModel().getColumn(2).setPreferredWidth(95);   // Date
        table.getColumnModel().getColumn(3).setPreferredWidth(110);  // Source
        table.getColumnModel().getColumn(4).setPreferredWidth(110);  // Destination
        table.getColumnModel().getColumn(5).setPreferredWidth(160);  // Cargo
        table.getColumnModel().getColumn(6).setPreferredWidth(110);  // Amount
        table.getColumnModel().getColumn(7).setPreferredWidth(90);   // Status

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Trips Completed for " + companyAdmin.getCompanyName()));

        // Bottom Summary & Action Bar - 2-tier layout so summary and buttons NEVER overlap
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBackground(UITheme.CARD_BG);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Tier 1: Summary Card
        JPanel summaryCard = new JPanel(new BorderLayout());
        summaryCard.setBackground(new Color(241, 245, 249));
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.BRAND_BLUE),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        totalAmountLabel = new JLabel("Total Payable: ₹ 0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalAmountLabel.setForeground(UITheme.BRAND_BLUE);
        summaryCard.add(totalAmountLabel, BorderLayout.CENTER);

        // Tier 2: Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnBar.setOpaque(false);

        ModernButton downloadInvoiceBtn = new ModernButton("📥 Download Invoice (PDF)", UITheme.SUCCESS_GREEN);
        downloadInvoiceBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        downloadInvoiceBtn.setPreferredSize(new Dimension(210, 38));
        downloadInvoiceBtn.addActionListener(e -> downloadDriverInvoicePDF());

        ModernButton closeBtn = new ModernButton("Close", UITheme.SECONDARY_GRAY);
        closeBtn.setPreferredSize(new Dimension(100, 38));
        closeBtn.addActionListener(e -> dispose());

        btnBar.add(downloadInvoiceBtn);
        btnBar.add(closeBtn);

        bottomPanel.add(summaryCard, BorderLayout.NORTH);
        bottomPanel.add(btnBar,       BorderLayout.SOUTH);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
    }

    private void loadTrips() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // STRICT ISOLATION: company_id = companyAdmin.getCompanyId() and driver_id = this.driverId
        trips = tripDAO.getTripsByCompany(companyAdmin.getCompanyId(), driverId, -1, -1, -1, null);

        double totalAmount = 0.0;
        for (Trip t : trips) {
            totalAmount += t.getAmount();
            tableModel.addRow(new Object[]{
                t.getTripCode(),
                t.getVehicleNumber(),
                sdf.format(t.getTripDate()),
                t.getSourceLocation(),
                t.getDestination(),
                t.getGoodsDescription() != null ? t.getGoodsDescription() : "General Cargo",
                String.format("₹ %,.2f", t.getAmount()),
                t.getTripStatus()
            });
        }

        totalTripsLabel.setText("Total Trips Completed: " + trips.size());
        totalAmountLabel.setText("Total Payable to " + driverName + ": ₹ " + String.format("%,.2f", totalAmount));
    }

    private void downloadDriverInvoicePDF() {
        if (trips == null || trips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips recorded for this driver to generate an invoice.", "No Records", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double totalAmount = 0.0;
        for (Trip t : trips) totalAmount += t.getAmount();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-DRV-" + companyAdmin.getCompanyId() + "-" + driverId);
        invoice.setInvoiceDate(new Date());
        invoice.setCompanyId(companyAdmin.getCompanyId());
        invoice.setCompanyName(companyAdmin.getCompanyName());
        invoice.setDriverId(driverId);
        invoice.setDriverName(driverName);
        invoice.setVehicleNumber(vehicleNumbers);
        invoice.setBillingMonth(11);
        invoice.setBillingYear(2026);
        invoice.setTotalTrips(trips.size());
        invoice.setTotalAmount(totalAmount);
        invoice.setInvoiceType("Driver");
        invoice.setStatus("Generated");

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Invoice_" + companyAdmin.getCompanyName().replaceAll("\\s+", "_") + "_" + driverName + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (InvoiceGenerator.generatePDF(invoice, trips, path)) {
                JOptionPane.showMessageDialog(this,
                        "Driver invoice generated successfully!\nFile: " + path,
                        "Invoice Downloaded", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create PDF invoice file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
