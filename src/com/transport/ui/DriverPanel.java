package com.transport.ui;

import com.transport.dao.DriverDAO;
import com.transport.model.Driver;
import com.transport.util.Validation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * DriverPanel.java - Manage Drivers and Access Credentials
 */
public class DriverPanel extends JPanel implements Refreshable {

    private JTable            table;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField mobileField;
    private JTextField licenceField;
    private JTextField usernameField;
    private JTextField passwordField;
    private JCheckBox  activeCheckBox;

    private ModernButton addBtn;
    private ModernButton updateBtn;
    private ModernButton clearBtn;

    private DriverDAO driverDAO = new DriverDAO();
    private int selectedDriverId = -1;

    public DriverPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadDrivers();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(UITheme.createCardBorder("Driver Information & Login Credentials"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        nameField     = new JTextField(15);
        mobileField   = new JTextField(15);
        licenceField  = new JTextField(15);
        usernameField = new JTextField(15);
        passwordField = new JTextField(15);
        activeCheckBox = new JCheckBox("Active Account (Allowed to Log In)", true);
        activeCheckBox.setFont(UITheme.FONT_BODY_BOLD);
        activeCheckBox.setOpaque(false);

        styleField(nameField);
        styleField(mobileField);
        styleField(licenceField);
        styleField(usernameField);
        styleField(passwordField);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Full Name: *"), gbc);
        gbc.gridx = 1;                formPanel.add(nameField, gbc);
        gbc.gridx = 2;                formPanel.add(new JLabel("Mobile Number:"), gbc);
        gbc.gridx = 3;                formPanel.add(mobileField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Licence Number:"), gbc);
        gbc.gridx = 1;                formPanel.add(licenceField, gbc);
        gbc.gridx = 2;                formPanel.add(new JLabel("Login Username: *"), gbc);
        gbc.gridx = 3;                formPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Login Password: *"), gbc);
        gbc.gridx = 1;                formPanel.add(passwordField, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2; formPanel.add(activeCheckBox, gbc);
        gbc.gridwidth = 1;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnPanel.setOpaque(false);
        addBtn    = new ModernButton("＋ Add Driver", UITheme.SUCCESS_GREEN);
        updateBtn = new ModernButton("✎ Update Driver", UITheme.UPDATE_BLUE);
        clearBtn  = new ModernButton("✕ Clear", UITheme.SECONDARY_GRAY);
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);

        String[] cols = {"ID", "Driver Name", "Mobile Number", "Licence No.", "Username", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fillFormFromRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Registered Driver Roster"));

        addBtn.addActionListener(e    -> addDriver());
        updateBtn.addActionListener(e -> updateDriver());
        clearBtn.addActionListener(e  -> clearForm());

        add(formPanel,  BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleField(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setPreferredSize(new Dimension(170, 32));
    }

    @Override
    public void refreshData() {
        loadDrivers();
    }

    private void loadDrivers() {
        tableModel.setRowCount(0);
        for (Driver d : driverDAO.getAllDrivers()) {
            tableModel.addRow(new Object[]{
                d.getId(), d.getDriverName(), d.getMobileNumber(),
                d.getLicenceNumber(), d.getUsername(),
                d.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void addDriver() {
        if (nameField.getText().trim().isEmpty() || usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Username, and Password are required fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Driver d = new Driver();
        d.setDriverName(nameField.getText().trim());
        d.setMobileNumber(mobileField.getText().trim());
        d.setLicenceNumber(licenceField.getText().trim());
        d.setUsername(usernameField.getText().trim());
        d.setPassword(passwordField.getText().trim());
        d.setActive(activeCheckBox.isSelected());

        if (driverDAO.addDriver(d)) {
            JOptionPane.showMessageDialog(this, "Driver profile registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadDrivers();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to register driver! The username may already exist.", "Duplicate Username", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDriver() {
        if (selectedDriverId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a driver from the roster table first!", "Select Driver", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Driver d = new Driver();
        d.setId(selectedDriverId);
        d.setDriverName(nameField.getText().trim());
        d.setMobileNumber(mobileField.getText().trim());
        d.setLicenceNumber(licenceField.getText().trim());
        d.setUsername(usernameField.getText().trim());
        d.setPassword(passwordField.getText().trim());

        if (driverDAO.updateDriver(d)) {
            driverDAO.setDriverActive(selectedDriverId, activeCheckBox.isSelected());
            JOptionPane.showMessageDialog(this, "Driver details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadDrivers();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update driver details!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFormFromRow() {
        int row = table.getSelectedRow();
        selectedDriverId = (int) tableModel.getValueAt(row, 0);
        nameField.setText((String) tableModel.getValueAt(row, 1));
        mobileField.setText((String) tableModel.getValueAt(row, 2));
        licenceField.setText((String) tableModel.getValueAt(row, 3));
        usernameField.setText((String) tableModel.getValueAt(row, 4));
        passwordField.setText("");
        activeCheckBox.setSelected("Active".equals(tableModel.getValueAt(row, 5)));
    }

    private void clearForm() {
        selectedDriverId = -1;
        nameField.setText(""); mobileField.setText(""); licenceField.setText("");
        usernameField.setText(""); passwordField.setText("");
        activeCheckBox.setSelected(true);
        table.clearSelection();
    }
}
