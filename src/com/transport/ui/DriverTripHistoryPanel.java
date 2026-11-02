package com.transport.ui;

import com.transport.dao.CompanyDAO;
import com.transport.dao.TripDAO;
import com.transport.dao.VehicleDAO;
import com.transport.model.Company;
import com.transport.model.Driver;
import com.transport.model.Trip;
import com.transport.model.Vehicle;
import com.transport.util.InvoiceGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DriverTripHistoryPanel.java - Comprehensive Trip History for Drivers
 *
 * Implements Module 3 for Drivers:
 * - View all trips logged by this driver across all client companies
 * - Filters by Company, Vehicle, Month, Year, and Location keyword
 * - Individual trip receipt PDF generation
 * - Complete statement PDF export
 */
public class DriverTripHistoryPanel extends JPanel implements Refreshable {

    private Driver driver;

    private JComboBox<Company> companyCombo;
    private JComboBox<Vehicle> vehicleCombo;
    private JComboBox<String>  monthCombo;
    private JComboBox<String>  yearCombo;
    private JTextField         searchField;

    private JTable            table;
    private DefaultTableModel tableModel;
    private JLabel            summaryLabel;

    private TripDAO    tripDAO    = new TripDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();

    private List<Trip> currentTrips = new ArrayList<>();

