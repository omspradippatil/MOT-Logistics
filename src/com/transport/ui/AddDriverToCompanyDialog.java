package com.transport.ui;

import com.transport.dao.DriverDAO;
import com.transport.dao.VehicleDAO;
import com.transport.model.Admin;
import com.transport.model.Driver;
import com.transport.model.Vehicle;
import com.transport.util.Validation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * AddDriverToCompanyDialog.java - Dialog for Company Admin to Add / Assign Drivers
 *
 * Provides two convenient workflows:
 * 1. Register a brand new commercial driver and vehicle linked to this company.
 * 2. Assign / engage an existing active fleet driver for this company.
 */
public class AddDriverToCompanyDialog extends JDialog {

    private Admin companyAdmin;
    private Runnable onDriverAddedCallback;

    private DriverDAO  driverDAO  = new DriverDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();

    // Fields for New Driver Registration
    private JTextField     nameField;
    private JTextField     mobileField;
    private JTextField     emailField;
    private JTextField     licenceField;
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JTextField     vehRegField;
    private JComboBox<String> vehTypeCombo;

    // Fields for Existing Driver Assignment
    private JComboBox<Driver> existingDriverCombo;
    private JLabel            existingDriverInfo;

    public AddDriverToCompanyDialog(Frame owner, Admin companyAdmin, Runnable onDriverAddedCallback) {
        super(owner, "Add Driver to " + companyAdmin.getCompanyName(), true);
        this.companyAdmin = companyAdmin;
        this.onDriverAddedCallback = onDriverAddedCallback;

        setSize(560, 520);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(UITheme.BG_LIGHT);

        // Header Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Onboard / Assign Commercial Driver", SwingConstants.LEFT);
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.BRAND_BLUE);

