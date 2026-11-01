package com.transport.ui;

import com.transport.dao.CompanyDAO;
import com.transport.dao.TripDAO;
import com.transport.dao.VehicleDAO;
import com.transport.model.Company;
import com.transport.model.Driver;
import com.transport.model.Trip;
import com.transport.model.Vehicle;
import com.transport.util.Validation;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * TripEntryPanel.java - Real-Time Daily Trip Logging
 *
 * Designed with high visibility, large prominent action buttons,
 * and intuitive auto-filling for quick entry by transport drivers.
 */
public class TripEntryPanel extends JPanel {

    private JTextField         tripCodeField;
    private JSpinner           dateSpinner;
    private JComboBox<Company> companyCombo;
    private JTextField         driverNameField;
    private JComboBox<Vehicle> vehicleCombo;
    private JTextField         sourceField;
    private JTextField         destinationField;
    private JTextField         amountField;
    private JTextField         remarksField;

    private ModernButton submitBtn;
    private ModernButton clearBtn;

    private Driver     loggedInDriver;
    private TripDAO    tripDAO    = new TripDAO();
    private CompanyDAO companyDAO = new CompanyDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();

    public TripEntryPanel(Driver driver) {
        this.loggedInDriver = driver;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setBackground(UITheme.BG_LIGHT);
        initUI();
    }

    private void initUI() {
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(UITheme.createCardBorder("Enter Trip Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(10, 15, 10, 15);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        tripCodeField = new JTextField(tripDAO.generateTripCode());
        tripCodeField.setEditable(false);
        tripCodeField.setBackground(new Color(241, 245, 249));
        tripCodeField.setFont(UITheme.FONT_BODY_BOLD);
        tripCodeField.setPreferredSize(new Dimension(220, 32));

        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setFont(UITheme.FONT_BODY);
        dateSpinner.setPreferredSize(new Dimension(220, 32));

        companyCombo = new JComboBox<>();
        companyCombo.setFont(UITheme.FONT_BODY);
        companyCombo.setPreferredSize(new Dimension(220, 32));
        List<Company> companies = companyDAO.getAllCompanies();
        for (Company c : companies) companyCombo.addItem(c);

        driverNameField = new JTextField(loggedInDriver.getDriverName());
        driverNameField.setEditable(false);
        driverNameField.setBackground(new Color(241, 245, 249));
        driverNameField.setFont(UITheme.FONT_BODY_BOLD);
        driverNameField.setPreferredSize(new Dimension(220, 32));

        vehicleCombo = new JComboBox<>();
        vehicleCombo.setFont(UITheme.FONT_BODY);
        vehicleCombo.setPreferredSize(new Dimension(220, 32));
        List<Vehicle> vehicles = vehicleDAO.getVehiclesByDriver(loggedInDriver.getId());
        for (Vehicle v : vehicles) vehicleCombo.addItem(v);

        sourceField      = new JTextField(20);
        destinationField = new JTextField(20);
        amountField      = new JTextField(20);
        remarksField     = new JTextField(20);

        styleInputField(sourceField);
        styleInputField(destinationField);
        styleInputField(amountField);
        styleInputField(remarksField);

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Trip Reference ID:", tripCodeField);
        addFormRow(formPanel, gbc, row++, "Trip Date (dd/mm/yyyy): *", dateSpinner);
        addFormRow(formPanel, gbc, row++, "Client Company: *", companyCombo);
        addFormRow(formPanel, gbc, row++, "Assigned Driver:", driverNameField);
        addFormRow(formPanel, gbc, row++, "Vehicle Registration: *", vehicleCombo);
        addFormRow(formPanel, gbc, row++, "Source Location (From): *", sourceField);
        addFormRow(formPanel, gbc, row++, "Destination (To): *", destinationField);
        addFormRow(formPanel, gbc, row++, "Trip Amount (₹): *", amountField);
        addFormRow(formPanel, gbc, row++, "Remarks / Notes:", remarksField);

        // Prominent Action Buttons Bar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        submitBtn = new ModernButton("✓  Submit Trip Entry", UITheme.SUCCESS_GREEN);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        submitBtn.setPreferredSize(new Dimension(220, 44));

        clearBtn = new ModernButton("✕  Clear Fields", UITheme.SECONDARY_GRAY);
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearBtn.setPreferredSize(new Dimension(140, 44));

        submitBtn.addActionListener(e -> submitTrip());
        clearBtn.addActionListener(e  -> clearForm());

        btnPanel.add(submitBtn);
        btnPanel.add(clearBtn);

        // Centered scrollable wrapper
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(formPanel);

        add(new JScrollPane(wrapper), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void styleInputField(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setPreferredSize(new Dimension(220, 32));
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component field) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void submitTrip() {
        if (sourceField.getText().trim().isEmpty() || destinationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Source and Destination locations are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (companyCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a client company!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (vehicleCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "No vehicle assigned to you! Please contact admin to assign a vehicle.", "Vehicle Required", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Validation.isValidAmount(amountField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive trip amount (numbers only)!", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount = Double.parseDouble(amountField.getText().trim());

        Trip trip = new Trip();
        trip.setTripCode(tripCodeField.getText());
        trip.setTripDate((Date) dateSpinner.getValue());
        trip.setDriverId(loggedInDriver.getId());
        trip.setDriverName(loggedInDriver.getDriverName());

        Company selectedCompany = (Company) companyCombo.getSelectedItem();
        trip.setCompanyId(selectedCompany.getId());

        Vehicle selectedVehicle = (Vehicle) vehicleCombo.getSelectedItem();
        trip.setVehicleId(selectedVehicle.getId());

        trip.setSourceLocation(sourceField.getText().trim());
        trip.setDestination(destinationField.getText().trim());
        trip.setAmount(amount);
        trip.setRemarks(remarksField.getText().trim());

        if (tripDAO.addTrip(trip)) {
            JOptionPane.showMessageDialog(this,
                "Trip recorded successfully!\n\n" +
                "Trip Reference : " + trip.getTripCode() + "\n" +
                "Company        : " + selectedCompany.getCompanyName() + "\n" +
                "Vehicle        : " + selectedVehicle.getRegistrationNumber() + "\n" +
                "Amount         : ₹ " + String.format("%.2f", amount),
                "Trip Logged Successfully", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Database error: Failed to persist trip entry!", "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        tripCodeField.setText(tripDAO.generateTripCode());
        dateSpinner.setValue(new Date());
        if (companyCombo.getItemCount() > 0) companyCombo.setSelectedIndex(0);
        if (vehicleCombo.getItemCount() > 0) vehicleCombo.setSelectedIndex(0);
        sourceField.setText("");
        destinationField.setText("");
        amountField.setText("");
        remarksField.setText("");
    }
}
