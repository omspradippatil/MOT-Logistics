package com.transport.ui;

import com.transport.dao.DriverDAO;
import com.transport.dao.TripDAO;
import com.transport.dao.VehicleDAO;
import com.transport.model.Admin;
import com.transport.model.Driver;
import com.transport.model.DriverCompanySummary;
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
 * CompanyTripManagementPanel.java - Trip Management for Company Admin
 *
 * Enforces strict multi-tenant isolation:
 * Only trips with company_id = companyAdmin.getCompanyId() are ever displayed or filtered.
 */
public class CompanyTripManagementPanel extends JPanel implements Refreshable {

    private Admin companyAdmin;

    private JTable            table;
    private DefaultTableModel tableModel;

    private JComboBox<Driver>  driverCombo;
    private JComboBox<Vehicle> vehicleCombo;
    private JComboBox<String>  monthCombo;
    private JComboBox<String>  yearCombo;
    private JTextField         searchField;

    private boolean isUpdatingDropdowns = false;

    private JLabel summaryLabel;

    private TripDAO    tripDAO    = new TripDAO();
    private DriverDAO  driverDAO  = new DriverDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();

    private List<Trip> currentTrips = new ArrayList<>();

    private static final String[] MONTHS = {
        "All Months", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public CompanyTripManagementPanel(Admin companyAdmin) {
        this.companyAdmin = companyAdmin;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadFilterDropdowns();
        filterTrips();
    }

    private void initUI() {
        // Top Filter Panel
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(UITheme.CARD_BG);
        filterPanel.setBorder(UITheme.createCardBorder("Filter Company Trips (" + companyAdmin.getCompanyName() + ")"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Row 1: Filters
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Driver:"), gbc);
        gbc.gridx = 1;
        driverCombo = new JComboBox<>();
        driverCombo.setFont(UITheme.FONT_BODY);
        driverCombo.setPreferredSize(new Dimension(140, 32));
        filterPanel.add(driverCombo, gbc);

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
        yearCombo.setSelectedItem("All Years");
        yearCombo.setFont(UITheme.FONT_BODY);
        yearCombo.setPreferredSize(new Dimension(100, 32));
        filterPanel.add(yearCombo, gbc);

        // Row 2: Search & Buttons
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("Search Location:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        searchField = new JTextField();
        searchField.setFont(UITheme.FONT_BODY);
        searchField.setPreferredSize(new Dimension(280, 32));
        searchField.setToolTipText("Search by source, destination or trip code...");
        filterPanel.add(searchField, gbc);

        // Dynamic instant filtering listeners
        driverCombo.addActionListener(e -> { if (!isUpdatingDropdowns) filterTrips(); });
        vehicleCombo.addActionListener(e -> { if (!isUpdatingDropdowns) filterTrips(); });
        monthCombo.addActionListener(e -> { if (!isUpdatingDropdowns) filterTrips(); });
        yearCombo.addActionListener(e -> { if (!isUpdatingDropdowns) filterTrips(); });
        searchField.addActionListener(e -> { if (!isUpdatingDropdowns) filterTrips(); });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingDropdowns) filterTrips(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingDropdowns) filterTrips(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingDropdowns) filterTrips(); }
        });

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
            "ID", "Trip Code", "Date", "Driver (Created By)", "Vehicle Reg.",
            "From (Source)", "To (Destination)", "Goods Description", "Amount (₹)", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Hide database ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(85);   // Trip Code
        table.getColumnModel().getColumn(2).setPreferredWidth(95);   // Date
        table.getColumnModel().getColumn(3).setPreferredWidth(140);  // Driver (Created By)
        table.getColumnModel().getColumn(4).setPreferredWidth(125);  // Vehicle Reg.
        table.getColumnModel().getColumn(5).setPreferredWidth(110);  // From
        table.getColumnModel().getColumn(6).setPreferredWidth(110);  // To
        table.getColumnModel().getColumn(7).setPreferredWidth(160);  // Goods
        table.getColumnModel().getColumn(8).setPreferredWidth(110);  // Amount
        table.getColumnModel().getColumn(9).setPreferredWidth(90);   // Status

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    showSelectedTripDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Trips Audit Ledger"));

        // Bottom Summary & Actions - 2-tier layout so summary and buttons NEVER overlap
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBackground(UITheme.CARD_BG);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
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

        summaryLabel = new JLabel("Total Trips: 0 | Total Amount: ₹ 0.00");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryLabel.setForeground(UITheme.BRAND_BLUE);
        summaryCard.add(summaryLabel, BorderLayout.CENTER);

        // Tier 2: Action Buttons
        JPanel actionBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionBtnBar.setOpaque(false);

        ModernButton viewDetailsBtn = new ModernButton("📋 View Trip Details", UITheme.UPDATE_BLUE);
        viewDetailsBtn.setPreferredSize(new Dimension(160, 36));
        viewDetailsBtn.addActionListener(e -> showSelectedTripDetails());

        ModernButton receiptBtn = new ModernButton("🧾 Download Receipt (PDF)", UITheme.AMBER_ORANGE);
        receiptBtn.setPreferredSize(new Dimension(190, 36));
        receiptBtn.addActionListener(e -> downloadSelectedTripReceipt());

        ModernButton exportPdfBtn = new ModernButton("📥 Export Trips PDF", UITheme.SUCCESS_GREEN);
        exportPdfBtn.setPreferredSize(new Dimension(160, 36));
        exportPdfBtn.addActionListener(e -> exportTripsPDF());

        actionBtnBar.add(viewDetailsBtn);
        actionBtnBar.add(receiptBtn);
        actionBtnBar.add(exportPdfBtn);

        bottomPanel.add(summaryCard, BorderLayout.NORTH);
        bottomPanel.add(actionBtnBar, BorderLayout.SOUTH);

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadFilterDropdowns() {
        int prevDriverId = (driverCombo != null && driverCombo.getSelectedItem() instanceof Driver)
                ? ((Driver) driverCombo.getSelectedItem()).getId() : -1;
        int prevVehicleId = (vehicleCombo != null && vehicleCombo.getSelectedItem() instanceof Vehicle)
                ? ((Vehicle) vehicleCombo.getSelectedItem()).getId() : -1;

        isUpdatingDropdowns = true;

        // Driver combo: only drivers who worked for this company
        driverCombo.removeAllItems();
        Driver allD = new Driver();
        allD.setId(-1);
        allD.setDriverName("-- All Drivers --");
        driverCombo.addItem(allD);

        List<DriverCompanySummary> summaries = driverDAO.getDriversForCompany(companyAdmin.getCompanyId());
        Driver toSelectDriver = allD;
        for (DriverCompanySummary s : summaries) {
            Driver d = new Driver();
            d.setId(s.getDriverId());
            d.setDriverName(s.getDriverName());
            driverCombo.addItem(d);
            if (prevDriverId > 0 && d.getId() == prevDriverId) {
                toSelectDriver = d;
            }
        }
        driverCombo.setSelectedItem(toSelectDriver);

        // Vehicle combo: only vehicles used for this company
        vehicleCombo.removeAllItems();
        Vehicle allV = new Vehicle();
        allV.setId(-1);
        allV.setRegistrationNumber("-- All Vehicles --");
        vehicleCombo.addItem(allV);

        List<Vehicle> compVehicles = vehicleDAO.getVehiclesByCompany(companyAdmin.getCompanyId());
        Vehicle toSelectVeh = allV;
        for (Vehicle v : compVehicles) {
            vehicleCombo.addItem(v);
            if (prevVehicleId > 0 && v.getId() == prevVehicleId) {
                toSelectVeh = v;
            }
        }
        vehicleCombo.setSelectedItem(toSelectVeh);

        isUpdatingDropdowns = false;
    }

    public void filterTrips() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        int driverId = -1;
        if (driverCombo.getSelectedItem() != null) {
            driverId = ((Driver) driverCombo.getSelectedItem()).getId();
        }

        int vehicleId = -1;
        if (vehicleCombo.getSelectedItem() != null) {
            vehicleId = ((Vehicle) vehicleCombo.getSelectedItem()).getId();
        }

        int month = monthCombo.getSelectedIndex(); // 0 is All Months

        int year = -1;
        String yearStr = (String) yearCombo.getSelectedItem();
        if (yearStr != null && !yearStr.equals("All Years")) {
            try { year = Integer.parseInt(yearStr); } catch (NumberFormatException ignored) {}
        }

        String search = searchField.getText().trim();

        // STRICT ISOLATION: company_id is locked to companyAdmin.getCompanyId()
        currentTrips = tripDAO.getTripsByCompany(companyAdmin.getCompanyId(), driverId, vehicleId, month, year, search);

        double totalAmount = 0.0;
        for (Trip t : currentTrips) {
            totalAmount += t.getAmount();
            tableModel.addRow(new Object[]{
                t.getId(),
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

        summaryLabel.setText("Total Completed Trips: " + currentTrips.size() + "  |  Total Billing: ₹ " + String.format("%,.2f", totalAmount));
    }

    private void resetFilters() {
        isUpdatingDropdowns = true;
        if (driverCombo.getItemCount() > 0) driverCombo.setSelectedIndex(0);
        if (vehicleCombo.getItemCount() > 0) vehicleCombo.setSelectedIndex(0);
        monthCombo.setSelectedIndex(0);
        yearCombo.setSelectedItem("All Years");
        searchField.setText("");
        isUpdatingDropdowns = false;
        filterTrips();
    }

    private void showSelectedTripDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a trip row to view its details.", "Select Trip", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int tripId = (int) tableModel.getValueAt(row, 0);
        Trip trip = tripDAO.getTripById(tripId);
        if (trip == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String message =
            "====================================================\n" +
            "       OPERATIONAL TRIP AUDIT & DRIVER CONNECTION   \n" +
            "====================================================\n\n" +
            "Trip Reference ID    : " + trip.getTripCode() + "\n" +
            "Trip Date            : " + sdf.format(trip.getTripDate()) + "\n" +
            "Client Company       : " + trip.getCompanyName() + "\n\n" +
            "DRIVER ATTRIBUTION & CONNECTION:\n" +
            "Created By Driver    : " + trip.getDriverName() + "\n" +
            "Assigned Vehicle Reg.: " + trip.getVehicleNumber() + "\n\n" +
            "LOGISTICS & FINANCIAL ROUTE:\n" +
            "Source (Pickup)      : " + trip.getSourceLocation() + "\n" +
            "Destination (Drop)   : " + trip.getDestination() + "\n" +
            "Cargo / Goods        : " + (trip.getGoodsDescription() != null ? trip.getGoodsDescription() : "General Cargo") + "\n" +
            "Trip Fare Amount     : ₹ " + String.format("%,.2f", trip.getAmount()) + "\n" +
            "Trip Status          : " + trip.getTripStatus() + "\n" +
            "Remarks              : " + (trip.getRemarks() != null && !trip.getRemarks().trim().isEmpty() ? trip.getRemarks() : "None") + "\n\n" +
            "====================================================";

        JOptionPane.showMessageDialog(this, message, "Trip Details - " + trip.getTripCode(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void downloadSelectedTripReceipt() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a trip row to download its official receipt.", "Select Trip", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int tripId = (int) tableModel.getValueAt(row, 0);
        Trip trip = tripDAO.getTripById(tripId);
        if (trip == null) {
            JOptionPane.showMessageDialog(this, "Failed to load trip details from database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String safeDriver = (trip.getDriverName() != null ? trip.getDriverName() : "Driver").replaceAll("\\s+", "_");
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Receipt_" + trip.getTripCode() + "_" + safeDriver + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetFile = chooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".pdf")) {
                targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".pdf");
            }
            if (targetFile.exists()) {
                int res = JOptionPane.showConfirmDialog(this,
                    "The file \"" + targetFile.getName() + "\" already exists.\nDo you want to overwrite it?",
                    "Confirm File Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (res != JOptionPane.YES_OPTION) return;
            }

            String path = targetFile.getAbsolutePath();
            if (InvoiceGenerator.generateTripReceiptPDF(trip, path)) {
                JOptionPane.showMessageDialog(this, "Official Trip Receipt PDF generated successfully!\n" + path, "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create PDF receipt.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportTripsPDF() {
        if (currentTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips to export.", "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop",
                "Trips_Report_" + companyAdmin.getCompanyName().replaceAll("\\s+", "_") + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetFile = chooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".pdf")) {
                targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".pdf");
            }
            if (targetFile.exists()) {
                int res = JOptionPane.showConfirmDialog(this,
                    "The file \"" + targetFile.getName() + "\" already exists.\nDo you want to overwrite it?",
                    "Confirm File Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (res != JOptionPane.YES_OPTION) return;
            }

            String path = targetFile.getAbsolutePath();
            String title = "TRIP AUDIT REPORT - " + companyAdmin.getCompanyName().toUpperCase();
            String subtitle = "Filtered Operational Logistics Records";

            if (InvoiceGenerator.generateTripsSummaryPDF(currentTrips, title, subtitle, path)) {
                JOptionPane.showMessageDialog(this, "Trips Report PDF saved successfully!\n" + path, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create PDF file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refreshData() {
        loadFilterDropdowns();
        filterTrips();
    }
}
