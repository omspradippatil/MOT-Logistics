package com.transport.ui;

import com.transport.dao.DriverDAO;
import com.transport.model.Admin;
import com.transport.model.DriverCompanySummary;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * CompanyDriversPanel.java - Company Admin's Drivers Roster & Management
 *
 * Displays drivers associated with this specific company:
 * - Drivers registered directly by this company
 * - Drivers who have completed commercial trips for this company
 *
 * Allows Company Admin to:
 * - Add new drivers and assign vehicles directly to their company
 * - Assign existing fleet drivers to this company
 * - View driver's trips and download official monthly invoices
 */
public class CompanyDriversPanel extends JPanel implements Refreshable {

    private Admin companyAdmin;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<DriverCompanySummary> driverSummaries;
    private DriverDAO driverDAO = new DriverDAO();

    public CompanyDriversPanel(Admin companyAdmin) {
        this.companyAdmin = companyAdmin;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadDrivers();
    }

    private void initUI() {
        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.CARD_BG);
        headerPanel.setBorder(UITheme.createCardBorder("Drivers & Vehicle Operators - " + companyAdmin.getCompanyName()));

        JLabel descLabel = new JLabel("Commercial drivers and vehicle operators engaged by " + companyAdmin.getCompanyName() + ". Double-click any driver to view trips and download invoices.");
        descLabel.setFont(UITheme.FONT_BODY);
        descLabel.setForeground(UITheme.TEXT_MUTED);
        descLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        headerPanel.add(descLabel, BorderLayout.CENTER);

        // Drivers Table
        String[] cols = {"Driver ID", "Driver Name", "Mobile Number", "Email", "Vehicle Number(s)", "Total Trips Completed"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Hide Driver ID column from display but keep in model
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openSelectedDriverTrips();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Assigned Driver Roster"));

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        ModernButton addDriverBtn = new ModernButton("➕ Add Driver to Company", UITheme.SUCCESS_GREEN);
        addDriverBtn.setPreferredSize(new Dimension(220, 36));
        addDriverBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addDriverBtn.addActionListener(e -> showAddDriverDialog());

        ModernButton viewTripsBtn = new ModernButton("🔍 View Driver's Trips & Invoices", UITheme.BRAND_BLUE);
        viewTripsBtn.setPreferredSize(new Dimension(250, 36));
        viewTripsBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewTripsBtn.addActionListener(e -> openSelectedDriverTrips());

        ModernButton refreshBtn = new ModernButton("↺ Refresh", UITheme.SECONDARY_GRAY);
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.addActionListener(e -> loadDrivers());

        bottomBar.add(addDriverBtn);
        bottomBar.add(viewTripsBtn);
        bottomBar.add(refreshBtn);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(bottomBar,   BorderLayout.SOUTH);
    }

    public void loadDrivers() {
        tableModel.setRowCount(0);
        // STRICT SCOPING: Fetch drivers who completed trips for companyAdmin OR registered by companyAdmin
        driverSummaries = driverDAO.getDriversForCompany(companyAdmin.getCompanyId());

        for (DriverCompanySummary s : driverSummaries) {
            tableModel.addRow(new Object[]{
                s.getDriverId(),
                s.getDriverName(),
                s.getMobileNumber(),
                s.getEmail() != null ? s.getEmail() : "N/A",
                s.getVehicleNumbers(),
                s.getTotalTrips()
            });
        }
    }

    private void showAddDriverDialog() {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        AddDriverToCompanyDialog dialog = new AddDriverToCompanyDialog(topFrame, companyAdmin, this::loadDrivers);
        dialog.setVisible(true);
    }

    private void openSelectedDriverTrips() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a driver from the table first.", "Select Driver", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int driverId = (int) tableModel.getValueAt(row, 0);
        String driverName = (String) tableModel.getValueAt(row, 1);
        String vehicleNumbers = (String) tableModel.getValueAt(row, 4);

        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        DriverTripsDetailDialog dialog = new DriverTripsDetailDialog(topFrame, companyAdmin, driverId, driverName, vehicleNumbers);
        dialog.setVisible(true);
    }

    @Override
    public void refreshData() {
        loadDrivers();
    }
}
