package com.transport.ui;

import com.transport.dao.VehicleDAO;
import com.transport.model.Driver;
import com.transport.model.Vehicle;
import com.transport.util.Validation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * DriverVehiclesPanel.java - Manage Driver's Commercial Vehicles
 *
 * Implements Module 4 for Drivers:
 * - View all vehicles owned or operated by the driver
 * - Register and assign new commercial vehicles to their fleet profile
 */
public class DriverVehiclesPanel extends JPanel implements Refreshable {

    private Driver driver;
    private JTable table;
    private DefaultTableModel tableModel;

    private VehicleDAO vehicleDAO = new VehicleDAO();
    private List<Vehicle> vehicles;

    public DriverVehiclesPanel(Driver driver) {
        this.driver = driver;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadVehicles();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.CARD_BG);
        headerPanel.setBorder(UITheme.createCardBorder("My Commercial Vehicles Fleet"));

        JLabel descLabel = new JLabel("Commercial vehicles registered to " + driver.getDriverName() + ". Drivers can own and operate multiple vehicles.");
        descLabel.setFont(UITheme.FONT_BODY);
        descLabel.setForeground(UITheme.TEXT_MUTED);
        descLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        headerPanel.add(descLabel, BorderLayout.CENTER);

        // Table
        String[] cols = {"Vehicle ID", "Registration Number", "Vehicle Type", "Operator / Owner", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Registered Vehicles"));

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        ModernButton addVehBtn = new ModernButton("➕ Register New Vehicle", UITheme.SUCCESS_GREEN);
        addVehBtn.setPreferredSize(new Dimension(210, 36));
        addVehBtn.addActionListener(e -> showAddVehicleDialog());

        ModernButton refreshBtn = new ModernButton("↺ Refresh", UITheme.SECONDARY_GRAY);
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.addActionListener(e -> loadVehicles());

        bottomBar.add(addVehBtn);
        bottomBar.add(refreshBtn);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(bottomBar,   BorderLayout.SOUTH);
    }

    public void loadVehicles() {
        tableModel.setRowCount(0);
        vehicles = vehicleDAO.getVehiclesByDriver(driver.getId());

        for (Vehicle v : vehicles) {
            tableModel.addRow(new Object[]{
                v.getId(),
                v.getRegistrationNumber(),
                v.getVehicleType(),
                driver.getDriverName(),
                v.getStatus()
            });
        }
    }

    private void showAddVehicleDialog() {
        JTextField regField = new JTextField(15);
        String[] types = {"Tempo", "Pickup Van", "Truck", "Heavy Truck", "Trailer", "Container Truck"};
        JComboBox<String> typeCombo = new JComboBox<>(types);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Registration Number: *"));
        form.add(regField);
        form.add(new JLabel("Vehicle Type: *"));
        form.add(typeCombo);

        int res = JOptionPane.showConfirmDialog(this, form, "Register Commercial Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String reg = regField.getText().trim().toUpperCase();
            if (reg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vehicle registration number cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!Validation.isValidVehicleNumber(reg)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid vehicle registration number (e.g. MH-48-AB-1234).", "Invalid Format", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Vehicle v = new Vehicle();
            v.setRegistrationNumber(reg);
            v.setVehicleType((String) typeCombo.getSelectedItem());
            v.setDriverId(driver.getId());
            v.setStatus("Active");

            if (vehicleDAO.addVehicle(v)) {
                JOptionPane.showMessageDialog(this, "Vehicle " + reg + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadVehicles();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register vehicle. Registration number might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refreshData() {
        loadVehicles();
    }
}
