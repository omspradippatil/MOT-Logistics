package com.transport.ui;

import com.transport.dao.CompanyDAO;
import com.transport.dao.DriverDAO;
import com.transport.dao.TripDAO;
import com.transport.model.Company;
import com.transport.model.Driver;
import com.transport.model.Trip;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * TripHistoryPanel.java - Trip Audit Ledger & Filter Engine
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
    private ModernButton deleteBtn;
    private JLabel       totalSummaryLabel;

    private TripDAO    tripDAO    = new TripDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private DriverDAO  driverDAO  = new DriverDAO();

    private int selectedTripId = -1;

    private static final String[] MONTHS = {"All Months", "January","February","March","April","May","June","July","August","September","October","November","December"};

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
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
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

        // Bottom Summary Bar
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));

        totalSummaryLabel = new JLabel("Total Trips: 0 | Total Amount: ₹ 0.00");
        totalSummaryLabel.setFont(UITheme.FONT_HEADER);
        totalSummaryLabel.setForeground(UITheme.BRAND_BLUE);
        bottomPanel.add(totalSummaryLabel, BorderLayout.WEST);

        if (isAdmin) {
            deleteBtn = new ModernButton("🗑 Delete Selected Trip", UITheme.DANGER_RED);
            deleteBtn.addActionListener(e -> deleteTrip());
            bottomPanel.add(deleteBtn, BorderLayout.EAST);
        }

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

        List<Trip> trips = tripDAO.getTripsByFilter(filterDriver, filterCompany, -1, filterMonth, filterYear);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        double total = 0;

        for (Trip t : trips) {
            total += t.getAmount();
            tableModel.addRow(new Object[]{
                t.getId(), t.getTripCode(),
                sdf.format(t.getTripDate()),
                t.getCompanyName(), t.getDriverName(), t.getVehicleNumber(),
                t.getSourceLocation(), t.getDestination(),
                String.format("%.2f", t.getAmount())
            });
        }

        totalSummaryLabel.setText(String.format("Showing %d Trips  |  Total Revenue: ₹ %,.2f", trips.size(), total));
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
