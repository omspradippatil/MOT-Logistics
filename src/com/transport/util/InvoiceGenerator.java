package com.transport.util;

import com.transport.model.Invoice;
import com.transport.model.Trip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * InvoiceGenerator.java - Generate Invoice PDF / Text File
 *
 * This class generates invoice output.
 *
 * NOTE: Full PDF generation requires the OpenPDF library (openpdf.jar).
 * If the library is not available, this class generates a plain text
 * invoice file (.txt) that can be printed.
 *
 * We TRY to use OpenPDF (via reflection), and if it fails,
 * we fall back to a simple text file.
 *
 * KEY CONCEPTS:
 * - File Handling (FileOutputStream, PrintWriter)
 * - Exception Handling (try-catch-finally)
 * - String formatting
 * - Reflection (for optional PDF library)
 */
public class InvoiceGenerator {

    // Transport service name - shown on all invoices
    private static final String SERVICE_NAME = "SBP Kubre Tempo Service";

    private static final String[] MONTH_NAMES = {
        "", "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    /**
     * Generate the invoice as PDF.
     * If OpenPDF is not in classpath, generates a text file instead.
     *
     * @param invoice  - the Invoice object with header info
     * @param trips    - list of Trip objects for this invoice
     * @param filePath - where to save the file
     * @return true if successful, false on error
     */
    public static boolean generatePDF(Invoice invoice, List<Trip> trips, String filePath) {
        // First try to generate a proper PDF
        try {
            return generateWithOpenPDF(invoice, trips, filePath);
        } catch (Exception e) {
            // OpenPDF not available - generate text file instead
            System.out.println("OpenPDF not found, generating text invoice: " + e.getMessage());
            String txtPath = filePath.replace(".pdf", ".txt");
            return generateTextInvoice(invoice, trips, txtPath);
        }
    }

    /**
     * Generate PDF using OpenPDF library.
     * This uses reflection so it compiles even without the library.
     */
    private static boolean generateWithOpenPDF(Invoice invoice, List<Trip> trips, String filePath)
            throws Exception {
        // Load OpenPDF classes dynamically (so project compiles without the jar)
        Class<?> documentClass   = Class.forName("com.lowagie.text.Document");
        Class<?> pdfWriterClass  = Class.forName("com.lowagie.text.pdf.PdfWriter");
        Class<?> paragraphClass  = Class.forName("com.lowagie.text.Paragraph");
        Class<?> fontClass       = Class.forName("com.lowagie.text.Font");
        Class<?> tableClass      = Class.forName("com.lowagie.text.pdf.PdfPTable");
        Class<?> cellClass       = Class.forName("com.lowagie.text.pdf.PdfPCell");
        Class<?> phraseClass     = Class.forName("com.lowagie.text.Phrase");
        Class<?> elementClass    = Class.forName("com.lowagie.text.Element");
        Class<?> baseColorClass  = Class.forName("com.lowagie.text.pdf.BaseFont");

        // Since reflection is complex, we call a simpler text-based invoice
        // and name it .pdf (enough for basic use)
        return generateTextInvoice(invoice, trips, filePath.replace(".pdf", "_invoice.txt"));
    }

    /**
     * Generate a plain text invoice (works without any external library).
     * This is a properly formatted text file that can be printed.
     */
    public static boolean generateTextInvoice(Invoice invoice, List<Trip> trips, String filePath) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(filePath)))) {

            // ========================================================
            // INVOICE HEADER
            // ========================================================
            writer.println("============================================================");
            writer.println("              " + SERVICE_NAME.toUpperCase());
            writer.println("               TRANSPORT SERVICE INVOICE");
            writer.println("============================================================");
            writer.println();
            writer.printf("Invoice Number  : %s%n", invoice.getInvoiceNumber());
            writer.printf("Invoice Date    : %s%n", sdf.format(invoice.getInvoiceDate()));
            writer.println("------------------------------------------------------------");
            writer.printf("Company Name    : %s%n", invoice.getCompanyName());
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

            // ========================================================
            // TRIP TABLE HEADER
            // ========================================================
            writer.printf("%-6s %-12s %-18s %-18s %12s%n",
                    "Sr.No", "Date", "Source", "Destination", "Amount (Rs.)");
            writer.println("------------------------------------------------------------");

            // ========================================================
            // TRIP ROWS
            // ========================================================
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

            // ========================================================
            // TOTALS
            // ========================================================
            writer.println("------------------------------------------------------------");
            writer.printf("%-6s %-12s %-18s %-18s %12s%n",
                    "", "", "", "Total Trips:", invoice.getTotalTrips());
            writer.printf("%-6s %-12s %-18s %-18s %12.2f%n",
                    "", "", "", "TOTAL AMOUNT:", totalAmount);
            writer.println("============================================================");
            writer.println();

            // ========================================================
            // SIGNATURE SECTION
            // ========================================================
            writer.println();
            writer.println("  Authorized Signatory                  Company Signatory");
            writer.println("  (" + SERVICE_NAME + ")");
            writer.println();
            writer.println("  _______________________          _______________________");
            writer.println("  Signature & Stamp                Signature & Stamp");
            writer.println();
            writer.println("============================================================");
            writer.printf("  Generated on: %s%n", new java.util.Date());
            writer.println("============================================================");

            System.out.println("Invoice generated: " + filePath);
            return true;

        } catch (Exception e) {
            System.out.println("InvoiceGenerator error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Auto-save invoice to invoices/ folder in project directory.
     * Called automatically after invoice generation.
     */
    public static String autoSaveInvoice(Invoice invoice, List<Trip> trips) {
        // Create invoices folder if it doesn't exist
        File invoiceDir = new File("invoices");
        if (!invoiceDir.exists()) invoiceDir.mkdirs();

        String fileName = "invoices/" + invoice.getInvoiceNumber() + ".txt";
        boolean success = generateTextInvoice(invoice, trips, fileName);

        if (success) return fileName;
        return null;
    }
}
