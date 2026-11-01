package com.transport.ui;

import com.transport.dao.PaymentDAO;
import com.transport.model.Payment;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * PaymentPanel.java - Manage Payments
 *
 * Admin can:
 * - View all payment records
 * - Update payment status (Pending -> Paid / Partially Paid)
 * - Track how much is received and how much is remaining
 */
public class PaymentPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;

    // Edit fields
    private JTextField amountReceivedField;
    private JComboBox<String> statusCombo;
    private JTextField paymentDateField;
    private JTextField notesField;

    private JButton updateBtn;
    private JButton refreshBtn;

    private PaymentDAO paymentDAO = new PaymentDAO();
    private int selectedPaymentId = -1;
    private double selectedTotalAmount = 0;

    private static final String[] STATUS_OPTIONS = {"Pending", "Partially Paid", "Paid"};

    public PaymentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        initUI();
        loadPayments();
    }

    private void initUI() {
        // Update form at top
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Update Payment"));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        amountReceivedField = new JTextField(12);
        statusCombo         = new JComboBox<>(STATUS_OPTIONS);
        paymentDateField    = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()), 12);
        notesField          = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Amount Received (Rs.):"), gbc);
        gbc.gridx = 1; formPanel.add(amountReceivedField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Payment Date (dd/MM/yyyy):"), gbc);
        gbc.gridx = 3; formPanel.add(paymentDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Payment Status:"), gbc);
        gbc.gridx = 1; formPanel.add(statusCombo, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 3; formPanel.add(notesField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);
        updateBtn  = createButton("Update Payment", new Color(0, 100, 180));
        refreshBtn = createButton("Refresh",        Color.DARK_GRAY);
        btnPanel.add(updateBtn);
        btnPanel.add(refreshBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);

        // Instructions
        JLabel hintLabel = new JLabel("  Select a row from the table below to update payment.");
        hintLabel.setForeground(new Color(100, 100, 100));
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));

        // Table
        String[] cols = {"ID", "Invoice No.", "Company", "Total (Rs.)", "Received (Rs.)", "Remaining (Rs.)", "Payment Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(0, 70, 130));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Color rows by status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    String status = (String) tableModel.getValueAt(row, 7);
                    if ("Paid".equals(status)) {
                        setBackground(new Color(220, 255, 220));
                    } else if ("Partially Paid".equals(status)) {
                        setBackground(new Color(255, 255, 200));
                    } else {
                        setBackground(new Color(255, 230, 230)); // Pending
                    }
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("All Payment Records"));

        updateBtn.addActionListener(e  -> updatePayment());
        refreshBtn.addActionListener(e -> loadPayments());

        JPanel northPanel = new JPanel(new BorderLayout(0, 5));
        northPanel.setOpaque(false);
        northPanel.add(formPanel,  BorderLayout.NORTH);
        northPanel.add(hintLabel,  BorderLayout.SOUTH);

        add(northPanel,   BorderLayout.NORTH);
        add(scrollPane,   BorderLayout.CENTER);
    }

    private void loadPayments() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Payment p : paymentDAO.getAllPayments()) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getInvoiceNumber(),
                p.getCompanyName(),
                String.format("%.2f", p.getTotalAmount()),
                String.format("%.2f", p.getAmountReceived()),
                String.format("%.2f", p.getRemainingAmount()),
                p.getPaymentDate() != null ? sdf.format(p.getPaymentDate()) : "Not Paid",
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
        paymentDateField.setText("Not Paid".equals(dateVal) ? new SimpleDateFormat("dd/MM/yyyy").format(new Date()) : dateVal);
    }

    private void updatePayment() {
        if (selectedPaymentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payment record!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double received;
        try {
            received = Double.parseDouble(amountReceivedField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid received amount!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double remaining = selectedTotalAmount - received;

        Payment payment = new Payment();
        payment.setId(selectedPaymentId);
        payment.setAmountReceived(received);
        payment.setRemainingAmount(remaining < 0 ? 0 : remaining);
        payment.setPaymentStatus((String) statusCombo.getSelectedItem());
        payment.setNotes(notesField.getText().trim());

        // Parse date
        try {
            Date pDate = new SimpleDateFormat("dd/MM/yyyy").parse(paymentDateField.getText().trim());
            payment.setPaymentDate(pDate);
        } catch (Exception ex) {
            payment.setPaymentDate(new Date());
        }

        if (paymentDAO.updatePayment(payment)) {
            JOptionPane.showMessageDialog(this, "Payment updated successfully!");
            loadPayments();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update payment!", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
