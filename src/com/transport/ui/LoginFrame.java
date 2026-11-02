package com.transport.ui;

import com.transport.dao.AdminDAO;
import com.transport.dao.DriverDAO;
import com.transport.model.Admin;
import com.transport.model.Company;
import com.transport.model.Driver;
import com.transport.util.Validation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * LoginFrame.java - Multi-Tenant Authentication Portal for MOT Logistics
 *
 * Implements two distinct login paradigms:
 * 1. Company Admin Login (Tenant-isolated company management)
 * 2. Driver / Vehicle Owner Login (Multi-vehicle operator management)
 *
 * Also includes full self-service registration and password reset recovery.
 */
public class LoginFrame extends JFrame {

    private JTextField     adminUsernameField;
    private JPasswordField adminPasswordField;
    private JTextField     driverUsernameField;
    private JPasswordField driverPasswordField;

    private AdminDAO  adminDAO  = new AdminDAO();
    private DriverDAO driverDAO = new DriverDAO();

    public LoginFrame() {
        setTitle("MOT Logistics - Authentication & Access Portal");
        setSize(520, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(UITheme.BG_LIGHT);

        // Header Title Banner
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        headerPanel.setOpaque(false);

        JLabel brandLabel = new JLabel("MOT LOGISTICS", SwingConstants.CENTER);
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandLabel.setForeground(UITheme.BRAND_BLUE);

        JLabel subtitleLabel = new JLabel("Transport Trip Management & Automated Monthly Billing", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(brandLabel);
        headerPanel.add(subtitleLabel);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_HEADER);
        tabs.addTab("  🏢 Company Admin  ", createAdminLoginPanel());
        tabs.addTab("  🚚 Driver / Owner  ", createDriverLoginPanel());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabs, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createAdminLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLbl = new JLabel("Username or Email:");
        userLbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(userLbl, gbc);

        gbc.gridx = 1;
        adminUsernameField = new JTextField(15);
        adminUsernameField.setFont(UITheme.FONT_BODY);
        adminUsernameField.setPreferredSize(new Dimension(190, 32));
        panel.add(adminUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(passLbl, gbc);

        gbc.gridx = 1;
        adminPasswordField = new JPasswordField(15);
        adminPasswordField.setFont(UITheme.FONT_BODY);
        adminPasswordField.setPreferredSize(new Dimension(190, 32));
        panel.add(adminPasswordField, gbc);

        // Submit Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        ModernButton loginBtn = new ModernButton("Login as Company Admin", UITheme.BRAND_BLUE);
        loginBtn.setPreferredSize(new Dimension(220, 38));
        loginBtn.addActionListener(this::handleAdminLogin);
        panel.add(loginBtn, gbc);

        // Links Panel (Register & Forgot Password)
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 6, 4, 6);
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        linksPanel.setOpaque(false);

        JButton regBtn = createLinkButton("➕ Register Company");
        regBtn.addActionListener(e -> showRegisterCompanyDialog());

        JButton forgotBtn = createLinkButton("🔑 Forgot Password?");
        forgotBtn.addActionListener(e -> showForgotCompanyPasswordDialog());

        linksPanel.add(regBtn);
        linksPanel.add(new JLabel("|"));
        linksPanel.add(forgotBtn);
        panel.add(linksPanel, gbc);

        // Demo credentials hint
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 6, 2, 6);
        JLabel hint = new JLabel("<html><center style='color:#64748b;font-size:10px;'>" +
                "Demo: Om Sai (<b>omsai / omsai123</b>)<br>" +
                "Oracle (<b>oracle / oracle123</b>) | XYZ (<b>xyz / xyz123</b>)</center></html>", SwingConstants.CENTER);
        panel.add(hint, gbc);

        return panel;
    }

    private JPanel createDriverLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLbl = new JLabel("Username or Email:");
        userLbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(userLbl, gbc);