        JLabel subLbl = new JLabel("Company Tenant: " + companyAdmin.getCompanyName(), SwingConstants.LEFT);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);

        titlePanel.add(titleLbl);
        titlePanel.add(subLbl);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.addTab("  ➕ Register New Driver  ",   createNewDriverPanel());
        tabs.addTab("  🔗 Assign Existing Driver  ", createAssignDriverPanel());

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(tabs,       BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createNewDriverPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        nameField     = new JTextField(18);
        mobileField   = new JTextField(18);
        emailField    = new JTextField(18);
        licenceField  = new JTextField(18);
        usernameField = new JTextField(18);
        passwordField = new JPasswordField(18);
        vehRegField   = new JTextField(18);

        String[] types = {"Tempo", "Pickup Van", "Truck", "Heavy Truck", "Trailer", "Container Truck"};
        vehTypeCombo = new JComboBox<>(types);

        int row = 0;
        addRow(form, gbc, row++, "Driver Full Name: *", nameField);
        addRow(form, gbc, row++, "Mobile Number: *", mobileField);
        addRow(form, gbc, row++, "Email Address:", emailField);
        addRow(form, gbc, row++, "Driving Licence No.: *", licenceField);
        addRow(form, gbc, row++, "Driver Login Username: *", usernameField);
        addRow(form, gbc, row++, "Driver Login Password: *", passwordField);
        addRow(form, gbc, row++, "Primary Vehicle Reg. No.: *", vehRegField);
        addRow(form, gbc, row++, "Vehicle Type: *", vehTypeCombo);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 8, 4, 8);

        ModernButton submitBtn = new ModernButton("✓ Register & Add Driver to Company", UITheme.SUCCESS_GREEN);
        submitBtn.setPreferredSize(new Dimension(280, 38));
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.addActionListener(e -> registerNewDriver());
        form.add(submitBtn, gbc);

        return form;
    }

    private JPanel createAssignDriverPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lbl = new JLabel("Select Fleet Driver:");
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        existingDriverCombo = new JComboBox<>();
        existingDriverCombo.setFont(UITheme.FONT_BODY);
        existingDriverCombo.setPreferredSize(new Dimension(240, 32));
        loadExistingDrivers();
        panel.add(existingDriverCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        existingDriverInfo = new JLabel("<html><div style='color:#64748B;font-size:11px;'>" +
                "Select an active driver to view vehicle and licence details.</div></html>");
        panel.add(existingDriverInfo, gbc);

        existingDriverCombo.addActionListener(e -> updateDriverInfoDisplay());
        updateDriverInfoDisplay();

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);

        ModernButton assignBtn = new ModernButton("✓ Assign Driver to " + companyAdmin.getCompanyName(), UITheme.BRAND_BLUE);
        assignBtn.setPreferredSize(new Dimension(280, 38));
        assignBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        assignBtn.addActionListener(e -> assignExistingDriver());
        panel.add(assignBtn, gbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        comp.setFont(UITheme.FONT_BODY);
        comp.setPreferredSize(new Dimension(210, 30));
        panel.add(comp, gbc);
    }

    private void loadExistingDrivers() {
        existingDriverCombo.removeAllItems();
        List<Driver> allDrivers = driverDAO.getActiveDrivers();
        for (Driver d : allDrivers) {
            existingDriverCombo.addItem(d);
        }
    }

    private void updateDriverInfoDisplay() {
        Driver d = (Driver) existingDriverCombo.getSelectedItem();
        if (d != null) {
            List<Vehicle> vehs = vehicleDAO.getVehiclesByDriver(d.getId());
            StringBuilder vStr = new StringBuilder();
            for (Vehicle v : vehs) {
                if (vStr.length() > 0) vStr.append(", ");
                vStr.append(v.getRegistrationNumber()).append(" (").append(v.getVehicleType()).append(")");
            }
            if (vStr.length() == 0) vStr.append("No vehicle registered");

            existingDriverInfo.setText("<html><div style='padding:6px;background:#F8FAFC;border:1px solid #E2E8F0;'>" +
                    "<b>Mobile:</b> " + d.getMobileNumber() + " &nbsp;|&nbsp; <b>Licence:</b> " + d.getLicenceNumber() + "<br>" +
                    "<b>Assigned Vehicles:</b> " + vStr.toString() + "</div></html>");
        }
    }

    private void registerNewDriver() {
        String name   = nameField.getText().trim();
        String mobile = mobileField.getText().trim();
        String email  = emailField.getText().trim();
        String lic    = licenceField.getText().trim();
        String user   = usernameField.getText().trim();
        String pass   = new String(passwordField.getPassword()).trim();
        String vehReg = vehRegField.getText().trim().toUpperCase();
        String vehType = (String) vehTypeCombo.getSelectedItem();

        if (name.isEmpty() || mobile.isEmpty() || lic.isEmpty() || user.isEmpty() || pass.isEmpty() || vehReg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all starred (*) mandatory fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validation.isValidMobile(mobile)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit Indian mobile number.", "Invalid Mobile", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.isEmpty() && !Validation.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validation.isValidVehicleNumber(vehReg)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid vehicle registration number (e.g. MH-48-JK-5555).", "Invalid Vehicle Format", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validation.isValidPassword(pass)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Invalid Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Driver driver = new Driver();
        driver.setDriverName(name);
        driver.setMobileNumber(mobile);
        driver.setEmail(email.isEmpty() ? null : email);
        driver.setLicenceNumber(lic);
        driver.setUsername(user);
        driver.setPassword(pass);
        driver.setActive(true);
        driver.setCompanyId(companyAdmin.getCompanyId());

        boolean ok = driverDAO.addDriverForCompany(driver, vehReg, vehType, companyAdmin.getCompanyId());

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Driver successfully registered and onboarded to " + companyAdmin.getCompanyName() + "!\n\n" +
                    "Driver Name     : " + name + "\n" +
                    "Username        : " + user + "\n" +
                    "Login Password  : " + pass + "\n" +
                    "Vehicle Reg.    : " + vehReg + " (" + vehType + ")\n\n" +
                    "The driver can now log into the Driver Portal using these credentials.",
                    "Driver Added Successfully", JOptionPane.INFORMATION_MESSAGE);

            if (onDriverAddedCallback != null) {
                onDriverAddedCallback.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to register driver. Username or vehicle plate may already exist.", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignExistingDriver() {
        Driver d = (Driver) existingDriverCombo.getSelectedItem();
        if (d == null) return;

        boolean ok = driverDAO.assignDriverToCompany(d.getId(), companyAdmin.getCompanyId());

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Driver " + d.getDriverName() + " has been assigned to " + companyAdmin.getCompanyName() + "!\n" +
                    "They will now appear in your active company driver roster.",
                    "Driver Assigned", JOptionPane.INFORMATION_MESSAGE);

            if (onDriverAddedCallback != null) {
                onDriverAddedCallback.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to assign driver.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
