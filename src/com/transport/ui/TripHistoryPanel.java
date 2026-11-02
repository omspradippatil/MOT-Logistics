package com.transport.ui;

import com.transport.dao.CompanyDAO;
import com.transport.dao.DriverDAO;
import com.transport.dao.TripDAO;
import com.transport.model.Company;
import com.transport.model.Driver;
import com.transport.model.Trip;
import com.transport.util.InvoiceGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * TripHistoryPanel.java - Trip Audit Ledger, Filter & Receipt Download Engine
 */
public class TripHistoryPanel extends JPanel implements Refreshable {

    private boolean isAdmin;
    private int     driverId;

    private JTable            table;
    private DefaultTableModel tableModel;

    private JComboBox<Driver>  driverFilterCombo;
    private JComboBox<Company> companyFilterCombo;
    private JComboBox<String>  monthFilterCombo;
    private JComboBox<String>  yearFilterCombo;

    private ModernButton searchBtn;
    private ModernButton clearFilterBtn;
    private ModernButton receiptBtn;
    private ModernButton reportPdfBtn;
    private ModernButton exportCsvBtn;
    private ModernButton deleteBtn;
    private JLabel       totalSummaryLabel;

    private TripDAO    tripDAO    = new TripDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private DriverDAO  driverDAO  = new DriverDAO();

    private int selectedTripId = -1;
    private List<Trip> currentLoadedTrips = new ArrayList<>();

    private static final String[] MONTHS = {
        "All Months", "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public TripHistoryPanel(boolean isAdmin, int driverId) {
        this.isAdmin  = isAdmin;
        this.driverId = driverId;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadTrips();
    }

    private void initUI() {
        // Filter Toolbar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBorder(UITheme.createCardBorder("Filter Trip Records"));
        filterPanel.setBackground(UITheme.CARD_BG);

        filterPanel.add(new JLabel("Month:"));
        monthFilterCombo = new JComboBox<>(MONTHS);
        monthFilterCombo.setFont(UITheme.FONT_BODY);
        monthFilterCombo.setPreferredSize(new Dimension(130, 32));
        filterPanel.add(monthFilterCombo);

        filterPanel.add(new JLabel("Year:"));
        String[] years = {"All Years", "2024", "2025", "2026", "2027"};
        yearFilterCombo = new JComboBox<>(years);
        yearFilterCombo.setSelectedItem("2026");
        yearFilterCombo.setFont(UITheme.FONT_BODY);
        yearFilterCombo.setPreferredSize(new Dimension(100, 32));
        filterPanel.add(yearFilterCombo);

        if (isAdmin) {
            filterPanel.add(new JLabel("Driver:"));
            driverFilterCombo = new JComboBox<>();
            driverFilterCombo.setFont(UITheme.FONT_BODY);
            driverFilterCombo.setPreferredSize(new Dimension(140, 32));
            Driver allDrivers = new Driver();
            allDrivers.setId(-1);
            allDrivers.setDriverName("-- All Drivers --");
            driverFilterCombo.addItem(allDrivers);
            for (Driver d : driverDAO.getAllDrivers()) driverFilterCombo.addItem(d);
            filterPanel.add(driverFilterCombo);

            filterPanel.add(new JLabel("Company:"));
            companyFilterCombo = new JComboBox<>();
            companyFilterCombo.setFont(UITheme.FONT_BODY);
            companyFilterCombo.setPreferredSize(new Dimension(140, 32));
            Company allComps = new Company();
            allComps.setId(-1);
            allComps.setCompanyName("-- All Companies --");
            companyFilterCombo.addItem(allComps);
            for (Company c : companyDAO.getAllCompanies()) companyFilterCombo.addItem(c);
            filterPanel.add(companyFilterCombo);
        }

        searchBtn      = new ModernButton("🔍 Search", UITheme.BRAND_BLUE);
        clearFilterBtn = new ModernButton("↺ Reset", UITheme.SECONDARY_GRAY);

        filterPanel.add(searchBtn);
        filterPanel.add(clearFilterBtn);

        // Table
        String[] cols = {"ID", "Trip Code", "Date", "Company", "Driver", "Vehicle No.", "From", "To", "Amount (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selectedTripId = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Completed Trip Records"));

