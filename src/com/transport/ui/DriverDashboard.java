package com.transport.ui;

import com.transport.model.Driver;

import javax.swing.*;
import java.awt.*;

/**
 * DriverDashboard.java - Driver Operations Portal for MOT Logistics
 */
public class DriverDashboard extends JFrame {

    private Driver driver;

    public DriverDashboard(Driver driver) {
        this.driver = driver;
        setTitle("MOT Logistics - Driver Portal (" + driver.getDriverName() + ")");
        setSize(960, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // Top Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BRAND_NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);

        JLabel brandLabel = new JLabel("MOT LOGISTICS");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        JLabel driverLabel = new JLabel("Driver: " + driver.getDriverName() + "  |  Mobile: " + driver.getMobileNumber());
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

        // Driver Workspace Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);

        tabs.addTab("  ➕ New Trip Entry  ", new TripEntryPanel(driver));
        tabs.addTab("  📋 My Trip History  ", new TripHistoryPanel(false, driver.getId()));
        tabs.addTab("  📄 My Invoices & Vouchers  ", new DriverInvoicesPanel(driver));

        tabs.addChangeListener(e -> {
            Component comp = tabs.getSelectedComponent();
            if (comp instanceof Refreshable) {
                ((Refreshable) comp).refreshData();
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}