        gbc.gridx = 1;
        driverUsernameField = new JTextField(15);
        driverUsernameField.setFont(UITheme.FONT_BODY);
        driverUsernameField.setPreferredSize(new Dimension(190, 32));
        panel.add(driverUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(passLbl, gbc);

        gbc.gridx = 1;
        driverPasswordField = new JPasswordField(15);
        driverPasswordField.setFont(UITheme.FONT_BODY);
        driverPasswordField.setPreferredSize(new Dimension(190, 32));
        panel.add(driverPasswordField, gbc);

        // Submit Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        ModernButton loginBtn = new ModernButton("Login as Driver", UITheme.SUCCESS_GREEN);
        loginBtn.setPreferredSize(new Dimension(220, 38));
        loginBtn.addActionListener(this::handleDriverLogin);
        panel.add(loginBtn, gbc);

        // Links Panel
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 6, 4, 6);
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        linksPanel.setOpaque(false);

        JButton regBtn = createLinkButton("➕ Register Driver");
        regBtn.addActionListener(e -> showRegisterDriverDialog());

        JButton forgotBtn = createLinkButton("🔑 Forgot Password?");
        forgotBtn.addActionListener(e -> showForgotDriverPasswordDialog());

        linksPanel.add(regBtn);
        linksPanel.add(new JLabel("|"));
        linksPanel.add(forgotBtn);
        panel.add(linksPanel, gbc);

