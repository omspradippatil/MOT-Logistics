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
 * VehiclePanel.java - Manage Vehicles
 *
 * Admin uses this panel to:
 * - View all vehicles
 * - Add new vehicles
 * - Assign vehicles to drivers
 * - Update or remove vehicles
 */
public class VehiclePanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;

    private JTextField  regNoField;
    private JComboBox<String>  typeCombo;
    private JComboBox<Driver>  driverCombo;
    private JComboBox<String>  statusCombo;

    private JButton addBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton clearBtn;

    private VehicleDAO vehicleDAO = new VehicleDAO();
    private DriverDAO  driverDAO  = new DriverDAO();
    private int selectedVehicleId = -1;

    // Vehicle type options
    private static final String[] VEHICLE_TYPES   = {"Tempo", "Pickup", "Truck", "Trailer", "Other"};
    private static final String[] STATUS_OPTIONS   = {"Active", "Inactive", "Under Repair"};

    public VehiclePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        initUI();
        loadVehicles();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Details"));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        regNoField  = new JTextField(15);
        typeCombo   = new JComboBox<>(VEHICLE_TYPES);
        driverCombo = new JComboBox<>();
        statusCombo = new JComboBox<>(STATUS_OPTIONS);

        // Load drivers into combo box
        List<Driver> drivers = driverDAO.getActiveDrivers();
        for (Driver d : drivers) {
            driverCombo.addItem(d);
        }

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Registration Number *"), gbc);
        gbc.gridx = 1;                formPanel.add(regNoField, gbc);
        gbc.gridx = 2;                formPanel.add(new JLabel("Vehicle Type *"), gbc);
        gbc.gridx = 3;                formPanel.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Assign Driver"), gbc);
        gbc.gridx = 1;                formPanel.add(driverCombo, gbc);
        gbc.gridx = 2;                formPanel.add(new JLabel("Status"), gbc);
        gbc.gridx = 3;                formPanel.add(statusCombo, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);
        addBtn    = createButton("Add",    new Color(0, 130, 70));
        updateBtn = createButton("Update", new Color(0, 100, 180));
        deleteBtn = createButton("Delete", new Color(200, 50, 50));
        clearBtn  = createButton("Clear",  Color.DARK_GRAY);
        btnPanel.add(addBtn); btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn); btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);

        // Table
        String[] cols = {"ID", "Registration No.", "Type", "Driver", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(0, 70, 130));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fillFormFromRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All Vehicles"));

        addBtn.addActionListener(e    -> addVehicle());
        updateBtn.addActionListener(e -> updateVehicle());
        deleteBtn.addActionListener(e -> deleteVehicle());
        clearBtn.addActionListener(e  -> clearForm());

        add(formPanel,  BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadVehicles() {
        tableModel.setRowCount(0);
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
            JOptionPane.showMessageDialog(this, "Registration Number is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehicle v = new Vehicle();
        v.setRegistrationNumber(regNoField.getText().trim().toUpperCase());
        v.setVehicleType((String) typeCombo.getSelectedItem());
        v.setStatus((String) statusCombo.getSelectedItem());

        Driver selected = (Driver) driverCombo.getSelectedItem();
        if (selected != null) v.setDriverId(selected.getId());

        if (vehicleDAO.addVehicle(v)) {
            JOptionPane.showMessageDialog(this, "Vehicle added successfully!");
            clearForm();
            loadVehicles();
        } else {
            JOptionPane.showMessageDialog(this, "Failed! Registration number may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateVehicle() {
        if (selectedVehicleId == -1) {
            JOptionPane.showMessageDialog(this, "Select a vehicle first!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Vehicle v = new Vehicle();
        v.setId(selectedVehicleId);
        v.setRegistrationNumber(regNoField.getText().trim().toUpperCase());
        v.setVehicleType((String) typeCombo.getSelectedItem());
        v.setStatus((String) statusCombo.getSelectedItem());

        Driver selected = (Driver) driverCombo.getSelectedItem();
        if (selected != null) v.setDriverId(selected.getId());

        if (vehicleDAO.updateVehicle(v)) {
            JOptionPane.showMessageDialog(this, "Vehicle updated!");
            clearForm();
            loadVehicles();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteVehicle() {
        if (selectedVehicleId == -1) {
            JOptionPane.showMessageDialog(this, "Select a vehicle first!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this vehicle?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (vehicleDAO.deleteVehicle(selectedVehicleId)) {
                JOptionPane.showMessageDialog(this, "Vehicle deleted!");
                clearForm();
                loadVehicles();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete - vehicle may have trips linked!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void fillFormFromRow() {
        int row = table.getSelectedRow();
        selectedVehicleId = (int) tableModel.getValueAt(row, 0);
        regNoField.setText((String) tableModel.getValueAt(row, 1));
        typeCombo.setSelectedItem(tableModel.getValueAt(row, 2));
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 4));
    }

    private void clearForm() {
        selectedVehicleId = -1;
        regNoField.setText("");
        typeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        if (driverCombo.getItemCount() > 0) driverCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }
}
