package com.transport.ui;

import com.transport.dao.InvoiceDAO;
import com.transport.dao.TripDAO;
import com.transport.model.Driver;
import com.transport.model.Invoice;
import com.transport.model.Trip;
import com.transport.util.InvoiceGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * DriverInvoicesPanel.java - Driver Portal for downloading billing invoices and vouchers
 */
public class DriverInvoicesPanel extends JPanel implements Refreshable {

    private Driver driver;
    private JTable table;
    private DefaultTableModel tableModel;

    private ModernButton downloadPdfBtn;
    private ModernButton downloadTxtBtn;
    private ModernButton downloadAllBtn;
    private ModernButton refreshBtn;

    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private TripDAO    tripDAO    = new TripDAO();

    private int selectedInvoiceId = -1;

    public DriverInvoicesPanel(Driver driver) {
        this.driver = driver;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(UITheme.BG_LIGHT);

        initUI();
        loadInvoices();
    }

    private void initUI() {
        // Header Info Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(UITheme.createCardBorder("Assigned Client Invoices & Billing Statements"));
        headerPanel.setBackground(UITheme.CARD_BG);

        JLabel descLabel = new JLabel("View and download official PDF and text invoices generated for trips completed by " + driver.getDriverName());
        descLabel.setFont(UITheme.FONT_BODY);
        descLabel.setForeground(UITheme.TEXT_MUTED);
        descLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        headerPanel.add(descLabel, BorderLayout.CENTER);

        // Invoices Table
        String[] cols = {"ID", "Invoice Number", "Invoice Date", "Client Company", "Billing Period", "Total Trips", "Amount (₹)", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selectedInvoiceId = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(UITheme.createCardBorder("Invoices List"));

        // Action Toolbar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomPanel.setBackground(UITheme.CARD_BG);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        downloadPdfBtn = new ModernButton("📥 Download Invoice (PDF)", UITheme.BRAND_BLUE);
        downloadTxtBtn = new ModernButton("📄 Download Invoice (Text)", UITheme.SECONDARY_GRAY);
        downloadAllBtn = new ModernButton("📦 Download All My Invoices", UITheme.SUCCESS_GREEN);
        refreshBtn     = new ModernButton("↺ Refresh", UITheme.SECONDARY_GRAY);

        downloadPdfBtn.addActionListener(e -> downloadSelectedInvoicePDF());
        downloadTxtBtn.addActionListener(e -> downloadSelectedInvoiceText());
        downloadAllBtn.addActionListener(e -> downloadAllInvoices());
        refreshBtn.addActionListener(e     -> loadInvoices());

        bottomPanel.add(downloadPdfBtn);
        bottomPanel.add(downloadTxtBtn);
        bottomPanel.add(downloadAllBtn);
        bottomPanel.add(refreshBtn);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        loadInvoices();
    }

    private void loadInvoices() {
        tableModel.setRowCount(0);
        selectedInvoiceId = -1;

        List<Invoice> invoices = invoiceDAO.getInvoicesByDriver(driver.getId());
        // Fallback: If no invoices specifically tagged to this driver id, load invoices where driver name matches
        if (invoices.isEmpty()) {
            for (Invoice inv : invoiceDAO.getAllInvoices()) {
                if (driver.getDriverName() != null && driver.getDriverName().equalsIgnoreCase(inv.getDriverName())) {
                    invoices.add(inv);
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Invoice inv : invoices) {
            String period = InvoiceGenerator.getBillingPeriodText(inv.getBillingMonth(), inv.getBillingYear());
            tableModel.addRow(new Object[]{
                inv.getId(),
                inv.getInvoiceNumber(),
                sdf.format(inv.getInvoiceDate()),
                inv.getCompanyName(),
                period,
                inv.getTotalTrips(),
                "₹ " + String.format("%,.2f", inv.getTotalAmount()),
                inv.getStatus()
            });
        }
    }

    private void downloadSelectedInvoicePDF() {
        if (selectedInvoiceId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table first!", "Select Invoice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Invoice inv = invoiceDAO.getInvoiceById(selectedInvoiceId);
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Could not load selected invoice!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Trip> trips = tripDAO.getTripsForInvoice(inv.getCompanyId(), inv.getDriverId(), inv.getVehicleId(), inv.getBillingMonth(), inv.getBillingYear());
        File dir = new File("invoices");
        if (!dir.exists()) dir.mkdirs();

        String path = "invoices/" + inv.getInvoiceNumber() + "_MOT_Logistics.pdf";
        if (InvoiceGenerator.generatePDF(inv, trips, path)) {
            File pdfFile = new File(path);
            int choice = JOptionPane.showConfirmDialog(this,
                "PDF Invoice downloaded successfully!\n\n" +
                "Invoice Number : " + inv.getInvoiceNumber() + "\n" +
                "Client         : " + inv.getCompanyName() + "\n" +
                "Saved Location : " + pdfFile.getAbsolutePath() + "\n\n" +
                "Would you like to open the PDF file now?",
                "Invoice Downloaded", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(pdfFile);
                    }
                } catch (Exception ex) {
                    System.out.println("Could not launch viewer: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to download PDF invoice!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadSelectedInvoiceText() {
        if (selectedInvoiceId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table first!", "Select Invoice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Invoice inv = invoiceDAO.getInvoiceById(selectedInvoiceId);
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Could not load selected invoice!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Trip> trips = tripDAO.getTripsForInvoice(inv.getCompanyId(), inv.getDriverId(), inv.getVehicleId(), inv.getBillingMonth(), inv.getBillingYear());
        File dir = new File("invoices");
        if (!dir.exists()) dir.mkdirs();

        String path = "invoices/" + inv.getInvoiceNumber() + "_MOT_Logistics.txt";
        if (InvoiceGenerator.generateTextInvoice(inv, trips, path)) {
            JOptionPane.showMessageDialog(this,
                "Text Invoice voucher downloaded successfully!\n\n" +
                "Saved Location : " + new File(path).getAbsolutePath(),
                "File Downloaded", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to download Text invoice!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadAllInvoices() {
        List<Invoice> invoices = invoiceDAO.getInvoicesByDriver(driver.getId());
        if (invoices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No invoices found for this driver!", "No Invoices", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File dir = new File("invoices");
        if (!dir.exists()) dir.mkdirs();

        int count = 0;
        for (Invoice inv : invoices) {
            List<Trip> trips = tripDAO.getTripsForInvoice(inv.getCompanyId(), inv.getDriverId(), inv.getVehicleId(), inv.getBillingMonth(), inv.getBillingYear());
            InvoiceGenerator.autoSaveInvoice(inv, trips);
            count++;
        }

        JOptionPane.showMessageDialog(this,
            "Successfully exported " + count + " invoice documents (PDF + Text)!\n\n" +
            "Saved to directory:\n" + dir.getAbsolutePath(),
            "Bulk Download Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
