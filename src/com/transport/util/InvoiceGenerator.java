package com.transport.util;

import com.transport.model.Invoice;
import com.transport.model.Trip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * InvoiceGenerator.java - Native PDF & Text Invoice Generator for MOT Logistics
 *
 * Generates valid PDF documents (PDF 1.4 specification) and formatted
 * text billing invoices using pure Core Java (no external JARs needed).
 */
public class InvoiceGenerator {

    private static final String SERVICE_NAME = "MOT Logistics";

    private static final String[] MONTH_NAMES = {
        "", "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    /**
     * Generate a genuine, standard PDF 1.4 invoice document.
     * Compatible with Adobe Acrobat, Apple Preview, Google Chrome, and web browsers.
     *
     * @param invoice  The invoice metadata object
     * @param trips    The itemized list of trips
     * @param filePath The destination file path (.pdf)
     * @return true if successful, false on error
     */
    public static boolean generatePDF(Invoice invoice, List<Trip> trips, String filePath) {
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
            List<Integer> offsets = new ArrayList<>();
            offsets.add(0);

            // 1. PDF Header
            pdfStream.write("%PDF-1.4\n%âãÏÓ\n".getBytes("ISO-8859-1"));

            // 2. Catalog (Obj 1)
            offsets.add(pdfStream.size());
            pdfStream.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes("ISO-8859-1"));

            // 3. Pages (Obj 2)
            offsets.add(pdfStream.size());
            pdfStream.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes("ISO-8859-1"));

            // 4. Page (Obj 3) - Standard A4 (595 x 842 pt)
            offsets.add(pdfStream.size());
            pdfStream.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>\nendobj\n".getBytes("ISO-8859-1"));

            // 5. Build Content Stream Operators
            StringBuilder sb = new StringBuilder();

            // --- Top Navy Brand Header Banner ---
            sb.append("0.06 0.15 0.33 rg\n"); // Navy blue fill
            sb.append("40 760 515 52 re f\n");

            sb.append("1 1 1 rg\n"); // White text
            sb.append("BT /F1 18 Tf 55 790 Td (MOT LOGISTICS) Tj ET\n");
            sb.append("BT /F2 9 Tf 55 772 Td (COMMERCIAL TRANSPORT FLEET MANAGEMENT & BILLING SYSTEM) Tj ET\n");

            // --- Invoice Header Details Grid ---
            sb.append("0.1 0.1 0.1 rg\n"); // Charcoal text
            sb.append("BT /F1 10 Tf 45 734 Td (Invoice Number:) Tj ET\n");
            sb.append("BT /F2 10 Tf 140 734 Td (").append(cleanPdfText(invoice.getInvoiceNumber())).append(") Tj ET\n");

            sb.append("BT /F1 10 Tf 45 716 Td (Invoice Date:) Tj ET\n");
            sb.append("BT /F2 10 Tf 140 716 Td (").append(sdf.format(invoice.getInvoiceDate())).append(") Tj ET\n");

            sb.append("BT /F1 10 Tf 45 698 Td (Billing Period:) Tj ET\n");
            sb.append("BT /F2 10 Tf 140 698 Td (").append(MONTH_NAMES[invoice.getBillingMonth()]).append(" ").append(invoice.getBillingYear()).append(") Tj ET\n");

            sb.append("BT /F1 10 Tf 315 734 Td (Billed To:) Tj ET\n");
            sb.append("BT /F2 10 Tf 385 734 Td (").append(cleanPdfText(invoice.getCompanyName())).append(") Tj ET\n");

            String driverText = invoice.getDriverName() != null ? invoice.getDriverName() : "Fleet Operations";
            sb.append("BT /F1 10 Tf 315 716 Td (Driver:) Tj ET\n");
            sb.append("BT /F2 10 Tf 385 716 Td (").append(cleanPdfText(driverText)).append(") Tj ET\n");

