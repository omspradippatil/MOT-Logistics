package com.transport.ui;

import com.transport.dao.TripDAO;
import com.transport.model.Admin;
import com.transport.model.Trip;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * AdminDashboard.java - Company Administrator Command Center
 *
 * Implements strict multi-tenant isolation:
 * Company Admin sees ONLY operational and billing data for their own company.
 */
public class AdminDashboard extends JFrame {

    private Admin admin;
    private TripDAO tripDAO = new TripDAO();

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        setTitle("MOT Logistics - " + admin.getCompanyName() + " Admin Command Center");
        setSize(1160, 740);
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

        JLabel brandLabel = new JLabel("MOT LOGISTICS  |  " + admin.getCompanyName().toUpperCase());
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Company Admin: " + admin.getAdminName() + "  |  Email: " + admin.getEmail());
        userLabel.setFont(UITheme.FONT_SMALL);
        userLabel.setForeground(new Color(148, 163, 184));

        titleBlock.add(brandLabel);
        titleBlock.add(userLabel);

        ModernButton logoutBtn = new ModernButton("Logout", UITheme.DANGER_RED);
        logoutBtn.setPreferredSize(new Dimension(100, 32));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        // Tabbed Workstation Navigation
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  📊 Dashboard  ",          createCompanyDashboardPanel());
        tabs.addTab("  👨 Drivers  ",            new CompanyDriversPanel(admin));
        tabs.addTab("  📋 Trip Management  ",    new CompanyTripManagementPanel(admin));
        tabs.addTab("  📄 Monthly Invoice  ",    new CompanyMonthlyInvoicePanel(admin));
        tabs.addTab("  💰 Payment Management  ", new CompanyPaymentManagementPanel(admin));
        tabs.addTab("  ⚙️ Company Profile  ",    new CompanyProfilePanel(admin));

        tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex == 0) {
                tabs.setComponentAt(0, createCompanyDashboardPanel());
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

    private JPanel createCompanyDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        panel.setBackground(UITheme.BG_LIGHT);

        // Stats Map for strictly this company
        Map<String, Object> stats = tripDAO.getCompanyStats(admin.getCompanyId());
        int totalTrips       = (int) stats.getOrDefault("totalTrips", 0);
        int totalDrivers     = (int) stats.getOrDefault("totalDrivers", 0);
        int totalVehicles    = (int) stats.getOrDefault("totalVehicles", 0);
        double monthBilling  = (double) stats.getOrDefault("currentMonthBilling", 0.0);
        double paidAmount    = (double) stats.getOrDefault("totalPaidAmount", 0.0);
        double pendingAmount = (double) stats.getOrDefault("totalPendingAmount", 0.0);

        // 2x3 Stat Cards Grid
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 14, 14));
        statsPanel.setOpaque(false);

        statsPanel.add(createModernCard("Total Trips Completed", String.valueOf(totalTrips), "Completed shipments for " + admin.getCompanyName(), UITheme.BRAND_BLUE));
        statsPanel.add(createModernCard("Total Drivers Engaged", String.valueOf(totalDrivers), "Commercial fleet operators", UITheme.SUCCESS_GREEN));
        statsPanel.add(createModernCard("Vehicles Employed", String.valueOf(totalVehicles), "Tempos, pickup vans & trucks", UITheme.UPDATE_BLUE));
        statsPanel.add(createModernCard("Current Month Billing", "₹ " + String.format("%,.0f", monthBilling), "Gross monthly transport billing", new Color(124, 58, 237)));
        statsPanel.add(createModernCard("Total Paid Invoices", "₹ " + String.format("%,.0f", paidAmount), "Settled invoice amounts", new Color(13, 148, 136)));
        statsPanel.add(createModernCard("Total Pending Invoices", "₹ " + String.format("%,.0f", pendingAmount), "Outstanding unpaid invoice balance", UITheme.DANGER_RED));

        // Recent Trips Section
        JPanel recentPanel = new JPanel(new BorderLayout(8, 8));
        recentPanel.setBackground(UITheme.CARD_BG);
        recentPanel.setBorder(UITheme.createCardBorder("Recent Operational Trips for " + admin.getCompanyName()));

        String[] cols = {"Trip ID", "Date", "Driver", "Vehicle No", "From (Source)", "To (Destination)", "Cargo / Goods", "Amount (₹)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<Trip> recentTrips = tripDAO.getTripsByCompany(admin.getCompanyId(), -1, -1, -1, -1, null);
        int limit = Math.min(8, recentTrips.size());
        for (int i = 0; i < limit; i++) {
            Trip t = recentTrips.get(i);
            model.addRow(new Object[]{
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
