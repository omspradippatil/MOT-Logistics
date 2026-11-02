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
 * TripEntryPanel.java - Real-Time Daily Trip Logging for Drivers
 *
 * Implements Module 5:
 * 1. Automatically generated Trip ID
 * 2. Trip date
 * 3. Company name (dropdown)
 * 4. Driver name (fixed to logged-in driver)
 * 5. Vehicle number (dropdown of driver's vehicles)
 * 6. Source location
 * 7. Destination location
 * 8. Cargo / Container / Goods description (optional)
 * 9. Trip amount
 * 10. Remarks (optional)
 */
public class TripEntryPanel extends JPanel implements Refreshable {

    private JTextField         tripCodeField;
    private JSpinner           dateSpinner;
    private JComboBox<Company> companyCombo;
    private JTextField         driverNameField;
    private JComboBox<Vehicle> vehicleCombo;
    private JTextField         sourceField;
    private JTextField         destinationField;
    private JTextField         goodsDescField;
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
        setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        setBackground(UITheme.BG_LIGHT);

        initUI();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(UITheme.createCardBorder("Log New Daily Transport Trip"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        tripCodeField = new JTextField(tripDAO.generateTripCode());
        tripCodeField.setEditable(false);
        tripCodeField.setBackground(new Color(241, 245, 249));
        tripCodeField.setFont(UITheme.FONT_BODY_BOLD);
        tripCodeField.setPreferredSize(new Dimension(240, 32));

        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setFont(UITheme.FONT_BODY);
        dateSpinner.setPreferredSize(new Dimension(240, 32));

        companyCombo = new JComboBox<>();
        companyCombo.setFont(UITheme.FONT_BODY);
        companyCombo.setPreferredSize(new Dimension(240, 32));
        loadCompanies();

        driverNameField = new JTextField(loggedInDriver.getDriverName());
        driverNameField.setEditable(false);
        driverNameField.setBackground(new Color(241, 245, 249));
        driverNameField.setFont(UITheme.FONT_BODY_BOLD);
        driverNameField.setPreferredSize(new Dimension(240, 32));

        vehicleCombo = new JComboBox<>();
        vehicleCombo.setFont(UITheme.FONT_BODY);
        vehicleCombo.setPreferredSize(new Dimension(240, 32));
        loadVehicles();

        sourceField      = new JTextField(20);
        destinationField = new JTextField(20);
        goodsDescField   = new JTextField(20);
        amountField      = new JTextField(20);
        remarksField     = new JTextField(20);

        styleInputField(sourceField);
        styleInputField(destinationField);
        styleInputField(goodsDescField);
        styleInputField(amountField);
        styleInputField(remarksField);

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Trip Reference ID:", tripCodeField);
        addFormRow(formPanel, gbc, row++, "Trip Date (dd/mm/yyyy): *", dateSpinner);
        addFormRow(formPanel, gbc, row++, "Select Client Company: *", companyCombo);
        addFormRow(formPanel, gbc, row++, "Driver In-Charge:", driverNameField);
        addFormRow(formPanel, gbc, row++, "Assigned Vehicle Reg.: *", vehicleCombo);
        addFormRow(formPanel, gbc, row++, "Source Location (From): *", sourceField);
        addFormRow(formPanel, gbc, row++, "Destination (To): *", destinationField);
        addFormRow(formPanel, gbc, row++, "Goods / Container Description:", goodsDescField);
        addFormRow(formPanel, gbc, row++, "Trip Fare / Amount (₹): *", amountField);
        addFormRow(formPanel, gbc, row++, "Remarks / Operational Notes:", remarksField);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);

        submitBtn = new ModernButton("✓  Submit Trip Entry", UITheme.SUCCESS_GREEN);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        submitBtn.setPreferredSize(new Dimension(220, 42));

        clearBtn = new ModernButton("✕  Clear Fields", UITheme.SECONDARY_GRAY);
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearBtn.setPreferredSize(new Dimension(140, 42));

        submitBtn.addActionListener(e -> submitTrip());
        clearBtn.addActionListener(e  -> clearForm());

        btnPanel.add(submitBtn);
        btnPanel.add(clearBtn);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(formPanel);

        add(new JScrollPane(wrapper), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void styleInputField(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setPreferredSize(new Dimension(240, 32));
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

    private void loadCompanies() {
        companyCombo.removeAllItems();
        List<Company> list = companyDAO.getAllCompanies();
        for (Company c : list) {
            companyCombo.addItem(c);
        }
    }

    private void loadVehicles() {
        vehicleCombo.removeAllItems();
        // First look for vehicles assigned to this driver
        List<Vehicle> list = vehicleDAO.getVehiclesByDriver(loggedInDriver.getId());
        if (list.isEmpty()) {
            // Fallback to all vehicles in system
            list = vehicleDAO.getAllVehicles();
        }
        for (Vehicle v : list) {
            vehicleCombo.addItem(v);
        }
    }

    private void submitTrip() {
        if (sourceField.getText().trim().isEmpty() || destinationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Source and Destination locations are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (companyCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a client company from the dropdown!", "Company Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (vehicleCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select or register a vehicle first under 'My Vehicles'!", "Vehicle Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validation.isValidAmount(amountField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive trip amount (e.g. 2500).", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount = Double.parseDouble(amountField.getText().trim());
        Company selectedComp = (Company) companyCombo.getSelectedItem();
        Vehicle selectedVeh  = (Vehicle) vehicleCombo.getSelectedItem();

        Trip trip = new Trip();
        trip.setTripCode(tripCodeField.getText());
        trip.setTripDate((Date) dateSpinner.getValue());
        trip.setCompanyId(selectedComp.getId());
        trip.setDriverId(loggedInDriver.getId());
        trip.setVehicleId(selectedVeh.getId());
        trip.setSourceLocation(sourceField.getText().trim());
        trip.setDestination(destinationField.getText().trim());
        trip.setGoodsDescription(goodsDescField.getText().trim().isEmpty() ? "General Cargo" : goodsDescField.getText().trim());
        trip.setAmount(amount);
        trip.setTripStatus("Completed");
        trip.setRemarks(remarksField.getText().trim());

        if (tripDAO.addTrip(trip)) {
            JOptionPane.showMessageDialog(this,
                "✓ Transport Trip Recorded & Connected to Company!\n\n" +
                "Trip Reference ID  : " + trip.getTripCode() + "\n" +
                "Client Company     : " + selectedComp.getCompanyName() + " (Directly visible in Company Admin Portal)\n" +
                "Created By Driver  : " + loggedInDriver.getDriverName() + " (Mobile: " + loggedInDriver.getMobileNumber() + ")\n" +
                "Assigned Vehicle   : " + selectedVeh.getRegistrationNumber() + " (" + selectedVeh.getVehicleType() + ")\n" +
                "Operational Route  : " + trip.getSourceLocation() + " -> " + trip.getDestination() + "\n" +
                "Cargo Description  : " + trip.getGoodsDescription() + "\n" +
                "Trip Fare Amount   : ₹ " + String.format("%.2f", amount) + "\n\n" +
                "The trip is now live and linked to " + selectedComp.getCompanyName() + "'s transport ledger and monthly invoice.",
                "Trip Submitted & Connected", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Database error: Failed to record trip entry.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        tripCodeField.setText(tripDAO.generateTripCode());
        dateSpinner.setValue(new Date());
        if (companyCombo.getItemCount() > 0) companyCombo.setSelectedIndex(0);
        if (vehicleCombo.getItemCount() > 0) vehicleCombo.setSelectedIndex(0);
        sourceField.setText("");
        destinationField.setText("");
        goodsDescField.setText("");
        amountField.setText("");
        remarksField.setText("");
    }

    @Override
    public void refreshData() {
        loadCompanies();
        loadVehicles();
        if (tripCodeField.getText().trim().isEmpty()) {
            tripCodeField.setText(tripDAO.generateTripCode());
        }
    }
}
