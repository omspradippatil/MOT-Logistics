package com.transport.ui;

import com.transport.dao.TripDAO;
import com.transport.model.Admin;
import com.transport.model.Trip;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * AdminDashboard.java - Company Administrator Command Center
 *
 * Implements strict multi-tenant isolation with real-time automatic synchronization:
 * - As soon as any driver submits a trip in Driver Login, it automatically updates
 *   in this company portal via background sync and window focus listeners.
 * - Prominently identifies WHICH DRIVER created every trip across all company views.
 */
public class AdminDashboard extends JFrame {

    private Admin admin;
    private TripDAO tripDAO = new TripDAO();

    private JTabbedPane tabs;
    private CompanyOverviewPanel overviewPanel;
    private CompanyDriversPanel driversPanel;
    private CompanyTripManagementPanel tripsPanel;
    private CompanyMonthlyInvoicePanel invoicesPanel;
    private CompanyPaymentManagementPanel paymentsPanel;
    private CompanyProfilePanel profilePanel;

    private Timer autoSyncTimer;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        setTitle("MOT Logistics - " + admin.getCompanyName() + " Admin Command Center");
        setSize(1160, 740);
        setMinimumSize(new Dimension(1040, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        initAutoSync();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BRAND_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);

        JLabel brandLabel = new JLabel("MOT LOGISTICS  |  " + admin.getCompanyName().toUpperCase());
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        JLabel liveBadge = new JLabel("● Live Connected (Driver Sync)");
        liveBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        liveBadge.setForeground(new Color(34, 197, 94)); // green

        brandRow.add(brandLabel);
        brandRow.add(liveBadge);

        JLabel userLabel = new JLabel("Company Admin: " + admin.getAdminName() + "  |  Email: " + admin.getEmail());
        userLabel.setFont(UITheme.FONT_SMALL);
        userLabel.setForeground(new Color(148, 163, 184));

        titleBlock.add(brandRow);
        titleBlock.add(userLabel);

        // Header Action Buttons
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);

        ModernButton syncBtn = new ModernButton("↺ Live Sync", UITheme.UPDATE_BLUE);
        syncBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        syncBtn.setPreferredSize(new Dimension(110, 32));
        syncBtn.setToolTipText("Immediately pull latest trips created by drivers from database");
        syncBtn.addActionListener(e -> {
            refreshActiveTab();
            JOptionPane.showMessageDialog(this,
                    "Live data refreshed!\nAll trips created by drivers for " + admin.getCompanyName() + " are up to date.",
                    "Fleet Synced", JOptionPane.INFORMATION_MESSAGE);
        });

        ModernButton logoutBtn = new ModernButton("Logout", UITheme.DANGER_RED);
        logoutBtn.setPreferredSize(new Dimension(90, 32));
        logoutBtn.addActionListener(e -> {
            if (autoSyncTimer != null) autoSyncTimer.stop();
            dispose();
            new LoginFrame().setVisible(true);
        });