        // Demo credentials hint
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 6, 2, 6);
        JLabel hint = new JLabel("<html><center style='color:#64748b;font-size:10px;'>" +
                "Demo: Rahul (<b>rahul / rahul123</b>)<br>" +
                "Amit (<b>amit / amit123</b>) | Sagar (<b>sagar / sagar123</b>)</center></html>", SwingConstants.CENTER);
        panel.add(hint, gbc);

        return panel;
    }

    private JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_SMALL);
        btn.setForeground(UITheme.BRAND_BLUE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void handleAdminLogin(ActionEvent e) {
        String username = adminUsernameField.getText().trim();
        String password = new String(adminPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username/email and password.", "Missing Credentials", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Admin admin = adminDAO.login(username, password);

        if (admin != null) {
            JOptionPane.showMessageDialog(this,
                    "Authentication successful!\n\nWelcome back, " + admin.getAdminName() +
                    "\nAccessing tenant command center: " + admin.getCompanyName(),
                    "Welcome " + admin.getCompanyName(), JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new AdminDashboard(admin).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid company admin credentials!\nCheck username or password and try again.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDriverLogin(ActionEvent e) {
        String username = driverUsernameField.getText().trim();
        String password = new String(driverPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username/email and password.", "Missing Credentials", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Driver driver = driverDAO.login(username, password);

        if (driver != null) {
            JOptionPane.showMessageDialog(this,
                    "Welcome, " + driver.getDriverName() + "!\nAccessing driver operations portal.",
                    "Authentication Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new DriverDashboard(driver).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid driver credentials or account inactive!\nCheck your username or password.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegisterCompanyDialog() {
        JTextField compName = new JTextField();
        JTextField admName  = new JTextField();
        JTextField email    = new JTextField();
        JTextField user     = new JTextField();
        JPasswordField pass = new JPasswordField();
        JTextField phone    = new JTextField();
        JTextField addr     = new JTextField();
        JTextField gst      = new JTextField();

        JPanel form = new JPanel(new GridLayout(8, 2, 6, 6));
        form.add(new JLabel("Company Name: *"));
        form.add(compName);
        form.add(new JLabel("Admin Contact Person: *"));
        form.add(admName);
        form.add(new JLabel("Official Email Address: *"));
        form.add(email);
        form.add(new JLabel("Login Username: *"));
        form.add(user);
        form.add(new JLabel("Login Password: *"));
        form.add(pass);
        form.add(new JLabel("Contact Number:"));
        form.add(phone);
        form.add(new JLabel("Office Address:"));
        form.add(addr);
        form.add(new JLabel("GST Number (Optional):"));
        form.add(gst);

        int res = JOptionPane.showConfirmDialog(this, form, "Register New Company Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String cName = compName.getText().trim();
            String aName = admName.getText().trim();
            String em    = email.getText().trim();
            String u     = user.getText().trim();
            String p     = new String(pass.getPassword()).trim();

            if (cName.isEmpty() || aName.isEmpty() || em.isEmpty() || u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All starred (*) fields are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Company company = new Company();
            company.setCompanyName(cName);
            company.setAdminName(aName);
            company.setEmail(em);
            company.setUsername(u);
            company.setPassword(p);
            company.setContactNumber(phone.getText().trim());
            company.setAddress(addr.getText().trim());
            company.setGstNumber(gst.getText().trim());

            if (adminDAO.registerCompany(company)) {
                JOptionPane.showMessageDialog(this, "Company registered successfully!\nYou can now log in with your credentials.", "Success", JOptionPane.INFORMATION_MESSAGE);
                adminUsernameField.setText(u);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Company name, email, or username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRegisterDriverDialog() {
        JTextField name     = new JTextField();
        JTextField mobile   = new JTextField();
        JTextField email    = new JTextField();
        JTextField licence  = new JTextField();
        JTextField username = new JTextField();
        JPasswordField pass = new JPasswordField();
        JTextField vehReg   = new JTextField();
        String[] types = {"Tempo", "Pickup Van", "Truck", "Heavy Truck", "Trailer"};
        JComboBox<String> vehType = new JComboBox<>(types);

        JPanel form = new JPanel(new GridLayout(8, 2, 6, 6));
        form.add(new JLabel("Driver Full Name: *"));
        form.add(name);
        form.add(new JLabel("Mobile Number: *"));
        form.add(mobile);
        form.add(new JLabel("Email Address:"));
        form.add(email);
        form.add(new JLabel("Licence Number: *"));
        form.add(licence);
        form.add(new JLabel("Login Username: *"));
        form.add(username);
        form.add(new JLabel("Login Password: *"));
        form.add(pass);
        form.add(new JLabel("Vehicle Reg. Number: *"));
        form.add(vehReg);
        form.add(new JLabel("Vehicle Type: *"));
        form.add(vehType);

        int res = JOptionPane.showConfirmDialog(this, form, "Register Commercial Driver & Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String n = name.getText().trim();
            String m = mobile.getText().trim();
            String l = licence.getText().trim();
            String u = username.getText().trim();
            String p = new String(pass.getPassword()).trim();
            String v = vehReg.getText().trim().toUpperCase();

            if (n.isEmpty() || m.isEmpty() || l.isEmpty() || u.isEmpty() || p.isEmpty() || v.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All starred (*) fields are mandatory!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Driver driver = new Driver();
            driver.setDriverName(n);
            driver.setMobileNumber(m);
            driver.setEmail(email.getText().trim());
            driver.setLicenceNumber(l);
            driver.setUsername(u);
            driver.setPassword(p);
            driver.setActive(true);

            if (driverDAO.registerDriverWithVehicle(driver, v, (String) vehType.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, "Driver and Vehicle registered successfully!\nYou can now log in.", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                driverUsernameField.setText(u);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register. Username or vehicle plate number might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showForgotCompanyPasswordDialog() {
        JTextField userOrEmailField = new JTextField();
        JPasswordField newPassField = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Company Username or Email:"));
        form.add(userOrEmailField);
        form.add(new JLabel("Enter New Password:"));
        form.add(newPassField);

        int res = JOptionPane.showConfirmDialog(this, form, "Reset Company Admin Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String id = userOrEmailField.getText().trim();
            String pass = new String(newPassField.getPassword()).trim();

            if (id.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username/Email and New Password cannot be empty.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (adminDAO.resetPassword(id, pass)) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!\nYou can now log in with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password. Username or email not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showForgotDriverPasswordDialog() {
        JTextField userOrEmailField = new JTextField();
        JPasswordField newPassField = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Driver Username or Email:"));
        form.add(userOrEmailField);
        form.add(new JLabel("Enter New Password:"));
        form.add(newPassField);

        int res = JOptionPane.showConfirmDialog(this, form, "Reset Driver Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String id = userOrEmailField.getText().trim();
            String pass = new String(newPassField.getPassword()).trim();

            if (id.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username/Email and New Password cannot be empty.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (driverDAO.resetPassword(id, pass)) {
                JOptionPane.showMessageDialog(this, "Driver password updated successfully!\nYou can now log in with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password. Username or email not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
