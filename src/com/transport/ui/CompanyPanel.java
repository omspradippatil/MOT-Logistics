package com.transport.ui;

import com.transport.dao.CompanyDAO;
import com.transport.model.Company;
import com.transport.util.Validation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * CompanyPanel.java - Corporate Client Directory
 */
public class CompanyPanel extends JPanel implements Refreshable {

    private JTable            table;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField addressField;
    private JTextField contactField;
    private JTextField gstField;

    private ModernButton addBtn;
    private ModernButton updateBtn;
    private ModernButton deleteBtn;
    private ModernButton clearBtn;

    private CompanyDAO companyDAO = new CompanyDAO();
    private int selectedCompanyId = -1;

    public CompanyPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadCompanies();
    }

    private void initUI() {
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(UITheme.createCardBorder("Manage Client Companies"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        nameField    = new JTextField(18);
        addressField = new JTextField(18);
        contactField = new JTextField(18);
        gstField     = new JTextField(18);

        styleField(nameField);
        styleField(addressField);
        styleField(contactField);
        styleField(gstField);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Company Name: *"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 3; formPanel.add(addressField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Contact Number:"), gbc);
        gbc.gridx = 1; formPanel.add(contactField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("GSTIN Number:"), gbc);
        gbc.gridx = 3; formPanel.add(gstField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnPanel.setOpaque(false);

        addBtn    = new ModernButton("＋ Add Company", UITheme.SUCCESS_GREEN);
        updateBtn = new ModernButton("✎ Update", UITheme.UPDATE_BLUE);
        deleteBtn = new ModernButton("🗑 Delete", UITheme.DANGER_RED);
        clearBtn  = new ModernButton("✕ Clear", UITheme.SECONDARY_GRAY);

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);

        // Table
        String[] columns = {"ID", "Company Name", "Address", "Contact Number", "GSTIN"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fillFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Registered Company Directory"));

        addBtn.addActionListener(e    -> addCompany());
        updateBtn.addActionListener(e -> updateCompany());
        deleteBtn.addActionListener(e -> deleteCompany());
        clearBtn.addActionListener(e  -> clearForm());

        add(formPanel,  BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleField(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setPreferredSize(new Dimension(180, 32));
    }

    @Override
    public void refreshData() {
        loadCompanies();
    }

    private void loadCompanies() {
        tableModel.setRowCount(0);
        List<Company> companies = companyDAO.getAllCompanies();
        for (Company c : companies) {
            tableModel.addRow(new Object[]{
                c.getId(), c.getCompanyName(), c.getAddress(),
                c.getContactNumber(), c.getGstNumber()
            });
        }
    }

    private void addCompany() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Company Name is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Company c = new Company();
        c.setCompanyName(nameField.getText().trim());
        c.setAddress(addressField.getText().trim());
        c.setContactNumber(contactField.getText().trim());
        c.setGstNumber(gstField.getText().trim());

        if (companyDAO.addCompany(c)) {
            JOptionPane.showMessageDialog(this, "Company added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadCompanies();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add company record!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCompany() {
        if (selectedCompanyId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a company row from the table first!", "Select Company", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Company c = new Company();
        c.setId(selectedCompanyId);
        c.setCompanyName(nameField.getText().trim());
        c.setAddress(addressField.getText().trim());
        c.setContactNumber(contactField.getText().trim());
        c.setGstNumber(gstField.getText().trim());

        if (companyDAO.updateCompany(c)) {
            JOptionPane.showMessageDialog(this, "Company updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadCompanies();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCompany() {
        if (selectedCompanyId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a company row to delete!", "Select Company", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this company?\nAssociated trips will prevent deletion.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (companyDAO.deleteCompany(selectedCompanyId)) {
                JOptionPane.showMessageDialog(this, "Company removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadCompanies();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete company! Trips are currently linked to this client.", "Integrity Constraint", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        selectedCompanyId = (int) tableModel.getValueAt(row, 0);
        nameField.setText((String) tableModel.getValueAt(row, 1));
        addressField.setText((String) tableModel.getValueAt(row, 2));
        contactField.setText((String) tableModel.getValueAt(row, 3));
        gstField.setText((String) tableModel.getValueAt(row, 4));
    }

    private void clearForm() {
        selectedCompanyId = -1;
        nameField.setText("");
        addressField.setText("");
        contactField.setText("");
        gstField.setText("");
        table.clearSelection();
    }
}
