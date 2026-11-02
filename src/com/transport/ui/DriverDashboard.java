package com.transport.ui;

import com.transport.dao.TripDAO;
import com.transport.model.Driver;
import com.transport.model.Trip;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * DriverDashboard.java - Driver & Vehicle Owner Operational Portal
 *
 * Implements the full Driver interface:
 * 1. Dashboard (Trips, Companies, Total Earnings, Current Month Trips, Pending/Paid amounts)
 * 2. Add New Trip
 * 3. My Trip History
 * 4. My Vehicles (Vehicle fleet management)
 * 5. My Monthly Invoices (Company & month selector, PDF generator)
 * 6. Profile & Settings
 */
public class DriverDashboard extends JFrame {

    private Driver driver;
    private TripDAO tripDAO = new TripDAO();

    public DriverDashboard(Driver driver) {
        this.driver = driver;
        setTitle("MOT Logistics - Driver Portal (" + driver.getDriverName() + ")");
        setSize(1140, 720);
        setMinimumSize(new Dimension(1040, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BRAND_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);

        JLabel brandLabel = new JLabel("MOT LOGISTICS  |  DRIVER & VEHICLE OWNER PORTAL");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        JLabel driverLabel = new JLabel("Driver: " + driver.getDriverName() + "  |  Mobile: " + driver.getMobileNumber() + "  |  Licence: " + driver.getLicenceNumber());
        driverLabel.setFont(UITheme.FONT_SMALL);
        driverLabel.setForeground(new Color(148, 163, 184));

        titleBlock.add(brandLabel);
        titleBlock.add(driverLabel);

        ModernButton logoutBtn = new ModernButton("Logout", UITheme.DANGER_RED);
        logoutBtn.setPreferredSize(new Dimension(100, 32));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        // Tabbed Navigation
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  📊 Dashboard  ",            createDriverDashboardPanel());
        tabs.addTab("  ➕ Add New Trip  ",         new TripEntryPanel(driver));
        tabs.addTab("  📋 My Trip History  ",      new DriverTripHistoryPanel(driver));
        tabs.addTab("  🚛 My Vehicles  ",          new DriverVehiclesPanel(driver));
        tabs.addTab("  📄 My Monthly Invoices  ",  new DriverMonthlyInvoicesPanel(driver));
        tabs.addTab("  👤 Profile & Settings  ",   new DriverProfilePanel(driver));

        tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex == 0) {
                tabs.setComponentAt(0, createDriverDashboardPanel());
            } else {
                Component comp = tabs.getComponentAt(selectedIndex);
                if (comp instanceof Refreshable) {
                    ((Refreshable) comp).refreshData();
                }
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createDriverDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        panel.setBackground(UITheme.BG_LIGHT);

        // Fetch driver statistics
        Map<String, Object> stats = tripDAO.getDriverStats(driver.getId());
        int totalTrips       = (int) stats.getOrDefault("totalTrips", 0);
        int totalCompanies   = (int) stats.getOrDefault("totalCompanies", 0);
        double totalEarnings = (double) stats.getOrDefault("totalEarnings", 0.0);
        int monthTrips       = (int) stats.getOrDefault("currentMonthTrips", 0);
        double paidAmount    = (double) stats.getOrDefault("paidAmount", 0.0);
        double pendingAmount = (double) stats.getOrDefault("pendingAmount", 0.0);

        // 2x3 Stat Card Grid
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 14, 14));
        statsPanel.setOpaque(false);

        statsPanel.add(createModernCard("Total Trips Completed", String.valueOf(totalTrips), "All commercial hauls to date", UITheme.BRAND_BLUE));
        statsPanel.add(createModernCard("Companies Worked With", String.valueOf(totalCompanies), "Corporate client partners", UITheme.SUCCESS_GREEN));
        statsPanel.add(createModernCard("Total Lifetime Earnings", "₹ " + String.format("%,.0f", totalEarnings), "Cumulative freight revenue", UITheme.UPDATE_BLUE));
        statsPanel.add(createModernCard("Current Month Trips", String.valueOf(monthTrips), "Trips completed this month", new Color(124, 58, 237)));
        statsPanel.add(createModernCard("Completed Payments", "₹ " + String.format("%,.0f", paidAmount), "Received settlements", new Color(13, 148, 136)));
        statsPanel.add(createModernCard("Pending Balance", "₹ " + String.format("%,.0f", pendingAmount), "Awaiting company payment", UITheme.DANGER_RED));

        // Recent Trips Section
        JPanel recentPanel = new JPanel(new BorderLayout(8, 8));
        recentPanel.setBackground(UITheme.CARD_BG);
        recentPanel.setBorder(UITheme.createCardBorder("My Recent Operational Trips"));

        String[] cols = {"Trip ID", "Date", "Client Company", "Vehicle No", "From", "To", "Cargo / Goods", "Amount (₹)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<Trip> recentTrips = tripDAO.getTripsByDriver(driver.getId(), -1, -1, -1, -1, null);
        int limit = Math.min(8, recentTrips.size());
        for (int i = 0; i < limit; i++) {
            Trip t = recentTrips.get(i);
            model.addRow(new Object[]{
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

        JScrollPane scrollPane = new JScrollPane(table);
        recentPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(recentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createModernCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
            )
        ));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(UITheme.TEXT_DARK);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(new Color(148, 163, 184));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);
        return card;
    }
}
