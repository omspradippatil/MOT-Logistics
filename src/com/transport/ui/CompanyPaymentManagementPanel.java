package com.transport.ui;

import com.transport.dao.InvoiceDAO;
import com.transport.dao.PaymentDAO;
import com.transport.model.Admin;
import com.transport.model.Invoice;
import com.transport.util.Validation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * CompanyPaymentManagementPanel.java - Payment & Settlement Management for Company Admin
 *
 * Displays all invoices for this specific company along with:
 * - Total invoice amount
 * - Amount paid
 * - Remaining balance
 * - Payment date & status (Paid, Partially Paid, Pending)
 * - Ability to record payments with real-time recalculation
 */
public class CompanyPaymentManagementPanel extends JPanel implements Refreshable {

    private Admin companyAdmin;

    private JTable            table;
    private DefaultTableModel tableModel;

    private JLabel totalBilledCard;
    private JLabel totalPaidCard;
    private JLabel totalRemainingCard;

    private JComboBox<String> statusCombo;
    private JComboBox<String> yearCombo;
    private JTextField         searchField;

    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();

    private List<Invoice> invoices;

    public CompanyPaymentManagementPanel(Admin companyAdmin) {
        this.companyAdmin = companyAdmin;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadPayments();
    }

    private void initUI() {
        // Top 3 Stat Cards
        JPanel statCardsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        statCardsPanel.setOpaque(false);
        statCardsPanel.setPreferredSize(new Dimension(100, 90));

        totalBilledCard    = createStatCard("Total Invoice Amount", "₹ 0.00", UITheme.BRAND_BLUE);
        totalPaidCard      = createStatCard("Total Amount Paid",   "₹ 0.00", UITheme.SUCCESS_GREEN);
        totalRemainingCard = createStatCard("Remaining Balance",   "₹ 0.00", UITheme.DANGER_RED);

        statCardsPanel.add(totalBilledCard);
        statCardsPanel.add(totalPaidCard);
        statCardsPanel.add(totalRemainingCard);

        JPanel topContainer = new JPanel(new BorderLayout(0, 8));
        topContainer.setOpaque(false);
        topContainer.add(statCardsPanel, BorderLayout.NORTH);

        // Filter toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(UITheme.CARD_BG);
        filterBar.setBorder(UITheme.createCardBorder("Filter Payment Invoices"));

        filterBar.add(new JLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{"All Statuses", "Paid", "Partially Paid", "Generated", "Pending"});
        statusCombo.setFont(UITheme.FONT_BODY);
        statusCombo.setPreferredSize(new Dimension(130, 30));
        statusCombo.addActionListener(e -> loadPayments());
        filterBar.add(statusCombo);

        filterBar.add(new JLabel("Year:"));
        yearCombo = new JComboBox<>(new String[]{"All Years", "2024", "2025", "2026", "2027"});
        yearCombo.setFont(UITheme.FONT_BODY);
        yearCombo.setPreferredSize(new Dimension(100, 30));
        yearCombo.addActionListener(e -> loadPayments());
        filterBar.add(yearCombo);

        filterBar.add(new JLabel("Search Invoice:"));
        searchField = new JTextField(12);
        searchField.setFont(UITheme.FONT_BODY);
        searchField.setPreferredSize(new Dimension(140, 30));
        searchField.addActionListener(e -> loadPayments());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadPayments(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadPayments(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadPayments(); }
        });
        filterBar.add(searchField);

        ModernButton resetBtn = new ModernButton("↺ Reset", UITheme.SECONDARY_GRAY);
        resetBtn.setPreferredSize(new Dimension(85, 30));
        resetBtn.addActionListener(e -> {
            statusCombo.setSelectedIndex(0);
            yearCombo.setSelectedIndex(0);
            searchField.setText("");
            loadPayments();
        });
        filterBar.add(resetBtn);

        topContainer.add(filterBar, BorderLayout.SOUTH);

