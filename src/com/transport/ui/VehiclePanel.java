package com.transport.ui;

import com.transport.dao.DriverDAO;
import com.transport.dao.VehicleDAO;
import com.transport.model.Driver;
import com.transport.model.Vehicle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * VehiclePanel.java - Commercial Fleet Directory & Driver Assignments
 */
public class VehiclePanel extends JPanel implements Refreshable {

    private JTable            table;
    private DefaultTableModel tableModel;

    private JTextField         regNoField;
    private JComboBox<String>  typeCombo;
    private JComboBox<Driver>  driverCombo;
    private JComboBox<String>  statusCombo;

    private ModernButton addBtn;
    private ModernButton updateBtn;
    private ModernButton deleteBtn;
    private ModernButton clearBtn;

    private VehicleDAO vehicleDAO = new VehicleDAO();
    private DriverDAO  driverDAO  = new DriverDAO();
    private int selectedVehicleId = -1;

    private static final String[] VEHICLE_TYPES  = {"Tempo", "Pickup Van", "Light Commercial Truck", "Heavy Truck", "Trailer", "Other"};
    private static final String[] STATUS_OPTIONS = {"Active", "Under Maintenance", "Inactive"};

    public VehiclePanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadVehicles();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(UITheme.createCardBorder("Vehicle Registration & Assignment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        regNoField  = new JTextField(15);
        regNoField.setFont(UITheme.FONT_BODY);
        regNoField.setPreferredSize(new Dimension(170, 32));

        typeCombo   = new JComboBox<>(VEHICLE_TYPES);
        typeCombo.setFont(UITheme.FONT_BODY);
        typeCombo.setPreferredSize(new Dimension(170, 32));

        driverCombo = new JComboBox<>();
        driverCombo.setFont(UITheme.FONT_BODY);
        driverCombo.setPreferredSize(new Dimension(170, 32));

        statusCombo = new JComboBox<>(STATUS_OPTIONS);
        statusCombo.setFont(UITheme.FONT_BODY);
        statusCombo.setPreferredSize(new Dimension(170, 32));

        refreshDriverCombo();

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Registration Number: *"), gbc);
        gbc.gridx = 1;                formPanel.add(regNoField, gbc);
        gbc.gridx = 2;                formPanel.add(new JLabel("Vehicle Type: *"), gbc);
        gbc.gridx = 3;                formPanel.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Assign Driver:"), gbc);
        gbc.gridx = 1;                formPanel.add(driverCombo, gbc);
        gbc.gridx = 2;                formPanel.add(new JLabel("Vehicle Status:"), gbc);
        gbc.gridx = 3;                formPanel.add(statusCombo, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnPanel.setOpaque(false);
        addBtn    = new ModernButton("＋ Add Vehicle", UITheme.SUCCESS_GREEN);
        updateBtn = new ModernButton("✎ Update", UITheme.UPDATE_BLUE);
        deleteBtn = new ModernButton("🗑 Delete", UITheme.DANGER_RED);
        clearBtn  = new ModernButton("✕ Clear", UITheme.SECONDARY_GRAY);

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);

        String[] cols = {"ID", "Registration No.", "Vehicle Type", "Assigned Driver", "Status"};
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
        scrollPane.setBorder(UITheme.createCardBorder("Fleet Inventory Roster"));

        addBtn.addActionListener(e    -> addVehicle());
        updateBtn.addActionListener(e -> updateVehicle());
        deleteBtn.addActionListener(e -> deleteVehicle());
        clearBtn.addActionListener(e  -> clearForm());

        add(formPanel,  BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshDriverCombo() {
        driverCombo.removeAllItems();
        Driver unassigned = new Driver();
        unassigned.setId(-1);
        unassigned.setDriverName("-- Unassigned --");
        driverCombo.addItem(unassigned);

        List<Driver> drivers = driverDAO.getActiveDrivers();
        for (Driver d : drivers) {
            driverCombo.addItem(d);
        }
    }

    @Override
    public void refreshData() {
        loadVehicles();
    }

    private void loadVehicles() {
        tableModel.setRowCount(0);
        refreshDriverCombo();
        for (Vehicle v : vehicleDAO.getAllVehicles()) {
            tableModel.addRow(new Object[]{
                v.getId(), v.getRegistrationNumber(), v.getVehicleType(),
                v.getDriverName() != null ? v.getDriverName() : "Unassigned",
                v.getStatus()
            });
        }
    }

    private void addVehicle() {
        if (regNoField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle Registration Number is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vehicle v = new Vehicle();
        v.setRegistrationNumber(regNoField.getText().trim().toUpperCase());
        v.setVehicleType((String) typeCombo.getSelectedItem());
        v.setStatus((String) statusCombo.getSelectedItem());

        Driver selected = (Driver) driverCombo.getSelectedItem();
        if (selected != null && selected.getId() > 0) {
            v.setDriverId(selected.getId());
        }

        if (vehicleDAO.addVehicle(v)) {
            JOptionPane.showMessageDialog(this, "Vehicle registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadVehicles();
        } else {
            JOptionPane.showMessageDialog(this, "Failed! The registration number may already exist.", "Duplicate Registration", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateVehicle() {
        if (selectedVehicleId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle row from the table first!", "Select Vehicle", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Vehicle v = new Vehicle();
        v.setId(selectedVehicleId);
        v.setRegistrationNumber(regNoField.getText().trim().toUpperCase());
        v.setVehicleType((String) typeCombo.getSelectedItem());
        v.setStatus((String) statusCombo.getSelectedItem());

        Driver selected = (Driver) driverCombo.getSelectedItem();
        if (selected != null && selected.getId() > 0) {
            v.setDriverId(selected.getId());
        }

        if (vehicleDAO.updateVehicle(v)) {
            JOptionPane.showMessageDialog(this, "Vehicle details updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadVehicles();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteVehicle() {
        if (selectedVehicleId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to delete!", "Select Vehicle", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this vehicle?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (vehicleDAO.deleteVehicle(selectedVehicleId)) {
                JOptionPane.showMessageDialog(this, "Vehicle deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadVehicles();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete vehicle! Daily trips are currently linked to this vehicle.", "Integrity Constraint", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void fillFormFromRow() {
        int row = table.getSelectedRow();
        selectedVehicleId = (int) tableModel.getValueAt(row, 0);
        regNoField.setText((String) tableModel.getValueAt(row, 1));
        typeCombo.setSelectedItem(tableModel.getValueAt(row, 2));
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 4));

        String driverName = (String) tableModel.getValueAt(row, 3);
        for (int i = 0; i < driverCombo.getItemCount(); i++) {
            Driver d = driverCombo.getItemAt(i);
            if (d.getDriverName().equals(driverName)) {
                driverCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void clearForm() {
        selectedVehicleId = -1;
        regNoField.setText("");
        typeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        if (driverCombo.getItemCount() > 0) driverCombo.setSelectedIndex(0);
        table.clearSelection();
    }
}