            String vehicleText = invoice.getVehicleNumber() != null ? invoice.getVehicleNumber() : "Assigned Fleet";
            sb.append("BT /F1 10 Tf 315 698 Td (Vehicle:) Tj ET\n");
            sb.append("BT /F2 10 Tf 385 698 Td (").append(cleanPdfText(vehicleText)).append(") Tj ET\n");

            // Subtle horizontal rule
            sb.append("0.85 0.88 0.92 rg\n");
            sb.append("40 682 515 1 re f\n");

            // --- Itemized Table Header Bar ---
            sb.append("0.93 0.95 0.98 rg\n"); // Light slate fill
            sb.append("40 652 515 22 re f\n");
            sb.append("0.06 0.15 0.33 rg\n"); // Navy column titles
            sb.append("BT /F1 9 Tf 50 659 Td (Sr.) Tj ET\n");
            sb.append("BT /F1 9 Tf 85 659 Td (Trip Date) Tj ET\n");
            sb.append("BT /F1 9 Tf 165 659 Td (Source Location) Tj ET\n");
            sb.append("BT /F1 9 Tf 310 659 Td (Destination) Tj ET\n");
            sb.append("BT /F1 9 Tf 465 659 Td (Amount (INR)) Tj ET\n");

            // --- Itemized Table Rows ---
            int y = 632;
            int sr = 1;
            double grandTotal = 0.0;

            for (Trip trip : trips) {
                grandTotal += trip.getAmount();

                // Row divider line
                sb.append("0.92 0.94 0.96 rg\n");
                sb.append("40 ").append(y - 5).append(" 515 0.5 re f\n");

                sb.append("0.15 0.15 0.15 rg\n"); // Row text
                sb.append("BT /F2 9 Tf 50 ").append(y).append(" Td (").append(sr++).append(") Tj ET\n");
                sb.append("BT /F2 9 Tf 85 ").append(y).append(" Td (").append(sdf.format(trip.getTripDate())).append(") Tj ET\n");
                sb.append("BT /F2 9 Tf 165 ").append(y).append(" Td (").append(cleanPdfText(trip.getSourceLocation())).append(") Tj ET\n");
                sb.append("BT /F2 9 Tf 310 ").append(y).append(" Td (").append(cleanPdfText(trip.getDestination())).append(") Tj ET\n");
                sb.append("BT /F2 9 Tf 465 ").append(y).append(" Td (").append(String.format("%,.2f", trip.getAmount())).append(") Tj ET\n");

                y -= 20;
                if (y < 220) break; // Keep within single page layout boundary
            }

            // --- Summary Total Box ---
            y -= 8;
            sb.append("0.95 0.97 0.99 rg\n"); // Very soft accent fill
            sb.append("40 ").append(y - 8).append(" 515 26 re f\n");

            sb.append("0.06 0.15 0.33 rg\n"); // Navy text
            sb.append("BT /F1 10 Tf 50 ").append(y).append(" Td (Total Completed Trips: ").append(trips.size()).append(") Tj ET\n");
            sb.append("BT /F1 11 Tf 340 ").append(y).append(" Td (TOTAL PAYABLE: INR ").append(String.format("%,.2f", grandTotal)).append(") Tj ET\n");

            // --- Signatures Section ---
            int sigY = Math.max(90, y - 90);
            sb.append("0.2 0.2 0.2 rg\n");
            sb.append("40 ").append(sigY + 30).append(" 180 1 re f\n");
            sb.append("BT /F1 9 Tf 40 ").append(sigY + 15).append(" Td (Authorized Signatory) Tj ET\n");
            sb.append("BT /F2 8 Tf 40 ").append(sigY + 3).append(" Td (MOT Logistics) Tj ET\n");

            sb.append("375 ").append(sigY + 30).append(" 180 1 re f\n");
            sb.append("BT /F1 9 Tf 375 ").append(sigY + 15).append(" Td (Client Signatory) Tj ET\n");
            sb.append("BT /F2 8 Tf 375 ").append(sigY + 3).append(" Td (").append(cleanPdfText(invoice.getCompanyName())).append(") Tj ET\n");

