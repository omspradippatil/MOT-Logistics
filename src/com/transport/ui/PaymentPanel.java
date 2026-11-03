package com.transport.ui;

import com.transport.dao.PaymentDAO;
import com.transport.model.Payment;
import com.transport.util.Validation;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PaymentPanel.java - Accounts Receivable & Payment Lifecycle Management
 */
public class PaymentPanel extends JPanel implements Refreshable {

    private JTable            table;
    private DefaultTableModel tableModel;

    private JTextField        amountReceivedField;
    private JComboBox<String> statusCombo;
    private JTextField        paymentDateField;
    private JTextField        notesField;

    private JComboBox<String> filterStatusCombo;
    private JTextField        filterSearchField;

    private ModernButton updateBtn;
    private ModernButton refreshBtn;

    private PaymentDAO paymentDAO = new PaymentDAO();
    private int selectedPaymentId = -1;
    private double selectedTotalAmount = 0;

    private static final String[] STATUS_OPTIONS = {"Pending", "Partially Paid", "Paid"};

    public PaymentPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadPayments();
    }

    private void initUI() {
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(UITheme.createCardBorder("Update Invoice Payment Record"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        amountReceivedField = new JTextField(12);
        statusCombo         = new JComboBox<>(STATUS_OPTIONS);
        paymentDateField    = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()), 12);
        notesField          = new JTextField(20);

        styleField(amountReceivedField);
        styleCombo(statusCombo);
        styleField(paymentDateField);
        styleField(notesField);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Amount Received (₹): *"), gbc);
        gbc.gridx = 1; formPanel.add(amountReceivedField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Payment Date (dd/mm/yyyy):"), gbc);
        gbc.gridx = 3; formPanel.add(paymentDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Settlement Status: *"), gbc);
        gbc.gridx = 1; formPanel.add(statusCombo, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Notes / Transaction Ref:"), gbc);
        gbc.gridx = 3; formPanel.add(notesField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnPanel.setOpaque(false);
        updateBtn  = new ModernButton("✓ Update Payment Status", UITheme.SUCCESS_GREEN);
        refreshBtn = new ModernButton("↺ Refresh Records", UITheme.SECONDARY_GRAY);
        btnPanel.add(updateBtn);
        btnPanel.add(refreshBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);

        JLabel hintLabel = new JLabel("  💡 Tip: Click any row in the ledger below to populate and update its payment status.");
        hintLabel.setForeground(UITheme.TEXT_MUTED);
        hintLabel.setFont(UITheme.FONT_SMALL);

        // Table
        String[] cols = {"ID", "Invoice No.", "Client Company", "Total (₹)", "Received (₹)", "Remaining (₹)", "Payment Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // High contrast status badges
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    String status = (String) tableModel.getValueAt(row, 7);
                    if ("Paid".equals(status)) {
                        setBackground(new Color(220, 252, 231)); // Soft Emerald
                        setForeground(new Color(22, 101, 52));
                    } else if ("Partially Paid".equals(status)) {
                        setBackground(new Color(254, 243, 199)); // Soft Amber
                        setForeground(new Color(146, 64, 14));
                    } else {
                        setBackground(new Color(254, 226, 226)); // Soft Rose
                        setForeground(new Color(153, 27, 27));
                    }
                } else {
                    setBackground(new Color(219, 234, 254));
                    setForeground(UITheme.BRAND_NAVY);
                }
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fillFormFromRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Accounts Receivable Ledger"));

        updateBtn.addActionListener(e  -> updatePayment());
        refreshBtn.addActionListener(e -> loadPayments());

        JPanel northPanel = new JPanel(new BorderLayout(0, 6));
        northPanel.setOpaque(false);
        northPanel.add(formPanel,  BorderLayout.NORTH);

        // Filter toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(UITheme.CARD_BG);
        filterBar.setBorder(UITheme.createCardBorder("Filter Accounts Receivable Ledger"));

        filterBar.add(new JLabel("Status:"));
        filterStatusCombo = new JComboBox<>(new String[]{"All Statuses", "Paid", "Partially Paid", "Pending"});
        filterStatusCombo.setFont(UITheme.FONT_BODY);
        filterStatusCombo.setPreferredSize(new Dimension(130, 30));
        filterStatusCombo.addActionListener(e -> loadPayments());
        filterBar.add(filterStatusCombo);

        filterBar.add(new JLabel("Search Invoice / Company:"));
        filterSearchField = new JTextField(15);
        filterSearchField.setFont(UITheme.FONT_BODY);
        filterSearchField.setPreferredSize(new Dimension(160, 30));
        filterSearchField.addActionListener(e -> loadPayments());
        filterSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadPayments(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadPayments(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadPayments(); }
        });
        filterBar.add(filterSearchField);

        ModernButton resetFilterBtn = new ModernButton("↺ Reset", UITheme.SECONDARY_GRAY);
        resetFilterBtn.setPreferredSize(new Dimension(85, 30));
        resetFilterBtn.addActionListener(e -> {
            filterStatusCombo.setSelectedIndex(0);
            filterSearchField.setText("");
            loadPayments();
        });
        filterBar.add(resetFilterBtn);

        JPanel centerNorth = new JPanel(new BorderLayout(0, 4));
        centerNorth.setOpaque(false);
        centerNorth.add(hintLabel, BorderLayout.NORTH);
        centerNorth.add(filterBar, BorderLayout.SOUTH);

        northPanel.add(centerNorth,  BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleField(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setPreferredSize(new Dimension(170, 32));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(UITheme.FONT_BODY);
        combo.setPreferredSize(new Dimension(170, 32));
    }

    @Override
    public void refreshData() {
        loadPayments();
    }

    private void loadPayments() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        String statusFilter = (filterStatusCombo != null && filterStatusCombo.getSelectedItem() != null)
                ? (String) filterStatusCombo.getSelectedItem() : "All Statuses";
        String kw = (filterSearchField != null) ? filterSearchField.getText().trim().toLowerCase() : "";

        for (Payment p : paymentDAO.getAllPayments()) {
            if (!"All Statuses".equalsIgnoreCase(statusFilter) && !statusFilter.equalsIgnoreCase(p.getPaymentStatus())) {
                continue;
            }
            if (!kw.isEmpty()) {
                String inv = p.getInvoiceNumber() != null ? p.getInvoiceNumber().toLowerCase() : "";
                String comp = p.getCompanyName() != null ? p.getCompanyName().toLowerCase() : "";
                String notes = p.getNotes() != null ? p.getNotes().toLowerCase() : "";
                if (!inv.contains(kw) && !comp.contains(kw) && !notes.contains(kw)) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                p.getId(),
                p.getInvoiceNumber(),
                p.getCompanyName(),
                String.format("%.2f", p.getTotalAmount()),
                String.format("%.2f", p.getAmountReceived()),
                String.format("%.2f", p.getRemainingAmount()),
                p.getPaymentDate() != null ? sdf.format(p.getPaymentDate()) : "Unpaid",
                p.getPaymentStatus()
            });
        }
    }

    private void fillFormFromRow() {
        int row = table.getSelectedRow();
        selectedPaymentId   = (int) tableModel.getValueAt(row, 0);
        selectedTotalAmount = Double.parseDouble((String) tableModel.getValueAt(row, 3));
        amountReceivedField.setText((String) tableModel.getValueAt(row, 4));
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 7));
        String dateVal = (String) tableModel.getValueAt(row, 6);
        paymentDateField.setText("Unpaid".equals(dateVal) ? new SimpleDateFormat("dd/MM/yyyy").format(new Date()) : dateVal);
    }

    private void updatePayment() {
        if (selectedPaymentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice payment row from the table first!", "Select Payment", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!Validation.isValidAmount(amountReceivedField.getText().trim()) && !"0".equals(amountReceivedField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric received amount!", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double received = Double.parseDouble(amountReceivedField.getText().trim());
        double remaining = selectedTotalAmount - received;
        if (remaining < 0) remaining = 0;

        Payment payment = new Payment();
        payment.setId(selectedPaymentId);
        payment.setAmountReceived(received);
        payment.setRemainingAmount(remaining);
        payment.setPaymentStatus((String) statusCombo.getSelectedItem());
        payment.setNotes(notesField.getText().trim());

        try {
            Date pDate = new SimpleDateFormat("dd/MM/yyyy").parse(paymentDateField.getText().trim());
            payment.setPaymentDate(pDate);
        } catch (Exception ex) {
            payment.setPaymentDate(new Date());
        }

        if (paymentDAO.updatePayment(payment)) {
            JOptionPane.showMessageDialog(this, "Payment record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPayments();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update payment record!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
