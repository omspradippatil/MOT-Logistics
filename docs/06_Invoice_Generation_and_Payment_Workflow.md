# 🧾 Document 6: Invoice Generation & Payment Workflow

This document explains the core automated billing engine, detailing how raw trip records are collected, aggregated, transformed into monthly invoices, and tracked through the payment lifecycle.

---

## 1. End-to-End Billing Lifecycle

```
[ Field Trips Entered Daily by Drivers ]
                   │
                   ▼ (Stored in MySQL 'trip' table)
┌──────────────────────────────────────────────────────────┐
│                   Admin selects:                         │
│  - Target Company (e.g., "Company A")                    │
│  - Billing Month & Year (e.g., "November 2026")          │
│  - Optional: Specific Driver or Vehicle                  │
└──────────────────────────┬───────────────────────────────┘
                           │
                           ▼ Click "Generate Invoice"
┌──────────────────────────────────────────────────────────┐
│           SQL Dynamic Retrieval & Aggregation            │
│  SELECT * FROM trip WHERE company_id = ? AND             │
│  MONTH(trip_date) = ? AND YEAR(trip_date) = ?            │
└──────────────────────────┬───────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│              Mathematical Calculation Loop               │
│         totalAmount = SUM(trip.getAmount())              │
│         totalTrips  = trips.size()                       │
└──────────────────────────┬───────────────────────────────┘
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
┌─────────────────────────┐ ┌─────────────────────────┐
│ Insert 'invoice' record │ │ Insert 'payment' record │
│  - Invoice No: INV-0001 │ │  - Status: "Pending"    │
│  - Total Amount: ₹9,000 │ │  - Received: ₹0.00      │
│  - Status: "Generated"  │ │  - Remaining: ₹9,000    │
└─────────────────────────┘ └─────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────────────────────┐
│   Instant JTable Preview Rendered in InvoicePanel        │
└──────────────────────────┬───────────────────────────────┘
                           │
                           ▼ Click "Export PDF / Invoice"
┌──────────────────────────────────────────────────────────┐
│      Print-Ready File Written to invoices/INV-0001.txt   │
└──────────────────────────────────────────────────────────┘
```

---

## 2. Sample Generated Invoice Format

When exported via [`InvoiceGenerator.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/util/InvoiceGenerator.java), the file output adheres to standard Indian transport billing layouts:

```text
============================================================
              SBP KUBRE TEMPO SERVICE
               TRANSPORT SERVICE INVOICE
============================================================

Invoice Number  : INV-0001
Invoice Date    : 30/11/2026
------------------------------------------------------------
Company Name    : Company A
Driver Name     : Rahul Sharma
Vehicle Number  : MH-48-AB-1234
Billing Period  : November 2026
------------------------------------------------------------

Sr.No  Date         Source             Destination          Amount (Rs.)
------------------------------------------------------------
1      01/11/2026   Wada               Thane                     2500.00
2      03/11/2026   Thane              Mumbai                    3000.00
3      05/11/2026   Wada               Bhiwandi                  2000.00
4      08/11/2026   Wada               Palghar                   1500.00
------------------------------------------------------------
                                       Total Trips:                    4
                                       TOTAL AMOUNT:             9000.00
============================================================

  Authorized Signatory                  Company Signatory
  (SBP Kubre Tempo Service)

  _______________________          _______________________
  Signature & Stamp                Signature & Stamp

============================================================
  Generated on: Sun Nov 01 15:40:00 IST 2026
============================================================
```

---

## 3. Payment Settlement Workflow

Every invoice automatically generates an associated payment entry in the `payment` table with initial state:
- `total_amount = totalAmount`
- `amount_received = 0.00`
- `remaining_amount = totalAmount`
- `payment_status = "Pending"`

### How the Admin Updates Payments:
1. Open the **💰 Payments** tab.
2. Select the invoice row in the table (which is highlighted in soft red).
3. In the form at the top, enter the `Amount Received (Rs.)` (e.g. `5000.00`).
4. Select the status:
   - If partial: choose `Partially Paid` (table row turns yellow, remaining is `4000.00`).
   - If full: enter `9000.00` and choose `Paid` (table row turns green, remaining is `0.00`).
5. Click **Update Payment**.
6. The updated status and balances are immediately synchronized with the database and reflected in the dashboard pending balances.