        // Bottom Toolbar Bar
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));

        totalSummaryLabel = new JLabel("Total Trips: 0 | Total Amount: ₹ 0.00");
        totalSummaryLabel.setFont(UITheme.FONT_HEADER);
        totalSummaryLabel.setForeground(UITheme.BRAND_BLUE);
        bottomPanel.add(totalSummaryLabel, BorderLayout.WEST);

        // Action Download & Delete Buttons
        JPanel actionBtnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnGroup.setOpaque(false);

        receiptBtn   = new ModernButton("🧾 Download Trip Receipt", UITheme.AMBER_ORANGE);
        reportPdfBtn = new ModernButton("📥 Download Report (PDF)", UITheme.BRAND_BLUE);
        exportCsvBtn = new ModernButton("📊 Export CSV", UITheme.SUCCESS_GREEN);

        receiptBtn.addActionListener(e   -> downloadSelectedTripReceipt());
        reportPdfBtn.addActionListener(e -> downloadTripsReportPDF());
        exportCsvBtn.addActionListener(e -> downloadTripsReportCSV());

        actionBtnGroup.add(receiptBtn);
        actionBtnGroup.add(reportPdfBtn);
        actionBtnGroup.add(exportCsvBtn);

        if (isAdmin) {
            deleteBtn = new ModernButton("🗑 Delete Trip", UITheme.DANGER_RED);
            deleteBtn.addActionListener(e -> deleteTrip());
            actionBtnGroup.add(deleteBtn);
        }

        bottomPanel.add(actionBtnGroup, BorderLayout.EAST);

        searchBtn.addActionListener(e      -> loadTrips());
        clearFilterBtn.addActionListener(e -> { resetFilters(); loadTrips(); });

        add(filterPanel,  BorderLayout.NORTH);
        add(scrollPane,   BorderLayout.CENTER);
        add(bottomPanel,  BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        if (isAdmin && driverFilterCombo != null) {
            Driver selectedDriver = (Driver) driverFilterCombo.getSelectedItem();
            driverFilterCombo.removeAllItems();
            Driver allDrivers = new Driver();
            allDrivers.setId(-1);
            allDrivers.setDriverName("-- All Drivers --");
            driverFilterCombo.addItem(allDrivers);
            for (Driver d : driverDAO.getAllDrivers()) {
                driverFilterCombo.addItem(d);
                if (selectedDriver != null && d.getId() == selectedDriver.getId()) {
                    driverFilterCombo.setSelectedItem(d);
                }
            }
        }

        if (isAdmin && companyFilterCombo != null) {
            Company selectedComp = (Company) companyFilterCombo.getSelectedItem();
            companyFilterCombo.removeAllItems();
            Company allComps = new Company();
            allComps.setId(-1);
            allComps.setCompanyName("-- All Companies --");
            companyFilterCombo.addItem(allComps);
            for (Company c : companyDAO.getAllCompanies()) {
                companyFilterCombo.addItem(c);
                if (selectedComp != null && c.getId() == selectedComp.getId()) {
                    companyFilterCombo.setSelectedItem(c);
                }
            }
        }

        loadTrips();
    }

    private void loadTrips() {
        tableModel.setRowCount(0);
        selectedTripId = -1;

        int filterDriver  = driverId > 0 ? driverId : -1;
        int filterCompany = -1;
        int filterMonth   = -1;
        int filterYear    = -1;

        if (isAdmin && driverFilterCombo != null) {
            Driver selectedDriver = (Driver) driverFilterCombo.getSelectedItem();
            if (selectedDriver != null && selectedDriver.getId() > 0) filterDriver = selectedDriver.getId();
        }

        if (isAdmin && companyFilterCombo != null) {
            Company selectedCompany = (Company) companyFilterCombo.getSelectedItem();
            if (selectedCompany != null && selectedCompany.getId() > 0) filterCompany = selectedCompany.getId();
        }

        int monthIndex = monthFilterCombo.getSelectedIndex();
        if (monthIndex > 0) filterMonth = monthIndex;

        String yearStr = (String) yearFilterCombo.getSelectedItem();
        if (!"All Years".equals(yearStr)) {
            try { filterYear = Integer.parseInt(yearStr); } catch (Exception ignored) {}
        }

        currentLoadedTrips = tripDAO.getTripsByFilter(filterDriver, filterCompany, -1, filterMonth, filterYear);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        double total = 0;

        for (Trip t : currentLoadedTrips) {
            total += t.getAmount();
            tableModel.addRow(new Object[]{
                t.getId(), t.getTripCode(),
                sdf.format(t.getTripDate()),
                t.getCompanyName(), t.getDriverName(), t.getVehicleNumber(),
                t.getSourceLocation(), t.getDestination(),
                String.format("%.2f", t.getAmount())
            });
        }

        totalSummaryLabel.setText(String.format("Showing %d Trips  |  Total Revenue: ₹ %,.2f", currentLoadedTrips.size(), total));
    }

    /** Downloads single trip official receipt voucher */
    private void downloadSelectedTripReceipt() {
        if (selectedTripId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trip row from the table first!", "Select Trip", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Trip trip = tripDAO.getTripById(selectedTripId);
        if (trip == null) {
            JOptionPane.showMessageDialog(this, "Could not load selected trip details!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File defaultDir = new File("invoices");
        if (!defaultDir.exists()) defaultDir.mkdirs();

        String pdfName = "Receipt_" + trip.getTripCode() + ".pdf";
        String txtName = "Receipt_" + trip.getTripCode() + ".txt";

        File pdfFile = new File(defaultDir, pdfName);
        File txtFile = new File(defaultDir, txtName);

        boolean pdfOk = InvoiceGenerator.generateTripReceiptPDF(trip, pdfFile.getAbsolutePath());
        boolean txtOk = InvoiceGenerator.generateTripReceiptText(trip, txtFile.getAbsolutePath());

        if (pdfOk && txtOk) {
            int openChoice = JOptionPane.showConfirmDialog(this,
                "Official Trip Receipt Generated Successfully!\n\n" +
                "Trip Code : " + trip.getTripCode() + "\n" +
                "Client    : " + trip.getCompanyName() + "\n" +
                "Amount    : ₹ " + String.format("%,.2f", trip.getAmount()) + "\n\n" +
                "Saved Locations:\n" +
                "✓ " + pdfFile.getAbsolutePath() + "\n" +
                "✓ " + txtFile.getAbsolutePath() + "\n\n" +
                "Would you like to open the PDF receipt now?",
                "Receipt Downloaded", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (openChoice == JOptionPane.YES_OPTION) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(pdfFile);
                    }
                } catch (Exception ex) {
                    System.out.println("Could not launch PDF viewer: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to generate trip receipt!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Downloads full trips report PDF */
    private void downloadTripsReportPDF() {
        if (currentLoadedTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips to export!", "Empty List", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File defaultDir = new File("invoices");
        if (!defaultDir.exists()) defaultDir.mkdirs();

        String monthStr = (String) monthFilterCombo.getSelectedItem();
        String yearStr  = (String) yearFilterCombo.getSelectedItem();
        String fileName = "Trips_Report_" + monthStr.replaceAll("[^a-zA-Z0-9]", "") + "_" + yearStr + ".pdf";

        File targetFile = new File(defaultDir, fileName);

        String title = "MOT LOGISTICS - COMPREHENSIVE TRIP AUDIT REPORT";
        String subtitle = "Period: " + monthStr + " " + yearStr + " | Total Records: " + currentLoadedTrips.size();

        if (InvoiceGenerator.generateTripsSummaryPDF(currentLoadedTrips, title, subtitle, targetFile.getAbsolutePath())) {
            int openChoice = JOptionPane.showConfirmDialog(this,
                "Trips Audit Report PDF Generated Successfully!\n\n" +
                "Period        : " + monthStr + " " + yearStr + "\n" +
                "Trips Count   : " + currentLoadedTrips.size() + "\n" +
                "File Path     : " + targetFile.getAbsolutePath() + "\n\n" +
                "Would you like to open the PDF report now?",
                "Report Downloaded", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (openChoice == JOptionPane.YES_OPTION) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(targetFile);
                    }
                } catch (Exception ex) {
                    System.out.println("Could not launch viewer: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to generate trips report PDF!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Exports full trips report to CSV */
    private void downloadTripsReportCSV() {
        if (currentLoadedTrips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips to export!", "Empty List", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File defaultDir = new File("invoices");
        if (!defaultDir.exists()) defaultDir.mkdirs();

        String monthStr = (String) monthFilterCombo.getSelectedItem();
        String yearStr  = (String) yearFilterCombo.getSelectedItem();
        String fileName = "Trips_Export_" + monthStr.replaceAll("[^a-zA-Z0-9]", "") + "_" + yearStr + ".csv";

        File targetFile = new File(defaultDir, fileName);

        if (InvoiceGenerator.exportTripsToCSV(currentLoadedTrips, targetFile.getAbsolutePath())) {
            JOptionPane.showMessageDialog(this,
                "Trips CSV exported successfully!\n\n" +
                "Total Rows: " + currentLoadedTrips.size() + "\n" +
                "Saved Location: " + targetFile.getAbsolutePath() + "\n\n" +
                "You can open this file in Microsoft Excel or Google Sheets.",
                "CSV Export Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to export CSV file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTrip() {
        if (selectedTripId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trip row to delete!", "Select Trip", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete this trip entry?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (tripDAO.deleteTrip(selectedTripId)) {
                JOptionPane.showMessageDialog(this, "Trip record deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTrips();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete trip record!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetFilters() {
        monthFilterCombo.setSelectedIndex(0);
        yearFilterCombo.setSelectedItem("2026");
        if (driverFilterCombo != null)  driverFilterCombo.setSelectedIndex(0);
        if (companyFilterCombo != null) companyFilterCombo.setSelectedIndex(0);
    }
}
