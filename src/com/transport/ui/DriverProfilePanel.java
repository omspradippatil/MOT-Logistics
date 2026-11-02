package com.transport.ui;

import com.transport.dao.DriverDAO;
import com.transport.model.Driver;
import com.transport.util.Validation;

import javax.swing.*;
import java.awt.*;

/**
 * DriverProfilePanel.java - Manage Driver Profile & Credentials
 */
public class DriverProfilePanel extends JPanel implements Refreshable {

    private Driver driver;

    private JTextField driverNameField;
    private JTextField licenceField;
    private JTextField usernameField;
    private JTextField mobileField;
    private JTextField emailField;

    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private DriverDAO driverDAO = new DriverDAO();

    public DriverProfilePanel(Driver driver) {
        this.driver = driver;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        populateFields();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(UITheme.createCardBorder("My Driver Account & Profile Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        driverNameField = new JTextField(20);
        driverNameField.setEditable(false);
        driverNameField.setBackground(new Color(241, 245, 249));

        licenceField = new JTextField(20);
        licenceField.setEditable(false);
        licenceField.setBackground(new Color(241, 245, 249));

        usernameField = new JTextField(20);
        usernameField.setEditable(false);
        usernameField.setBackground(new Color(241, 245, 249));

        mobileField = new JTextField(20);
        emailField  = new JTextField(20);

        newPasswordField     = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        int row = 0;
        addRow(formPanel, gbc, row++, "Full Driver Name (Registered):", driverNameField);
        addRow(formPanel, gbc, row++, "Driving Licence Number:", licenceField);
        addRow(formPanel, gbc, row++, "Login Username:", usernameField);
        addRow(formPanel, gbc, row++, "Mobile Contact Number: *", mobileField);
        addRow(formPanel, gbc, row++, "Personal Email Address: *", emailField);

        // Divider
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        formPanel.add(sep, gbc);

        gbc.gridwidth = 1;
        addRow(formPanel, gbc, row++, "New Password (Optional):", newPasswordField);
        addRow(formPanel, gbc, row++, "Confirm New Password:", confirmPasswordField);

        // Save Button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        btnPanel.setOpaque(false);

        ModernButton saveBtn = new ModernButton("✓  Update Profile & Password", UITheme.SUCCESS_GREEN);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setPreferredSize(new Dimension(240, 40));
        saveBtn.addActionListener(e -> saveProfile());

        btnPanel.add(saveBtn);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(formPanel);

        add(new JScrollPane(wrapper), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setFont(UITheme.FONT_BODY);
        field.setPreferredSize(new Dimension(240, 32));
        panel.add(field, gbc);
    }

    private void populateFields() {
        Driver d = driverDAO.getDriverById(driver.getId());
        if (d != null) this.driver = d;

        driverNameField.setText(driver.getDriverName());
        licenceField.setText(driver.getLicenceNumber());
        usernameField.setText(driver.getUsername());
        mobileField.setText(driver.getMobileNumber());
        emailField.setText(driver.getEmail() != null ? driver.getEmail() : "");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void saveProfile() {
        String mobile = mobileField.getText().trim();
        String email  = emailField.getText().trim();

        if (mobile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mobile number cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validation.isValidMobile(mobile)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit mobile number.", "Invalid Mobile", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.isEmpty() && !Validation.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newPass = new String(newPasswordField.getPassword()).trim();
        String confPass = new String(confirmPasswordField.getPassword()).trim();

        if (!newPass.isEmpty()) {
            if (!newPass.equals(confPass)) {
                JOptionPane.showMessageDialog(this, "New password and Confirm password do not match!", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Short Password", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        boolean ok = driverDAO.updateProfile(driver.getId(), mobile, email, newPass.isEmpty() ? null : newPass);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Driver profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            populateFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile. Email might already be taken.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refreshData() {
        populateFields();
    }
}
