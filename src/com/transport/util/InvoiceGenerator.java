package com.transport.util;

import com.transport.model.Invoice;
import com.transport.model.Trip;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * InvoiceGenerator.java - Native PDF & Text Invoice/Receipt Engine for MOT Logistics
 *
 * Pure Core Java implementation (no third-party JARs required).
 * Generates standards-compliant PDF 1.4 documents (invoices, receipts, and summary ledgers)
 * and formatted text/CSV vouchers.
 */
public class InvoiceGenerator {

    private static final String SERVICE_NAME = "MOT Logistics";

    private static final String[] MONTH_NAMES = {
        "All Months", "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    /** Get user-friendly billing period text */
    public static String getBillingPeriodText(int month, int year) {
        if (month <= 0 || month > 12) {
            return "All Months (Annual Statement " + year + ")";
        }
        return MONTH_NAMES[month] + " " + year;
    }

    /**
     * Generate a multi-page, genuine standard PDF 1.4 invoice document.
     * Compatible with Adobe Acrobat, Apple Preview, Google Chrome, and web browsers.
     *
     * @param invoice  The invoice metadata object
     * @param trips    The itemized list of trips (handles any number of trips across pages)
     * @param filePath The destination file path (.pdf)
     * @return true if successful, false on error
     */
    public static boolean generatePDF(Invoice invoice, List<Trip> trips, String filePath) {
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }

        ensureDirectoryExists(filePath);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
            List<Integer> offsets = new ArrayList<>();
            offsets.add(0);

            // Calculate pagination
            int tripsPerPage = 20;
            int totalTrips = (trips != null) ? trips.size() : 0;
            int totalPages = totalTrips <= tripsPerPage ? 1 : (int) Math.ceil((double) totalTrips / tripsPerPage);
            if (totalPages == 0) totalPages = 1;

            // 1. PDF Header
            pdfStream.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));

            // Catalog (Obj 1)
            offsets.add(pdfStream.size());
            pdfStream.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Pages (Obj 2)
            StringBuilder kids = new StringBuilder();
            for (int p = 0; p < totalPages; p++) {
                kids.append((3 + p * 2)).append(" 0 R ");
            }
            offsets.add(pdfStream.size());
            pdfStream.write(String.format("2 0 obj\n<< /Type /Pages /Kids [%s] /Count %d >>\nendobj\n",
                    kids.toString().trim(), totalPages).getBytes(StandardCharsets.ISO_8859_1));

            int font1Obj = 3 + totalPages * 2;
            int font2Obj = font1Obj + 1;

            double grandTotal = 0.0;
            if (trips != null) {
                for (Trip t : trips) grandTotal += t.getAmount();
            }

            int tripIndex = 0;

            for (int p = 0; p < totalPages; p++) {
                int pageObj = 3 + p * 2;
                int contentObj = pageObj + 1;

                StringBuilder sb = new StringBuilder();

                // Top Navy Brand Header Banner
                sb.append("0.06 0.15 0.33 rg\n");
                sb.append("40 760 515 52 re f\n");

                sb.append("1 1 1 rg\n");
                sb.append("BT /F1 18 Tf 55 790 Td (MOT LOGISTICS) Tj ET\n");
                sb.append("BT /F2 9 Tf 55 772 Td (COMMERCIAL TRANSPORT FLEET MANAGEMENT & BILLING SYSTEM) Tj ET\n");

                // Page Indicator
                sb.append("BT /F2 8 Tf 480 772 Td (Page ").append(p + 1).append(" of ").append(totalPages).append(") Tj ET\n");

                // Invoice Header Details Grid
                sb.append("0.1 0.1 0.1 rg\n");
                sb.append("BT /F1 10 Tf 45 734 Td (Invoice Number:) Tj ET\n");
                sb.append("BT /F2 10 Tf 140 734 Td (").append(cleanPdfText(invoice.getInvoiceNumber())).append(") Tj ET\n");

                sb.append("BT /F1 10 Tf 45 716 Td (Invoice Date:) Tj ET\n");
                sb.append("BT /F2 10 Tf 140 716 Td (").append(sdf.format(invoice.getInvoiceDate())).append(") Tj ET\n");

                String periodText = getBillingPeriodText(invoice.getBillingMonth(), invoice.getBillingYear());
                sb.append("BT /F1 10 Tf 45 698 Td (Billing Period:) Tj ET\n");
                sb.append("BT /F2 10 Tf 140 698 Td (").append(cleanPdfText(periodText)).append(") Tj ET\n");

                sb.append("BT /F1 10 Tf 315 734 Td (Billed To:) Tj ET\n");
                sb.append("BT /F2 10 Tf 385 734 Td (").append(cleanPdfText(invoice.getCompanyName())).append(") Tj ET\n");

                String driverText = (invoice.getDriverName() != null && !invoice.getDriverName().isEmpty())
                        ? invoice.getDriverName() : "All / Fleet Operations";
                sb.append("BT /F1 10 Tf 315 716 Td (Driver:) Tj ET\n");
                sb.append("BT /F2 10 Tf 385 716 Td (").append(cleanPdfText(driverText)).append(") Tj ET\n");

                String vehicleText = (invoice.getVehicleNumber() != null && !invoice.getVehicleNumber().isEmpty())
                        ? invoice.getVehicleNumber() : "Fleet Operations";
                sb.append("BT /F1 10 Tf 315 698 Td (Vehicle:) Tj ET\n");
                sb.append("BT /F2 10 Tf 385 698 Td (").append(cleanPdfText(vehicleText)).append(") Tj ET\n");

                // Rule
                sb.append("0.85 0.88 0.92 rg\n");
                sb.append("40 682 515 1 re f\n");

                // Itemized Table Header Bar
                sb.append("0.93 0.95 0.98 rg\n");
                sb.append("40 652 515 22 re f\n");
                sb.append("0.06 0.15 0.33 rg\n");
                sb.append("BT /F1 9 Tf 46 659 Td (Sr.) Tj ET\n");
                sb.append("BT /F1 9 Tf 68 659 Td (Trip Date) Tj ET\n");
                sb.append("BT /F1 9 Tf 130 659 Td (Driver Name) Tj ET\n");
                sb.append("BT /F1 9 Tf 205 659 Td (Vehicle Reg.) Tj ET\n");
                sb.append("BT /F1 9 Tf 285 659 Td (Route (From -> To)) Tj ET\n");
                sb.append("BT /F1 9 Tf 468 659 Td (Amount (INR)) Tj ET\n");

                int y = 632;
                int pageTripsRendered = 0;

                while (trips != null && tripIndex < trips.size() && pageTripsRendered < tripsPerPage) {
                    Trip trip = trips.get(tripIndex);

                    // Row divider line
                    sb.append("0.92 0.94 0.96 rg\n");
                    sb.append("40 ").append(y - 5).append(" 515 0.5 re f\n");

                    String dName = trip.getDriverName() != null ? trip.getDriverName() : "-";
                    String vNo   = trip.getVehicleNumber() != null ? trip.getVehicleNumber() : "-";
                    String route = trip.getSourceLocation() + " -> " + trip.getDestination();
                    if (route.length() > 28) route = route.substring(0, 26) + "..";

                    sb.append("0.15 0.15 0.15 rg\n");
                    sb.append("BT /F2 8.5 Tf 46 ").append(y).append(" Td (").append(tripIndex + 1).append(") Tj ET\n");
                    sb.append("BT /F2 8.5 Tf 68 ").append(y).append(" Td (").append(sdf.format(trip.getTripDate())).append(") Tj ET\n");
                    sb.append("BT /F2 8.5 Tf 130 ").append(y).append(" Td (").append(cleanPdfText(dName)).append(") Tj ET\n");
                    sb.append("BT /F2 8.5 Tf 205 ").append(y).append(" Td (").append(cleanPdfText(vNo)).append(") Tj ET\n");
                    sb.append("BT /F2 8.5 Tf 285 ").append(y).append(" Td (").append(cleanPdfText(route)).append(") Tj ET\n");
                    sb.append("BT /F2 8.5 Tf 468 ").append(y).append(" Td (").append(String.format("%,.2f", trip.getAmount())).append(") Tj ET\n");

                    y -= 20;
                    tripIndex++;
                    pageTripsRendered++;
                }

                // If last page, render Summary & Signatures
                if (p == totalPages - 1) {
                    y -= 6;
                    sb.append("0.95 0.97 0.99 rg\n");
                    sb.append("40 ").append(y - 8).append(" 515 26 re f\n");

                    sb.append("0.06 0.15 0.33 rg\n");
                    sb.append("BT /F1 10 Tf 50 ").append(y).append(" Td (Total Completed Trips: ").append(totalTrips).append(") Tj ET\n");
                    sb.append("BT /F1 11 Tf 340 ").append(y).append(" Td (TOTAL PAYABLE: INR ").append(String.format("%,.2f", grandTotal)).append(") Tj ET\n");

                    int sigY = Math.max(90, y - 80);
                    sb.append("0.2 0.2 0.2 rg\n");
                    sb.append("40 ").append(sigY + 30).append(" 180 1 re f\n");
                    sb.append("BT /F1 9 Tf 40 ").append(sigY + 15).append(" Td (Authorized Signatory) Tj ET\n");
                    sb.append("BT /F2 8 Tf 40 ").append(sigY + 3).append(" Td (MOT Logistics) Tj ET\n");

                    sb.append("375 ").append(sigY + 30).append(" 180 1 re f\n");
                    sb.append("BT /F1 9 Tf 375 ").append(sigY + 15).append(" Td (Client Signatory) Tj ET\n");
                    sb.append("BT /F2 8 Tf 375 ").append(sigY + 3).append(" Td (").append(cleanPdfText(invoice.getCompanyName())).append(") Tj ET\n");
                }

                // Footer
                sb.append("0.5 0.5 0.5 rg\n");
                sb.append("40 45 515 0.5 re f\n");
                sb.append("BT /F2 8 Tf 40 32 Td (Generated on: ").append(new Date().toString())
                  .append(" | Computer generated invoice by MOT Logistics) Tj ET\n");

                byte[] streamBytes = sb.toString().getBytes(StandardCharsets.ISO_8859_1);

                // Write Page Object
                offsets.add(pdfStream.size());
                pdfStream.write(String.format("%d 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents %d 0 R /Resources << /Font << /F1 %d 0 R /F2 %d 0 R >> >> >>\nendobj\n",
                        pageObj, contentObj, font1Obj, font2Obj).getBytes(StandardCharsets.ISO_8859_1));

                // Write Content Stream Object
                offsets.add(pdfStream.size());
                pdfStream.write(String.format("%d 0 obj\n<< /Length %d >>\nstream\n", contentObj, streamBytes.length).getBytes(StandardCharsets.ISO_8859_1));
                pdfStream.write(streamBytes);
                pdfStream.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            }

            // Fonts
            offsets.add(pdfStream.size());
            pdfStream.write(String.format("%d 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n", font1Obj).getBytes(StandardCharsets.ISO_8859_1));

            offsets.add(pdfStream.size());
            pdfStream.write(String.format("%d 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n", font2Obj).getBytes(StandardCharsets.ISO_8859_1));

            // Cross-reference Table (xref)
            int xrefStart = pdfStream.size();
            pdfStream.write(String.format("xref\n0 %d\n0000000000 65535 f \n", offsets.size()).getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i < offsets.size(); i++) {
                pdfStream.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
            }

            // Trailer & EOF
            pdfStream.write(String.format("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n",
                    offsets.size(), xrefStart).getBytes(StandardCharsets.ISO_8859_1));

            // Output to file
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

    /**
     * Generate an official Single Trip Receipt / Cash Voucher in PDF format.
     */
    public static boolean generateTripReceiptPDF(Trip trip, String filePath) {
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }
        ensureDirectoryExists(filePath);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
            List<Integer> offsets = new ArrayList<>();
            offsets.add(0);

            pdfStream.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));

            // Catalog (Obj 1)
            offsets.add(pdfStream.size());
            pdfStream.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Pages (Obj 2)
            offsets.add(pdfStream.size());
            pdfStream.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Page (Obj 3)
            offsets.add(pdfStream.size());
            pdfStream.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            StringBuilder sb = new StringBuilder();

            // Brand Banner
            sb.append("0.06 0.15 0.33 rg\n");
            sb.append("40 760 515 52 re f\n");
            sb.append("1 1 1 rg\n");
            sb.append("BT /F1 18 Tf 55 790 Td (MOT LOGISTICS) Tj ET\n");
            sb.append("BT /F2 9 Tf 55 772 Td (OFFICIAL TRIP RECEIPT & DELIVERY VOUCHER) Tj ET\n");

            // Receipt Box
            sb.append("0.96 0.97 0.99 rg\n");
            sb.append("40 440 515 300 re f\n");
            sb.append("0.8 0.85 0.9 rg\n");
            sb.append("40 440 515 300 re s\n");

            sb.append("0.1 0.1 0.1 rg\n");
            sb.append("BT /F1 12 Tf 55 715 Td (Receipt No: REC-").append(cleanPdfText(trip.getTripCode())).append(") Tj ET\n");
            sb.append("BT /F1 10 Tf 370 715 Td (Trip Date: ").append(sdf.format(trip.getTripDate())).append(") Tj ET\n");

            int y = 670;
            String[][] fields = {
                {"Trip Reference Code", trip.getTripCode()},
                {"Client / Consignor",  trip.getCompanyName() != null ? trip.getCompanyName() : "Direct Cargo Client"},
                {"Driver In-Charge",     trip.getDriverName() != null ? trip.getDriverName() : "Fleet Driver"},
                {"Vehicle Reg. Number", trip.getVehicleNumber() != null ? trip.getVehicleNumber() : "Fleet Commercial Vehicle"},
                {"Origin (Pickup)",     trip.getSourceLocation()},
                {"Destination (Drop)",  trip.getDestination()},
                {"Remarks / Consignment Notes", trip.getRemarks() != null && !trip.getRemarks().isEmpty() ? trip.getRemarks() : "Commercial logistics transit completed"}
            };

            for (String[] row : fields) {
                sb.append("BT /F1 10 Tf 55 ").append(y).append(" Td (").append(cleanPdfText(row[0])).append(":) Tj ET\n");
                sb.append("BT /F2 10 Tf 220 ").append(y).append(" Td (").append(cleanPdfText(row[1])).append(") Tj ET\n");
                sb.append("0.88 0.9 0.94 rg\n");
                sb.append("55 ").append(y - 6).append(" 480 0.5 re f\n");
                sb.append("0.1 0.1 0.1 rg\n");
                y -= 26;
            }

            // Fare Amount Highlight Banner
            y -= 10;
            sb.append("0.06 0.15 0.33 rg\n");
            sb.append("55 ").append(y - 12).append(" 480 34 re f\n");
            sb.append("1 1 1 rg\n");
            sb.append("BT /F1 12 Tf 70 ").append(y).append(" Td (TOTAL TRIP FARE / VOUCHER AMOUNT:) Tj ET\n");
            sb.append("BT /F1 14 Tf 370 ").append(y).append(" Td (INR ").append(String.format("%,.2f", trip.getAmount())).append(") Tj ET\n");

            // Signatures
            int sigY = 320;
            sb.append("0.2 0.2 0.2 rg\n");
            sb.append("55 ").append(sigY + 30).append(" 180 1 re f\n");
            sb.append("BT /F1 9 Tf 55 ").append(sigY + 15).append(" Td (Driver Signature) Tj ET\n");
            sb.append("BT /F2 8 Tf 55 ").append(sigY + 3).append(" Td (").append(cleanPdfText(trip.getDriverName())).append(") Tj ET\n");

            sb.append("355 ").append(sigY + 30).append(" 180 1 re f\n");
            sb.append("BT /F1 9 Tf 355 ").append(sigY + 15).append(" Td (Client / Receiver Acknowledgment) Tj ET\n");
            sb.append("BT /F2 8 Tf 355 ").append(sigY + 3).append(" Td (Signature & Official Stamp) Tj ET\n");

            // Footer
            sb.append("0.5 0.5 0.5 rg\n");
            sb.append("40 50 515 0.5 re f\n");
            sb.append("BT /F2 8 Tf 40 38 Td (Generated on: ").append(new Date().toString())
              .append(" | Official electronic voucher by MOT Logistics) Tj ET\n");

            byte[] streamBytes = sb.toString().getBytes(StandardCharsets.ISO_8859_1);

            // Content Stream (Obj 4)
            offsets.add(pdfStream.size());
            pdfStream.write(String.format("4 0 obj\n<< /Length %d >>\nstream\n", streamBytes.length).getBytes(StandardCharsets.ISO_8859_1));
            pdfStream.write(streamBytes);
            pdfStream.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Fonts (Obj 5, 6)
            offsets.add(pdfStream.size());
            pdfStream.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            offsets.add(pdfStream.size());
            pdfStream.write("6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Xref & Trailer
            int xrefStart = pdfStream.size();
            pdfStream.write(String.format("xref\n0 %d\n0000000000 65535 f \n", offsets.size()).getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i < offsets.size(); i++) {
                pdfStream.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
            }
            pdfStream.write(String.format("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n", offsets.size(), xrefStart).getBytes(StandardCharsets.ISO_8859_1));

            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                pdfStream.writeTo(fos);
            }
            return true;

        } catch (Exception e) {
            System.err.println("InvoiceGenerator.generateTripReceiptPDF error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate an official Trip Report PDF containing all filtered trips.
     */
    public static boolean generateTripsSummaryPDF(List<Trip> trips, String title, String subtitle, String filePath) {
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }
        ensureDirectoryExists(filePath);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
            List<Integer> offsets = new ArrayList<>();
            offsets.add(0);

            int tripsPerPage = 22;
            int totalTrips = (trips != null) ? trips.size() : 0;
            int totalPages = totalTrips <= tripsPerPage ? 1 : (int) Math.ceil((double) totalTrips / tripsPerPage);
            if (totalPages == 0) totalPages = 1;

            pdfStream.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));

            offsets.add(pdfStream.size());
            pdfStream.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            StringBuilder kids = new StringBuilder();
            for (int p = 0; p < totalPages; p++) kids.append((3 + p * 2)).append(" 0 R ");
            offsets.add(pdfStream.size());
            pdfStream.write(String.format("2 0 obj\n<< /Type /Pages /Kids [%s] /Count %d >>\nendobj\n", kids.toString().trim(), totalPages).getBytes(StandardCharsets.ISO_8859_1));

            int font1Obj = 3 + totalPages * 2;
            int font2Obj = font1Obj + 1;

            double grandTotal = 0.0;
            if (trips != null) {
                for (Trip t : trips) grandTotal += t.getAmount();
            }

            int tripIndex = 0;

            for (int p = 0; p < totalPages; p++) {
                int pageObj = 3 + p * 2;
                int contentObj = pageObj + 1;

                StringBuilder sb = new StringBuilder();
                sb.append("0.06 0.15 0.33 rg\n");
                sb.append("40 760 515 52 re f\n");
                sb.append("1 1 1 rg\n");
                sb.append("BT /F1 16 Tf 55 790 Td (").append(cleanPdfText(title)).append(") Tj ET\n");
                sb.append("BT /F2 9 Tf 55 772 Td (").append(cleanPdfText(subtitle)).append(") Tj ET\n");
                sb.append("BT /F2 8 Tf 480 772 Td (Page ").append(p + 1).append(" of ").append(totalPages).append(") Tj ET\n");

                // Table Header
                sb.append("0.93 0.95 0.98 rg\n");
                sb.append("40 725 515 22 re f\n");
                sb.append("0.06 0.15 0.33 rg\n");
                sb.append("BT /F1 9 Tf 45 732 Td (Trip) Tj ET\n");
                sb.append("BT /F1 9 Tf 90 732 Td (Date) Tj ET\n");
                sb.append("BT /F1 9 Tf 145 732 Td (Company) Tj ET\n");
                sb.append("BT /F1 9 Tf 245 732 Td (Driver) Tj ET\n");
                sb.append("BT /F1 9 Tf 325 732 Td (From -> To) Tj ET\n");
                sb.append("BT /F1 9 Tf 465 732 Td (Amount (INR)) Tj ET\n");

                int y = 705;
                int count = 0;
                while (trips != null && tripIndex < trips.size() && count < tripsPerPage) {
                    Trip t = trips.get(tripIndex);
                    sb.append("0.92 0.94 0.96 rg\n");
                    sb.append("40 ").append(y - 4).append(" 515 0.5 re f\n");
                    sb.append("0.15 0.15 0.15 rg\n");
                    sb.append("BT /F2 8.5 Tf 45 ").append(y).append(" Td (").append(cleanPdfText(t.getTripCode())).append(") Tj ET\n");
                    sb.append("BT /F2 8.5 Tf 90 ").append(y).append(" Td (").append(sdf.format(t.getTripDate())).append(") Tj ET\n");

                    String comp = t.getCompanyName() != null ? t.getCompanyName() : "-";
                    if (comp.length() > 18) comp = comp.substring(0, 16) + "..";
                    sb.append("BT /F2 8.5 Tf 145 ").append(y).append(" Td (").append(cleanPdfText(comp)).append(") Tj ET\n");

                    String dname = t.getDriverName() != null ? t.getDriverName() : "-";
                    if (dname.length() > 14) dname = dname.substring(0, 12) + "..";
                    sb.append("BT /F2 8.5 Tf 245 ").append(y).append(" Td (").append(cleanPdfText(dname)).append(") Tj ET\n");

                    String route = t.getSourceLocation() + " -> " + t.getDestination();
                    if (route.length() > 24) route = route.substring(0, 22) + "..";
                    sb.append("BT /F2 8.5 Tf 325 ").append(y).append(" Td (").append(cleanPdfText(route)).append(") Tj ET\n");

                    sb.append("BT /F2 8.5 Tf 465 ").append(y).append(" Td (").append(String.format("%,.2f", t.getAmount())).append(") Tj ET\n");

                    y -= 19;
                    tripIndex++;
                    count++;
                }

                if (p == totalPages - 1) {
                    y -= 6;
                    sb.append("0.95 0.97 0.99 rg\n");
                    sb.append("40 ").append(y - 6).append(" 515 24 re f\n");
                    sb.append("0.06 0.15 0.33 rg\n");
                    sb.append("BT /F1 10 Tf 50 ").append(y).append(" Td (Total Completed Trips: ").append(totalTrips).append(") Tj ET\n");
                    sb.append("BT /F1 11 Tf 340 ").append(y).append(" Td (TOTAL REVENUE: INR ").append(String.format("%,.2f", grandTotal)).append(") Tj ET\n");
                }

                // Footer
                sb.append("0.5 0.5 0.5 rg\n");
                sb.append("40 45 515 0.5 re f\n");
                sb.append("BT /F2 8 Tf 40 32 Td (Generated on: ").append(new Date().toString())
                  .append(" | MOT Logistics Fleet Operations Report) Tj ET\n");

                byte[] streamBytes = sb.toString().getBytes(StandardCharsets.ISO_8859_1);

                offsets.add(pdfStream.size());
                pdfStream.write(String.format("%d 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents %d 0 R /Resources << /Font << /F1 %d 0 R /F2 %d 0 R >> >> >>\nendobj\n",
                        pageObj, contentObj, font1Obj, font2Obj).getBytes(StandardCharsets.ISO_8859_1));

                offsets.add(pdfStream.size());
                pdfStream.write(String.format("%d 0 obj\n<< /Length %d >>\nstream\n", contentObj, streamBytes.length).getBytes(StandardCharsets.ISO_8859_1));
                pdfStream.write(streamBytes);
                pdfStream.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            }

            offsets.add(pdfStream.size());
            pdfStream.write(String.format("%d 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n", font1Obj).getBytes(StandardCharsets.ISO_8859_1));
            offsets.add(pdfStream.size());
            pdfStream.write(String.format("%d 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n", font2Obj).getBytes(StandardCharsets.ISO_8859_1));

            int xrefStart = pdfStream.size();
            pdfStream.write(String.format("xref\n0 %d\n0000000000 65535 f \n", offsets.size()).getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i < offsets.size(); i++) {
                pdfStream.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
            }
            pdfStream.write(String.format("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n", offsets.size(), xrefStart).getBytes(StandardCharsets.ISO_8859_1));

            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                pdfStream.writeTo(fos);
            }
            return true;

        } catch (Exception e) {
            System.err.println("InvoiceGenerator.generateTripsSummaryPDF error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate a formatted, print-ready plain text invoice (.txt).
     */
    public static boolean generateTextInvoice(Invoice invoice, List<Trip> trips, String filePath) {
        if (!filePath.toLowerCase().endsWith(".txt")) {
            filePath += ".txt";
        }
        ensureDirectoryExists(filePath);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), StandardCharsets.UTF_8))) {

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
            writer.printf("Billing Period  : %s%n", getBillingPeriodText(invoice.getBillingMonth(), invoice.getBillingYear()));
            writer.println("---------------------------------------------------------------------------------------------------");
            writer.println();

            writer.printf("%-5s %-12s %-15s %-15s %-16s %-16s %14s%n",
                    "Sr.No", "Date", "Driver (Created By)", "Vehicle Reg.", "Source", "Destination", "Amount (INR)");
            writer.println("---------------------------------------------------------------------------------------------------");

            double totalAmount = 0;
            int sr = 1;

            if (trips != null) {
                for (Trip trip : trips) {
                    totalAmount += trip.getAmount();
                    writer.printf("%-5d %-12s %-15s %-15s %-16s %-16s %14.2f%n",
                            sr++,
                            sdf.format(trip.getTripDate()),
                            trip.getDriverName() != null ? trip.getDriverName() : "-",
                            trip.getVehicleNumber() != null ? trip.getVehicleNumber() : "-",
                            trip.getSourceLocation(),
                            trip.getDestination(),
                            trip.getAmount());
                }
            }

            writer.println("---------------------------------------------------------------------------------------------------");
            writer.printf("%-5s %-12s %-15s %-15s %-16s %-16s %14d%n",
                    "", "", "", "", "", "Total Trips:", (trips != null ? trips.size() : 0));
            writer.printf("%-5s %-12s %-15s %-15s %-16s %-16s %14.2f%n",
                    "", "", "", "", "", "TOTAL PAYABLE:", totalAmount);
            writer.println("===================================================================================================");
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

            return true;

        } catch (Exception e) {
            System.err.println("InvoiceGenerator.generateTextInvoice error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate an official Single Trip Receipt / Cash Voucher in plain text.
     */
    public static boolean generateTripReceiptText(Trip trip, String filePath) {
        if (!filePath.toLowerCase().endsWith(".txt")) {
            filePath += ".txt";
        }
        ensureDirectoryExists(filePath);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), StandardCharsets.UTF_8))) {
            writer.println("============================================================");
            writer.println("                      " + SERVICE_NAME.toUpperCase());
            writer.println("           OFFICIAL TRIP RECEIPT & DELIVERY VOUCHER");
            writer.println("============================================================");
            writer.printf("Receipt Number : REC-%s%n", trip.getTripCode());
            writer.printf("Trip Date      : %s%n", sdf.format(trip.getTripDate()));
            writer.println("------------------------------------------------------------");
            writer.printf("Client Billed  : %s%n", trip.getCompanyName() != null ? trip.getCompanyName() : "General Client");
            writer.printf("Driver Name    : %s%n", trip.getDriverName() != null ? trip.getDriverName() : "Fleet Driver");
            writer.printf("Vehicle Number : %s%n", trip.getVehicleNumber() != null ? trip.getVehicleNumber() : "Fleet Vehicle");
            writer.printf("Pickup (From)  : %s%n", trip.getSourceLocation());
            writer.printf("Drop (To)      : %s%n", trip.getDestination());
            writer.printf("Remarks        : %s%n", trip.getRemarks() != null ? trip.getRemarks() : "Standard commercial dispatch");
            writer.println("------------------------------------------------------------");
            writer.printf("VOUCHER TOTAL  : INR %.2f%n", trip.getAmount());
            writer.println("============================================================");
            writer.println();
            writer.println("  Driver Signature                      Receiver Signature");
            writer.println();
            writer.println("  _______________________          _______________________");
            writer.println();
            writer.println("============================================================");
            writer.printf("  Generated: %s%n", new Date());
            writer.println("============================================================");
            return true;
        } catch (Exception e) {
            System.err.println("InvoiceGenerator.generateTripReceiptText error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Export a list of trips into a comma-separated (CSV) spreadsheet.
     */
    public static boolean exportTripsToCSV(List<Trip> trips, String filePath) {
        if (!filePath.toLowerCase().endsWith(".csv")) {
            filePath += ".csv";
        }
        ensureDirectoryExists(filePath);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), StandardCharsets.UTF_8))) {
            writer.println("Trip ID,Trip Code,Date,Client Company,Driver Name,Vehicle Number,Source Location,Destination,Amount (INR),Remarks");
            if (trips != null) {
                for (Trip t : trips) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\"%n",
                            t.getId(),
                            escapeCsv(t.getTripCode()),
                            sdf.format(t.getTripDate()),
                            escapeCsv(t.getCompanyName()),
                            escapeCsv(t.getDriverName()),
                            escapeCsv(t.getVehicleNumber()),
                            escapeCsv(t.getSourceLocation()),
                            escapeCsv(t.getDestination()),
                            t.getAmount(),
                            escapeCsv(t.getRemarks()));
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("InvoiceGenerator.exportTripsToCSV error: " + e.getMessage());
            return false;
        }
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
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

    /** Ensures that parent directories exist before creating a file */
    private static void ensureDirectoryExists(String filePath) {
        File f = new File(filePath);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    /** Helper to escape parenthesis and backslashes in PDF text strings */
    private static String cleanPdfText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replace("₹", "INR ")
                   .replace("\n", " ")
                   .replace("\r", " ")
                   .replaceAll("[^\\x20-\\x7E]", " ");
    }
}