            // --- Footer ---
            sb.append("0.5 0.5 0.5 rg\n");
            sb.append("40 45 515 0.5 re f\n");
            sb.append("BT /F2 8 Tf 40 32 Td (Generated on: ").append(new Date().toString()).append(" | Computer generated invoice by MOT Logistics) Tj ET\n");

            byte[] streamBytes = sb.toString().getBytes("ISO-8859-1");

            // 6. Content Stream (Obj 4)
            offsets.add(pdfStream.size());
            pdfStream.write(String.format("4 0 obj\n<< /Length %d >>\nstream\n", streamBytes.length).getBytes("ISO-8859-1"));
            pdfStream.write(streamBytes);
            pdfStream.write("\nendstream\nendobj\n".getBytes("ISO-8859-1"));

            // 7. Font F1 (Helvetica-Bold) (Obj 5)
            offsets.add(pdfStream.size());
            pdfStream.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes("ISO-8859-1"));

            // 8. Font F2 (Helvetica) (Obj 6)
            offsets.add(pdfStream.size());
            pdfStream.write("6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes("ISO-8859-1"));

            // 9. Cross-reference Table (xref)
            int xrefStart = pdfStream.size();
            pdfStream.write(String.format("xref\n0 %d\n0000000000 65535 f \n", offsets.size()).getBytes("ISO-8859-1"));
            for (int i = 1; i < offsets.size(); i++) {
                pdfStream.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes("ISO-8859-1"));
            }

            // 10. Trailer & EOF
            pdfStream.write(String.format("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n", offsets.size(), xrefStart).getBytes("ISO-8859-1"));

            // Write out to destination file
            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                pdfStream.writeTo(fos);
            }

            System.out.println("PDF Invoice generated successfully: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("InvoiceGenerator.generatePDF error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Helper to escape parenthesis and backslashes in PDF text strings */
    private static String cleanPdfText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replace("₹", "INR ");
    }

    /**
     * Generate a formatted, print-ready plain text invoice (.txt).
     */
    public static boolean generateTextInvoice(Invoice invoice, List<Trip> trips, String filePath) {
        if (!filePath.toLowerCase().endsWith(".txt")) {
            filePath += ".txt";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(filePath)))) {

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

            writer.printf("%-6s %-12s %-18s %-18s %12s%n",
                    "Sr.No", "Date", "Source", "Destination", "Amount (INR)");
            writer.println("------------------------------------------------------------");

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

            writer.println("------------------------------------------------------------");
            writer.printf("%-6s %-12s %-18s %-18s %12s%n",
                    "", "", "", "Total Trips:", invoice.getTotalTrips());
            writer.printf("%-6s %-12s %-18s %-18s %12.2f%n",
                    "", "", "", "TOTAL PAYABLE:", totalAmount);
            writer.println("============================================================");
            writer.println();

            writer.println("  Authorized Signatory                  Client Company Signatory");
            writer.println("  (" + SERVICE_NAME + ")");
            writer.println();
            writer.println("  _______________________          _______________________");
            writer.println("  Signature & Stamp                Signature & Stamp");
            writer.println();
            writer.println("============================================================");
            writer.printf("  Generated Timestamp: %s%n", new Date());
            writer.println("============================================================");

            System.out.println("Text Invoice generated: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("InvoiceGenerator.generateTextInvoice error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Automatically saves both .pdf and .txt invoice files to the invoices/ directory.
     */
    public static void autoSaveInvoice(Invoice invoice, List<Trip> trips) {
        File invoiceDir = new File("invoices");
        if (!invoiceDir.exists()) invoiceDir.mkdirs();

        String baseName = "invoices/" + invoice.getInvoiceNumber() + "_MOT_Logistics";
        generatePDF(invoice, trips, baseName + ".pdf");
        generateTextInvoice(invoice, trips, baseName + ".txt");
    }
}
