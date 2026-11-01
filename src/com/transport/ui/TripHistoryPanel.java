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
 * TripHistoryPanel.java - View Trip Records
 *
 * Admin sees ALL trips.
 * Driver sees ONLY their own trips.
 *
 * Has filter options to search by:
 * - Driver
 * - Company
 * - Month / Year
 *
 * KEY CONCEPT: The same panel works for BOTH admin and driver.
 * The boolean 'isAdmin' controls what data is shown.
 * This is an example of REUSABILITY.
 */
public class TripHistoryPanel extends JPanel {

    private boolean isAdmin;
    private int     driverId;   // -1 means show all (admin)

    private JTable            table;
    private DefaultTableModel tableModel;

    // Filter fields
    private JComboBox<Driver>  driverFilterCombo;
    private JComboBox<Company> companyFilterCombo;
    private JComboBox<String>  monthFilterCombo;
    private JComboBox<String>  yearFilterCombo;

    private JButton searchBtn;
    private JButton clearFilterBtn;
    private JButton deleteBtn;   // Admin only

    private TripDAO    tripDAO    = new TripDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private DriverDAO  driverDAO  = new DriverDAO();

    private int selectedTripId = -1;

    private static final String[] MONTHS = {"All", "January","February","March","April","May","June","July","August","September","October","November","December"};

    /**
     * Constructor
     * @param isAdmin - true if admin, false if driver
     * @param driverId - the logged-in driver's ID (-1 for admin)
     */
    public TripHistoryPanel(boolean isAdmin, int driverId) {
        this.isAdmin  = isAdmin;
        this.driverId = driverId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        initUI();
        loadTrips();
    }

    private void initUI() {
        // Filter bar at top
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search / Filter"));
        filterPanel.setBackground(Color.WHITE);

        // Month filter
        filterPanel.add(new JLabel("Month:"));
        monthFilterCombo = new JComboBox<>(MONTHS);
        monthFilterCombo.setPreferredSize(new Dimension(100, 25));
        filterPanel.add(monthFilterCombo);

        // Year filter
        filterPanel.add(new JLabel("Year:"));
        String[] years = {"All", "2024", "2025", "2026", "2027"};
        yearFilterCombo = new JComboBox<>(years);
        yearFilterCombo.setSelectedItem("2026");
        yearFilterCombo.setPreferredSize(new Dimension(70, 25));
        filterPanel.add(yearFilterCombo);

        // Admin-only filters: driver and company
        if (isAdmin) {
            filterPanel.add(new JLabel("Driver:"));
            driverFilterCombo = new JComboBox<>();
            driverFilterCombo.addItem(new Driver()); // "All" placeholder
            driverFilterCombo.getModel().getElementAt(0).setDriverName("All Drivers");
            for (Driver d : driverDAO.getAllDrivers()) driverFilterCombo.addItem(d);
            driverFilterCombo.setPreferredSize(new Dimension(130, 25));
            filterPanel.add(driverFilterCombo);

            filterPanel.add(new JLabel("Company:"));
            companyFilterCombo = new JComboBox<>();
            companyFilterCombo.addItem(new Company()); // "All" placeholder
            companyFilterCombo.getModel().getElementAt(0).setCompanyName("All Companies");
            for (Company c : companyDAO.getAllCompanies()) companyFilterCombo.addItem(c);
            companyFilterCombo.setPreferredSize(new Dimension(130, 25));
            filterPanel.add(companyFilterCombo);
        }

        searchBtn      = createButton("Search",       new Color(0, 100, 180));
        clearFilterBtn = createButton("Show All",     Color.DARK_GRAY);
        filterPanel.add(searchBtn);
        filterPanel.add(clearFilterBtn);

        // Table
        String[] cols = {"ID", "Trip Code", "Date", "Company", "Driver", "Vehicle", "From", "To", "Amount (Rs.)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(0, 70, 130));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selectedTripId = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // Bottom: total amount label + delete button (admin only)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);

        JLabel totalLabel = new JLabel("  Select filters and click Search.");
        totalLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        if (isAdmin) {
            deleteBtn = createButton("Delete Selected Trip", new Color(200, 50, 50));
            deleteBtn.addActionListener(e -> deleteTrip());
            bottomPanel.add(deleteBtn, BorderLayout.EAST);
        }

        searchBtn.addActionListener(e      -> loadTrips());
        clearFilterBtn.addActionListener(e -> { resetFilters(); loadTrips(); });

        add(filterPanel,  BorderLayout.NORTH);
        add(scrollPane,   BorderLayout.CENTER);
        add(bottomPanel,  BorderLayout.SOUTH);
    }

    /** Load trips based on current filter selections */
    private void loadTrips() {
        tableModel.setRowCount(0);
        selectedTripId = -1;

        int filterDriver  = driverId > 0 ? driverId : -1; // Driver-mode uses their ID
        int filterCompany = -1;
        int filterMonth   = -1;
        int filterYear    = -1;

        // Read filter values (admin only)
        if (isAdmin && driverFilterCombo != null) {
            Driver selectedDriver = (Driver) driverFilterCombo.getSelectedItem();
            if (selectedDriver != null && selectedDriver.getId() > 0) filterDriver = selectedDriver.getId();
        }

        if (isAdmin && companyFilterCombo != null) {
            Company selectedCompany = (Company) companyFilterCombo.getSelectedItem();
            if (selectedCompany != null && selectedCompany.getId() > 0) filterCompany = selectedCompany.getId();
        }

        // Month
        int monthIndex = monthFilterCombo.getSelectedIndex();
        if (monthIndex > 0) filterMonth = monthIndex; // index 1 = January = month 1

        // Year
        String yearStr = (String) yearFilterCombo.getSelectedItem();
        if (!"All".equals(yearStr)) {
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
    }

    private void deleteTrip() {
        if (selectedTripId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trip to delete!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this trip record?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (tripDAO.deleteTrip(selectedTripId)) {
                JOptionPane.showMessageDialog(this, "Trip deleted!");
                loadTrips();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete trip!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetFilters() {
        monthFilterCombo.setSelectedIndex(0);
        yearFilterCombo.setSelectedItem("2026");
        if (driverFilterCombo != null)  driverFilterCombo.setSelectedIndex(0);
        if (companyFilterCombo != null) companyFilterCombo.setSelectedIndex(0);
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
