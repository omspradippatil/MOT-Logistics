package com.transport.ui;

import com.transport.dao.AdminDAO;
import com.transport.model.Admin;
import com.transport.util.Validation;

import javax.swing.*;
import java.awt.*;

/**
 * CompanyProfilePanel.java - Manage Company Profile & Account Settings
 */
public class CompanyProfilePanel extends JPanel implements Refreshable {

    private Admin companyAdmin;

    private JTextField companyNameField;
    private JTextField adminNameField;
    private JTextField emailField;
    private JTextField usernameField;
    private JTextField contactField;
    private JTextField addressField;
    private JTextField gstField;

    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private AdminDAO adminDAO = new AdminDAO();

    public CompanyProfilePanel(Admin companyAdmin) {
        this.companyAdmin = companyAdmin;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        populateFields();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(UITheme.createCardBorder("Company Admin Profile & Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        companyNameField = new JTextField(20);
        adminNameField   = new JTextField(20);
        emailField       = new JTextField(20);
        usernameField    = new JTextField(20);
        usernameField.setEditable(false);
        usernameField.setBackground(new Color(241, 245, 249));

        contactField     = new JTextField(20);
        addressField     = new JTextField(20);
        gstField         = new JTextField(20);

        newPasswordField     = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        int row = 0;
        addRow(formPanel, gbc, row++, "Company Name: *", companyNameField);
        addRow(formPanel, gbc, row++, "Admin Contact Person: *", adminNameField);
        addRow(formPanel, gbc, row++, "Official Email Address: *", emailField);
        addRow(formPanel, gbc, row++, "Login Username (Fixed):", usernameField);
        addRow(formPanel, gbc, row++, "Contact Mobile Number:", contactField);
        addRow(formPanel, gbc, row++, "Registered Office Address:", addressField);
        addRow(formPanel, gbc, row++, "GST Number:", gstField);

        // Divider
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        formPanel.add(sep, gbc);

        gbc.gridwidth = 1;
        addRow(formPanel, gbc, row++, "New Password (Optional):", newPasswordField);
        addRow(formPanel, gbc, row++, "Confirm New Password:", confirmPasswordField);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        btnPanel.setOpaque(false);

        ModernButton saveBtn = new ModernButton("✓  Save Profile Changes", UITheme.SUCCESS_GREEN);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setPreferredSize(new Dimension(210, 40));
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
        Admin a = adminDAO.getCompanyById(companyAdmin.getCompanyId());
        if (a != null) {
            this.companyAdmin = a;
        }

        companyNameField.setText(companyAdmin.getCompanyName());
        adminNameField.setText(companyAdmin.getAdminName());
        emailField.setText(companyAdmin.getEmail());
        usernameField.setText(companyAdmin.getUsername());
        contactField.setText(companyAdmin.getContactNumber() != null ? companyAdmin.getContactNumber() : "");
        addressField.setText(companyAdmin.getAddress() != null ? companyAdmin.getAddress() : "");
        gstField.setText(companyAdmin.getGstNumber() != null ? companyAdmin.getGstNumber() : "");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void saveProfile() {
        String companyName = companyNameField.getText().trim();
        String adminName   = adminNameField.getText().trim();
        String email       = emailField.getText().trim();

        if (companyName.isEmpty() || adminName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Company Name, Admin Name, and Email are mandatory fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validation.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        companyAdmin.setCompanyName(companyName);
        companyAdmin.setAdminName(adminName);
        companyAdmin.setEmail(email);
        companyAdmin.setContactNumber(contactField.getText().trim());
        companyAdmin.setAddress(addressField.getText().trim());
        companyAdmin.setGstNumber(gstField.getText().trim());

        boolean ok = adminDAO.updateCompanyProfile(companyAdmin);

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
            adminDAO.updatePassword(companyAdmin.getCompanyId(), newPass);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Company profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            populateFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile. Email or company name might already be in use.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refreshData() {
        populateFields();
    }
}
