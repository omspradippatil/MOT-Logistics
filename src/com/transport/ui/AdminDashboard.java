package com.transport.ui;

import com.transport.dao.*;
import com.transport.model.Admin;

import javax.swing.*;
import java.awt.*;

/**
 * AdminDashboard.java - Executive Command Center for MOT Logistics
 */
public class AdminDashboard extends JFrame {

    private Admin admin;

    private TripDAO    tripDAO    = new TripDAO();
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private DriverDAO  driverDAO  = new DriverDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        setTitle("MOT Logistics - Admin Command Center (" + admin.getFullName() + ")");
        setSize(1080, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // Top Brand Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BRAND_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);

        JLabel brandLabel = new JLabel("MOT LOGISTICS");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Logged in as Admin: " + admin.getFullName());
        userLabel.setFont(UITheme.FONT_SMALL);
        userLabel.setForeground(new Color(148, 163, 184)); // Cool slate

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

        tabs.addTab("  📊 Dashboard  ",   createDashboardPanel());
        tabs.addTab("  🏢 Companies  ",   new CompanyPanel());
        tabs.addTab("  👨 Drivers  ",     new DriverPanel());
        tabs.addTab("  🚛 Vehicles  ",    new VehiclePanel());
        tabs.addTab("  📋 Trip History  ",new TripHistoryPanel(true, -1));
        tabs.addTab("  📄 Invoices  ",    new InvoicePanel());
        tabs.addTab("  💰 Payments  ",    new PaymentPanel());

        tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex == 0) {
                tabs.setComponentAt(0, createDashboardPanel());
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

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_LIGHT);

        JLabel welcomeLabel = new JLabel("Fleet & Operational Performance Overview", SwingConstants.LEFT);
        welcomeLabel.setFont(UITheme.FONT_HEADER);
        welcomeLabel.setForeground(UITheme.BRAND_BLUE);

        // 2x3 Stat Card Grid
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 16, 16));
        statsPanel.setOpaque(false);

        int totalCompanies  = companyDAO.getAllCompanies().size();
        int totalDrivers    = driverDAO.getAllDrivers().size();
        int totalVehicles   = vehicleDAO.getAllVehicles().size();
        int totalTrips      = tripDAO.getTotalTripCount();
        double monthEarning = tripDAO.getCurrentMonthTotal();
        double pendingAmt   = paymentDAO.getTotalPendingAmount();
        int totalInvoices   = invoiceDAO.getTotalInvoiceCount();

        statsPanel.add(createModernCard("Registered Companies", String.valueOf(totalCompanies), "Active corporate clients", UITheme.BRAND_BLUE));
        statsPanel.add(createModernCard("Commercial Drivers",   String.valueOf(totalDrivers),   "Licensed fleet operators",  UITheme.SUCCESS_GREEN));
        statsPanel.add(createModernCard("Fleet Vehicles",       String.valueOf(totalVehicles),  "Tempos, trucks & vans",     UITheme.UPDATE_BLUE));
        statsPanel.add(createModernCard("Completed Trips",      String.valueOf(totalTrips),     "Total trips logged to date",new Color(124, 58, 237))); // Violet
        statsPanel.add(createModernCard("This Month's Revenue", "₹ " + String.format("%,.0f", monthEarning), "Current month gross billing", new Color(13, 148, 136))); // Teal
        statsPanel.add(createModernCard("Pending Receivables",  "₹ " + String.format("%,.0f", pendingAmt),   "Outstanding unpaid balances", UITheme.DANGER_RED));

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomBar.setOpaque(false);
        JLabel invSummary = new JLabel("Total Invoices Generated: " + totalInvoices + " | System Status: Active & Operational");
        invSummary.setFont(UITheme.FONT_BODY_BOLD);
        invSummary.setForeground(UITheme.TEXT_MUTED);
        bottomBar.add(invSummary);

        panel.add(welcomeLabel, BorderLayout.NORTH);
        panel.add(statsPanel,   BorderLayout.CENTER);
        panel.add(bottomBar,    BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createModernCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        // Top accent stripe / title
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(accentColor);
        topRow.add(titleLbl, BorderLayout.WEST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(UITheme.TEXT_DARK);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);

        card.add(topRow, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }
}