        // Table
        String[] cols = {
            "ID", "Invoice Number", "Invoice Date", "Period",
            "Total Amount (₹)", "Amount Paid (₹)", "Remaining (₹)", "Payment Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Hide internal ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Payment Records & Settlement Status - " + companyAdmin.getCompanyName()));

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        ModernButton recordPayBtn = new ModernButton("💳 Record / Settle Payment", UITheme.SUCCESS_GREEN);
        recordPayBtn.setPreferredSize(new Dimension(220, 36));
        recordPayBtn.addActionListener(e -> recordPaymentDialog());

        ModernButton refreshBtn = new ModernButton("↺ Refresh", UITheme.SECONDARY_GRAY);
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.addActionListener(e -> loadPayments());

        bottomBar.add(recordPayBtn);
        bottomBar.add(refreshBtn);

        add(topContainer,   BorderLayout.NORTH);
        add(scrollPane,     BorderLayout.CENTER);
        add(bottomBar,      BorderLayout.SOUTH);
    }

    private JLabel createStatCard(String title, String initialVal, Color accent) {
        JLabel card = new JLabel("<html><div style='padding:6px;'>" +
                "<span style='color:#64748B;font-size:10px;text-transform:uppercase;'>" + title + "</span><br>" +
                "<b style='font-size:16px;color:#0F172A;'>" + initialVal + "</b></div></html>", SwingConstants.CENTER);
        card.setOpaque(true);
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1)
        ));
        return card;
    }

    public void loadPayments() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // STRICT ISOLATION: company_id = companyAdmin.getCompanyId()
        invoices = invoiceDAO.getInvoicesByCompany(companyAdmin.getCompanyId());

        String statusFilter = (statusCombo != null && statusCombo.getSelectedItem() != null)
                ? (String) statusCombo.getSelectedItem() : "All Statuses";
        String yearFilter = (yearCombo != null && yearCombo.getSelectedItem() != null)
                ? (String) yearCombo.getSelectedItem() : "All Years";
        String kw = (searchField != null) ? searchField.getText().trim().toLowerCase() : "";

        double totalBilled = 0.0;
        double totalPaid   = 0.0;
        double totalRem    = 0.0;

        for (Invoice inv : invoices) {
            if (!"All Statuses".equalsIgnoreCase(statusFilter) && !statusFilter.equalsIgnoreCase(inv.getStatus())) {
                continue;
            }
            if (!"All Years".equalsIgnoreCase(yearFilter)) {
                try {
                    int y = Integer.parseInt(yearFilter);
                    if (inv.getBillingYear() != y) continue;
                } catch (NumberFormatException ignored) {}
            }
            if (!kw.isEmpty()) {
                String invNum = inv.getInvoiceNumber() != null ? inv.getInvoiceNumber().toLowerCase() : "";
                if (!invNum.contains(kw)) continue;
            }

            totalBilled += inv.getTotalAmount();
            totalPaid   += inv.getAmountPaid();
            totalRem    += inv.getRemainingAmount();

            String period = (inv.getBillingMonth() <= 0)
                    ? "All Months " + inv.getBillingYear()
                    : inv.getBillingMonth() + "/" + inv.getBillingYear();

            tableModel.addRow(new Object[]{
                inv.getId(),
                inv.getInvoiceNumber(),
                sdf.format(inv.getInvoiceDate()),
                period,
                String.format("₹ %,.2f", inv.getTotalAmount()),
                String.format("₹ %,.2f", inv.getAmountPaid()),
                String.format("₹ %,.2f", inv.getRemainingAmount()),
                inv.getStatus()
            });
        }

        updateCard(totalBilledCard,    "Total Invoice Amount", String.format("₹ %,.2f", totalBilled));
        updateCard(totalPaidCard,      "Total Amount Paid",   String.format("₹ %,.2f", totalPaid));
        updateCard(totalRemainingCard, "Remaining Balance",   String.format("₹ %,.2f", totalRem));
    }

    private void updateCard(JLabel card, String title, String val) {
        card.setText("<html><div style='padding:6px;'>" +
                "<span style='color:#64748B;font-size:10px;text-transform:uppercase;'>" + title + "</span><br>" +
                "<b style='font-size:16px;color:#0F172A;'>" + val + "</b></div></html>");
    }

    private void recordPaymentDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table to record a payment.", "Select Invoice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int invoiceId = (int) tableModel.getValueAt(row, 0);
        Invoice inv = invoiceDAO.getInvoiceById(invoiceId);
        if (inv == null) return;

        if (inv.getRemainingAmount() <= 0) {
            JOptionPane.showMessageDialog(this, "This invoice is already fully Paid! Remaining balance is ₹ 0.00.", "Fully Paid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextField amountField = new JTextField(String.format("%.2f", inv.getRemainingAmount()));
        JTextField notesField  = new JTextField("Settlement via Bank Transfer / Cheque");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Invoice Number:"));
        form.add(new JLabel(inv.getInvoiceNumber()));
        form.add(new JLabel("Amount to Pay (₹):"));
        form.add(amountField);
        form.add(new JLabel("Payment Notes:"));
        form.add(notesField);

        int result = JOptionPane.showConfirmDialog(this, form, "Record Payment - " + inv.getInvoiceNumber(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String amtStr = amountField.getText().trim();
            if (!Validation.isValidAmount(amtStr)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive number for payment amount.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double payAmt = Double.parseDouble(amtStr);
            if (payAmt > inv.getRemainingAmount()) {
                JOptionPane.showMessageDialog(this, "Payment amount cannot exceed the remaining balance of ₹ " + String.format("%.2f", inv.getRemainingAmount()), "Amount Exceeded", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (invoiceDAO.recordPayment(invoiceId, companyAdmin.getCompanyId(), payAmt, notesField.getText().trim(), new Date())) {
                JOptionPane.showMessageDialog(this, "Payment of ₹ " + String.format("%.2f", payAmt) + " recorded successfully!", "Payment Recorded", JOptionPane.INFORMATION_MESSAGE);
                loadPayments();
            } else {
                JOptionPane.showMessageDialog(this, "Database error: Failed to record payment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refreshData() {
        loadPayments();
    }
}