        rightHeader.add(syncBtn);
        rightHeader.add(logoutBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // Tabbed Workstation Navigation
        tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        overviewPanel  = new CompanyOverviewPanel();
        driversPanel   = new CompanyDriversPanel(admin);
        tripsPanel     = new CompanyTripManagementPanel(admin);
        invoicesPanel  = new CompanyMonthlyInvoicePanel(admin);
        paymentsPanel  = new CompanyPaymentManagementPanel(admin);
        profilePanel   = new CompanyProfilePanel(admin);

        tabs.addTab("  📊 Dashboard  ",          overviewPanel);
        tabs.addTab("  👨 Drivers  ",            driversPanel);
        tabs.addTab("  📋 Trip Management  ",    tripsPanel);
        tabs.addTab("  📄 Monthly Invoice  ",    invoicesPanel);
        tabs.addTab("  💰 Payment Management  ", paymentsPanel);
        tabs.addTab("  ⚙️ Company Profile  ",    profilePanel);

        tabs.addChangeListener(e -> refreshActiveTab());

        add(headerPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void initAutoSync() {
        // Automatic background polling every 3.5 seconds
        autoSyncTimer = new Timer(3500, e -> {
            if (isVisible() && getState() != Frame.ICONIFIED) {
                refreshActiveTab();
            }
        });
        autoSyncTimer.start();

        // Refresh immediately whenever window gains focus
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                refreshActiveTab();
            }
        });
    }

    /**
     * Refreshes whichever tab is currently active.
     */
    public void refreshActiveTab() {
        int selectedIndex = tabs.getSelectedIndex();
        Component comp = tabs.getComponentAt(selectedIndex);
        if (comp instanceof Refreshable) {
            ((Refreshable) comp).refreshData();
        }
    }

    /**
     * Inner Class: CompanyOverviewPanel
     * Displays summary metrics and recent trips with clear driver attribution.
     */
    private class CompanyOverviewPanel extends JPanel implements Refreshable {

        private JLabel totalTripsVal;
        private JLabel totalDriversVal;
        private JLabel totalVehiclesVal;
        private JLabel monthBillingVal;
        private JLabel paidAmountVal;
        private JLabel pendingAmountVal;

        private JTable table;
        private DefaultTableModel tableModel;

        public CompanyOverviewPanel() {
            setLayout(new BorderLayout(15, 15));
            setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
            setBackground(UITheme.BG_LIGHT);

            initPanel();
            refreshData();
        }

        private void initPanel() {
            // 2x3 Stat Cards Grid
            JPanel statsPanel = new JPanel(new GridLayout(2, 3, 14, 14));
            statsPanel.setOpaque(false);

            totalTripsVal    = new JLabel("0");
            totalDriversVal  = new JLabel("0");
            totalVehiclesVal = new JLabel("0");
            monthBillingVal  = new JLabel("₹ 0");
            paidAmountVal    = new JLabel("₹ 0");
            pendingAmountVal = new JLabel("₹ 0");

            statsPanel.add(buildStatCard("Total Trips Completed", totalTripsVal, "Completed shipments for " + admin.getCompanyName(), UITheme.BRAND_BLUE));
            statsPanel.add(buildStatCard("Total Drivers Engaged", totalDriversVal, "Commercial fleet operators", UITheme.SUCCESS_GREEN));
            statsPanel.add(buildStatCard("Vehicles Employed", totalVehiclesVal, "Tempos, pickup vans & trucks", UITheme.UPDATE_BLUE));
            statsPanel.add(buildStatCard("Current Month Billing", monthBillingVal, "Gross monthly transport billing", new Color(124, 58, 237)));
            statsPanel.add(buildStatCard("Total Paid Invoices", paidAmountVal, "Settled invoice amounts", new Color(13, 148, 136)));
            statsPanel.add(buildStatCard("Total Pending Invoices", pendingAmountVal, "Outstanding unpaid balance", UITheme.DANGER_RED));

            // Recent Trips Section with live driver attribution
            JPanel recentPanel = new JPanel(new BorderLayout(8, 8));
            recentPanel.setBackground(UITheme.CARD_BG);

            // Table Header Bar
            JPanel tableHeaderBar = new JPanel(new BorderLayout());
            tableHeaderBar.setOpaque(false);
            tableHeaderBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));

            JLabel recentTitle = new JLabel("Recent Operational Trips for " + admin.getCompanyName() + " (Directly Created by Drivers)");
            recentTitle.setFont(UITheme.FONT_BODY_BOLD);
            recentTitle.setForeground(UITheme.BRAND_NAVY);

            ModernButton refreshTripsBtn = new ModernButton("↺ Refresh", UITheme.SECONDARY_GRAY);
            refreshTripsBtn.setPreferredSize(new Dimension(90, 28));
            refreshTripsBtn.addActionListener(e -> refreshData());

            tableHeaderBar.add(recentTitle, BorderLayout.WEST);
            tableHeaderBar.add(refreshTripsBtn, BorderLayout.EAST);

            // Columns clearly emphasizing WHICH DRIVER created the trip
            String[] cols = {
                "Trip ID", "Trip Date", "Driver (Created By)", "Vehicle Reg.",
                "From (Source)", "To (Destination)", "Cargo / Goods", "Fare Amount (₹)", "Status"
            };
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(tableModel);
            UITheme.styleTable(table);

            // Explicit column widths so nothing is truncated
            table.getColumnModel().getColumn(0).setPreferredWidth(85);   // Trip ID
            table.getColumnModel().getColumn(1).setPreferredWidth(95);   // Date
            table.getColumnModel().getColumn(2).setPreferredWidth(140);  // Driver (Created By)
            table.getColumnModel().getColumn(3).setPreferredWidth(125);  // Vehicle
            table.getColumnModel().getColumn(4).setPreferredWidth(110);  // From
            table.getColumnModel().getColumn(5).setPreferredWidth(110);  // To
            table.getColumnModel().getColumn(6).setPreferredWidth(160);  // Cargo
            table.getColumnModel().getColumn(7).setPreferredWidth(110);  // Amount
            table.getColumnModel().getColumn(8).setPreferredWidth(90);   // Status

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                        showTripDetailsDialog(table.getSelectedRow());
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

            recentPanel.add(tableHeaderBar, BorderLayout.NORTH);
            recentPanel.add(scrollPane,     BorderLayout.CENTER);

            add(statsPanel, BorderLayout.NORTH);
            add(recentPanel, BorderLayout.CENTER);
        }

        private JPanel buildStatCard(String title, JLabel valueLbl, String subtitle, Color accentColor) {
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

            valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
            valueLbl.setForeground(UITheme.TEXT_DARK);

            JLabel subLabel = new JLabel(subtitle);
            subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            subLabel.setForeground(new Color(148, 163, 184));

            card.add(titleLabel, BorderLayout.NORTH);
            card.add(valueLbl,   BorderLayout.CENTER);
            card.add(subLabel,   BorderLayout.SOUTH);
            return card;
        }

        private void showTripDetailsDialog(int row) {
            String tripCode = (String) tableModel.getValueAt(row, 0);
            String tripDate = (String) tableModel.getValueAt(row, 1);
            String driver   = (String) tableModel.getValueAt(row, 2);
            String vehicle  = (String) tableModel.getValueAt(row, 3);
            String from     = (String) tableModel.getValueAt(row, 4);
            String to       = (String) tableModel.getValueAt(row, 5);
            String goods    = (String) tableModel.getValueAt(row, 6);
            String amount   = (String) tableModel.getValueAt(row, 7);
            String status   = (String) tableModel.getValueAt(row, 8);

            String msg =
                "====================================================\n" +
                "       OPERATIONAL TRIP AUDIT & DRIVER CONNECTION   \n" +
                "====================================================\n\n" +
                "Trip Reference ID    : " + tripCode + "\n" +
                "Trip Date            : " + tripDate + "\n" +
                "Client Company       : " + admin.getCompanyName() + "\n\n" +
                "DRIVER ATTRIBUTION:\n" +
                "Created By Driver    : " + driver + "\n" +
                "Assigned Vehicle Reg.: " + vehicle + "\n\n" +
                "ROUTE & CARGO:\n" +
                "Source (Pickup)      : " + from + "\n" +
                "Destination (Drop)   : " + to + "\n" +
                "Cargo Description    : " + goods + "\n" +
                "Trip Fare Amount     : " + amount + "\n" +
                "Operational Status   : " + status + "\n\n" +
                "====================================================";

            JOptionPane.showMessageDialog(this, msg, "Trip Details - " + tripCode, JOptionPane.INFORMATION_MESSAGE);
        }

        @Override
        public void refreshData() {
            // Live stats from database
            Map<String, Object> stats = tripDAO.getCompanyStats(admin.getCompanyId());
            int totalTrips       = (int) stats.getOrDefault("totalTrips", 0);
            int totalDrivers     = (int) stats.getOrDefault("totalDrivers", 0);
            int totalVehicles    = (int) stats.getOrDefault("totalVehicles", 0);
            double monthBilling  = (double) stats.getOrDefault("currentMonthBilling", 0.0);
            double paidAmount    = (double) stats.getOrDefault("totalPaidAmount", 0.0);
            double pendingAmount = (double) stats.getOrDefault("totalPendingAmount", 0.0);

            totalTripsVal.setText(String.valueOf(totalTrips));
            totalDriversVal.setText(String.valueOf(totalDrivers));
            totalVehiclesVal.setText(String.valueOf(totalVehicles));
            monthBillingVal.setText("₹ " + String.format("%,.0f", monthBilling));
            paidAmountVal.setText("₹ " + String.format("%,.0f", paidAmount));
            pendingAmountVal.setText("₹ " + String.format("%,.0f", pendingAmount));

            // Live recent trips from database
            tableModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            List<Trip> recentTrips = tripDAO.getTripsByCompany(admin.getCompanyId(), -1, -1, -1, -1, null);
            int limit = Math.min(10, recentTrips.size());
            for (int i = 0; i < limit; i++) {
                Trip t = recentTrips.get(i);
                tableModel.addRow(new Object[]{
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
        }
    }
}
