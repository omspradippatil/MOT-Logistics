package com.transport.ui;

import com.transport.dao.AdminDAO;
import com.transport.dao.DriverDAO;
import com.transport.model.Admin;
import com.transport.model.Driver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * LoginFrame.java - Authentication Screen for MOT Logistics
 *
 * Modern high-contrast interface for Admin and Driver authentication.
 */
public class LoginFrame extends JFrame {

    private JTextField     adminUsernameField;
    private JPasswordField adminPasswordField;
    private JTextField     driverUsernameField;
    private JPasswordField driverPasswordField;

    private AdminDAO  adminDAO  = new AdminDAO();
    private DriverDAO driverDAO = new DriverDAO();

    public LoginFrame() {
        setTitle("MOT Logistics - Login");
        setSize(480, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(UITheme.BG_LIGHT);

        // Header Title Banner
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        headerPanel.setOpaque(false);

        JLabel brandLabel = new JLabel("MOT LOGISTICS", SwingConstants.CENTER);
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandLabel.setForeground(UITheme.BRAND_BLUE);

        JLabel subtitleLabel = new JLabel("Transport Trip Management & Automated Billing", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(brandLabel);
        headerPanel.add(subtitleLabel);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_HEADER);
        tabs.addTab("  Admin Login  ",  createAdminLoginPanel());
        tabs.addTab("  Driver Login  ", createDriverLoginPanel());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabs, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createAdminLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(userLbl, gbc);

        gbc.gridx = 1;
        adminUsernameField = new JTextField(15);
        adminUsernameField.setFont(UITheme.FONT_BODY);
        adminUsernameField.setPreferredSize(new Dimension(180, 32));
        panel.add(adminUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(passLbl, gbc);

        gbc.gridx = 1;
        adminPasswordField = new JPasswordField(15);
        adminPasswordField.setFont(UITheme.FONT_BODY);
        adminPasswordField.setPreferredSize(new Dimension(180, 32));
        panel.add(adminPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 8, 8);

        ModernButton loginBtn = new ModernButton("Login as Admin", UITheme.BRAND_BLUE);
        loginBtn.setPreferredSize(new Dimension(200, 38));
        loginBtn.addActionListener(this::handleAdminLogin);
        panel.add(loginBtn, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(4, 8, 4, 8);
        JLabel hint = new JLabel("Default Admin: admin / admin123", SwingConstants.CENTER);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setFont(UITheme.FONT_SMALL);
        panel.add(hint, gbc);

        return panel;
    }

    private JPanel createDriverLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(userLbl, gbc);

        gbc.gridx = 1;
        driverUsernameField = new JTextField(15);
        driverUsernameField.setFont(UITheme.FONT_BODY);
        driverUsernameField.setPreferredSize(new Dimension(180, 32));
        panel.add(driverUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(passLbl, gbc);

        gbc.gridx = 1;
        driverPasswordField = new JPasswordField(15);
        driverPasswordField.setFont(UITheme.FONT_BODY);
        driverPasswordField.setPreferredSize(new Dimension(180, 32));
        panel.add(driverPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 8, 8);

        ModernButton loginBtn = new ModernButton("Login as Driver", UITheme.SUCCESS_GREEN);
        loginBtn.setPreferredSize(new Dimension(200, 38));
        loginBtn.addActionListener(this::handleDriverLogin);
        panel.add(loginBtn, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(4, 8, 4, 8);
        JLabel hint = new JLabel("Default Driver: rahul / rahul123", SwingConstants.CENTER);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setFont(UITheme.FONT_SMALL);
        panel.add(hint, gbc);

        return panel;
    }

    private void handleAdminLogin(ActionEvent e) {
        String username = adminUsernameField.getText().trim();
        String password = new String(adminPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Missing Credentials", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Admin admin = adminDAO.login(username, password);

        if (admin != null) {
            JOptionPane.showMessageDialog(this, "Welcome back, " + admin.getFullName() + "!", "Authentication Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new AdminDashboard(admin).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid admin username or password!", "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDriverLogin(ActionEvent e) {
        String username = driverUsernameField.getText().trim();
        String password = new String(driverPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Missing Credentials", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Driver driver = driverDAO.login(username, password);

        if (driver != null) {
            JOptionPane.showMessageDialog(this, "Welcome, " + driver.getDriverName() + "!", "Authentication Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new DriverDashboard(driver).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials or account inactive!", "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }
}
