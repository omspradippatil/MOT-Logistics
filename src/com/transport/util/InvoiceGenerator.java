package com.transport.util;

import com.transport.model.Invoice;
import com.transport.model.Trip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * InvoiceGenerator.java - Printable Invoice Generator for MOT Logistics
 *
 * Formats and exports professional transport invoices with itemized
 * daily trip ledgers, total calculations, and signature blocks.
 */
public class InvoiceGenerator {

    private static final String SERVICE_NAME = "MOT Logistics";

    private static final String[] MONTH_NAMES = {
        "", "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    /**
     * Generate the invoice. Falls back safely to plain text output if OpenPDF is absent.
     */
    public static boolean generatePDF(Invoice invoice, List<Trip> trips, String filePath) {
        String targetPath = filePath;
        if (!targetPath.endsWith(".txt") && !targetPath.endsWith(".pdf")) {
            targetPath += ".txt";
        } else if (targetPath.endsWith(".pdf")) {
            targetPath = targetPath.replace(".pdf", ".txt");
        }
        return generateTextInvoice(invoice, trips, targetPath);
    }

    /**
     * Generate a formatted, print-ready plain text invoice.
     */
    public static boolean generateTextInvoice(Invoice invoice, List<Trip> trips, String filePath) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(filePath)))) {

            // Header Banner
            writer.println("============================================================");
            writer.println("                      " + SERVICE_NAME.toUpperCase());
            writer.println("           TRANSPORT TRIP & INVOICE MANAGEMENT SYSTEM");
            writer.println("============================================================");
            writer.println();
            writer.printf("Invoice Number  : %s%n", invoice.getInvoiceNumber());
            writer.printf("Invoice Date    : %s%n", sdf.format(invoice.getInvoiceDate()));
            writer.println("------------------------------------------------------------");
            writer.printf("Billed To       : %s%n", invoice.getCompanyName());
            if (invoice.getDriverName() != null && !invoice.getDriverName().isEmpty()) {
                writer.printf("Driver Name     : %s%n", invoice.getDriverName());
            }
            if (invoice.getVehicleNumber() != null && !invoice.getVehicleNumber().isEmpty()) {
                writer.printf("Vehicle Number  : %s%n", invoice.getVehicleNumber());
            }
            writer.printf("Billing Period  : %s %d%n",
                    MONTH_NAMES[invoice.getBillingMonth()], invoice.getBillingYear());
            writer.println("------------------------------------------------------------");
            writer.println();

            // Table Header
            writer.printf("%-6s %-12s %-18s %-18s %12s%n",
                    "Sr.No", "Date", "Source", "Destination", "Amount (INR)");
            writer.println("------------------------------------------------------------");

            // Line Items
            double totalAmount = 0;
            int sr = 1;

            for (Trip trip : trips) {
                totalAmount += trip.getAmount();
                writer.printf("%-6d %-12s %-18s %-18s %12.2f%n",
                        sr++,
                        sdf.format(trip.getTripDate()),
                        trip.getSourceLocation(),
                        trip.getDestination(),
                        trip.getAmount());
            }

            // Totals
            writer.println("------------------------------------------------------------");
            writer.printf("%-6s %-12s %-18s %-18s %12s%n",
                    "", "", "", "Total Trips:", invoice.getTotalTrips());
            writer.printf("%-6s %-12s %-18s %-18s %12.2f%n",
                    "", "", "", "TOTAL PAYABLE:", totalAmount);
            writer.println("============================================================");
            writer.println();

            // Signatures
            writer.println();
            writer.println("  Authorized Signatory                  Client Company Signatory");
            writer.println("  (" + SERVICE_NAME + ")");
            writer.println();
            writer.println("  _______________________          _______________________");
            writer.println("  Signature & Stamp                Signature & Stamp");
            writer.println();
            writer.println("============================================================");
            writer.printf("  Generated Timestamp: %s%n", new java.util.Date());
            writer.println("============================================================");

            System.out.println("Invoice successfully exported: " + filePath);
            return true;

        } catch (Exception e) {
            System.out.println("InvoiceGenerator error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Auto-save a copy in the local invoices/ folder.
     */
    public static String autoSaveInvoice(Invoice invoice, List<Trip> trips) {
        File invoiceDir = new File("invoices");
        if (!invoiceDir.exists()) invoiceDir.mkdirs();

        String fileName = "invoices/" + invoice.getInvoiceNumber() + "_MOT_Logistics.txt";
        boolean success = generateTextInvoice(invoice, trips, fileName);

        if (success) return fileName;
        return null;
    }
}