    private static final String[] MONTHS = {
        "All Months", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public DriverTripHistoryPanel(Driver driver) {
        this.driver = driver;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadDropdowns();
        filterTrips();
    }

    private void initUI() {
        // Filter Toolbar
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(UITheme.CARD_BG);
        filterPanel.setBorder(UITheme.createCardBorder("Filter My Trips History"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Company:"), gbc);
        gbc.gridx = 1;
        companyCombo = new JComboBox<>();
        companyCombo.setFont(UITheme.FONT_BODY);
        companyCombo.setPreferredSize(new Dimension(150, 32));
        filterPanel.add(companyCombo, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Vehicle:"), gbc);
        gbc.gridx = 3;
        vehicleCombo = new JComboBox<>();
        vehicleCombo.setFont(UITheme.FONT_BODY);
        vehicleCombo.setPreferredSize(new Dimension(140, 32));
        filterPanel.add(vehicleCombo, gbc);

        gbc.gridx = 4;
        filterPanel.add(new JLabel("Month:"), gbc);
        gbc.gridx = 5;
        monthCombo = new JComboBox<>(MONTHS);
        monthCombo.setFont(UITheme.FONT_BODY);
        monthCombo.setPreferredSize(new Dimension(120, 32));
        filterPanel.add(monthCombo, gbc);

        gbc.gridx = 6;
        filterPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 7;
        String[] years = {"All Years", "2024", "2025", "2026", "2027"};
        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem("2026");
        yearCombo.setFont(UITheme.FONT_BODY);
        yearCombo.setPreferredSize(new Dimension(90, 32));
        filterPanel.add(yearCombo, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("Search Location:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        searchField = new JTextField();
        searchField.setFont(UITheme.FONT_BODY);
        searchField.setPreferredSize(new Dimension(280, 32));
        searchField.setToolTipText("Search by source, destination or trip code...");
        filterPanel.add(searchField, gbc);

        gbc.gridx = 4; gbc.gridwidth = 4;
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnGroup.setOpaque(false);

        ModernButton filterBtn = new ModernButton("🔍 Apply Filter", UITheme.BRAND_BLUE);
        filterBtn.setPreferredSize(new Dimension(130, 32));
        filterBtn.addActionListener(e -> filterTrips());

        ModernButton resetBtn = new ModernButton("↺ Reset", UITheme.SECONDARY_GRAY);
        resetBtn.setPreferredSize(new Dimension(90, 32));
        resetBtn.addActionListener(e -> resetFilters());

        btnGroup.add(filterBtn);
        btnGroup.add(resetBtn);
        filterPanel.add(btnGroup, gbc);

        // Table
        String[] cols = {
            "ID", "Trip Code", "Date", "Client Company", "Vehicle No",
            "From (Source)", "To (Destination)", "Cargo / Goods", "Amount (₹)", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(85);   // Trip Code
        table.getColumnModel().getColumn(2).setPreferredWidth(95);   // Date
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Client Company
        table.getColumnModel().getColumn(4).setPreferredWidth(125);  // Vehicle No
        table.getColumnModel().getColumn(5).setPreferredWidth(110);  // From
        table.getColumnModel().getColumn(6).setPreferredWidth(110);  // To
        table.getColumnModel().getColumn(7).setPreferredWidth(160);  // Cargo
        table.getColumnModel().getColumn(8).setPreferredWidth(110);  // Amount
        table.getColumnModel().getColumn(9).setPreferredWidth(90);   // Status

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("My Completed Trips"));

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

        summaryLabel = new JLabel("Total Trips: 0  |  Total Earnings: ₹ 0.00");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryLabel.setForeground(UITheme.BRAND_BLUE);
        summaryCard.add(summaryLabel, BorderLayout.CENTER);

        // Tier 2: Action Buttons
        JPanel actionBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionBtnBar.setOpaque(false);

        ModernButton receiptBtn = new ModernButton("🧾 Download Receipt (PDF)", UITheme.UPDATE_BLUE);
        receiptBtn.setPreferredSize(new Dimension(200, 36));
        receiptBtn.addActionListener(e -> downloadTripReceiptPDF());

        ModernButton exportPdfBtn = new ModernButton("📥 Export Trips Statement (PDF)", UITheme.SUCCESS_GREEN);
        exportPdfBtn.setPreferredSize(new Dimension(220, 36));
        exportPdfBtn.addActionListener(e -> exportTripsStatementPDF());

        actionBtnBar.add(receiptBtn);
        actionBtnBar.add(exportPdfBtn);

        bottomBar.add(summaryCard, BorderLayout.NORTH);
        bottomBar.add(actionBtnBar, BorderLayout.SOUTH);

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(bottomBar,   BorderLayout.SOUTH);
    }

    private void loadDropdowns() {
        companyCombo.removeAllItems();
        Company allC = new Company();
        allC.setId(-1);
        allC.setCompanyName("-- All Companies --");
        companyCombo.addItem(allC);
        for (Company c : companyDAO.getAllCompanies()) companyCombo.addItem(c);

        vehicleCombo.removeAllItems();
        Vehicle allV = new Vehicle();
        allV.setId(-1);
        allV.setRegistrationNumber("-- All Vehicles --");
        vehicleCombo.addItem(allV);
        for (Vehicle v : vehicleDAO.getVehiclesByDriver(driver.getId())) vehicleCombo.addItem(v);
    }

    public void filterTrips() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        int companyId = -1;
        if (companyCombo.getSelectedItem() != null) {
            companyId = ((Company) companyCombo.getSelectedItem()).getId();
        }

        int vehicleId = -1;
        if (vehicleCombo.getSelectedItem() != null) {
            vehicleId = ((Vehicle) vehicleCombo.getSelectedItem()).getId();
        }

        int month = monthCombo.getSelectedIndex();
        int year = -1;
        String yearStr = (String) yearCombo.getSelectedItem();
        if (yearStr != null && !yearStr.equals("All Years")) {
            try { year = Integer.parseInt(yearStr); } catch (NumberFormatException ignored) {}
        }

        String search = searchField.getText().trim();

        currentTrips = tripDAO.getTripsByDriver(driver.getId(), companyId, vehicleId, month, year, search);

        double totalAmount = 0.0;
        for (Trip t : currentTrips) {
            totalAmount += t.getAmount();
            tableModel.addRow(new Object[]{
                t.getId(),
                t.getTripCode(),
                sdf.format(t.getTripDate()),
                t.getCompanyName(),
                t.getVehicleNumber(),
                t.getSourceLocation(),
                t.getDestination(),
                t.getGoodsDescription() != null ? t.getGoodsDescription() : "-",
                String.format("₹ %,.2f", t.getAmount()),
                t.getTripStatus()
            });
        }

        summaryLabel.setText("Total Completed Trips: " + currentTrips.size() + "  |  Total Earnings: ₹ " + String.format("%,.2f", totalAmount));
    }

    private void resetFilters() {
        companyCombo.setSelectedIndex(0);
        vehicleCombo.setSelectedIndex(0);
        monthCombo.setSelectedIndex(0);
        yearCombo.setSelectedItem("2026");
        searchField.setText("");
        filterTrips();
    }

    private void downloadTripReceiptPDF() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a trip from the table to download its receipt.", "Select Trip", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int tripId = (int) tableModel.getValueAt(row, 0);
        Trip trip = tripDAO.getTripById(tripId);
        if (trip == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Receipt_" + trip.getTripCode() + "_" + driver.getDriverName() + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (InvoiceGenerator.generateTripReceiptPDF(trip, path)) {
                JOptionPane.showMessageDialog(this, "Official Trip Receipt PDF generated successfully!\n" + path, "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create PDF receipt.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportTripsStatementPDF() {
        if (currentTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips to export.", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Trips_Statement_" + driver.getDriverName() + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            String title = "DRIVER TRIPS STATEMENT - " + driver.getDriverName().toUpperCase();
            String subtitle = "Consolidated Commercial Haulage Audit Ledger";

            if (InvoiceGenerator.generateTripsSummaryPDF(currentTrips, title, subtitle, path)) {
                JOptionPane.showMessageDialog(this, "Trips Statement PDF saved successfully!\n" + path, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create PDF statement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refreshData() {
        loadDropdowns();
        filterTrips();
    }
}
