-- ============================================================
-- MOT TRANS - Transport Trip Management & Automated Invoice Generation System
-- Multi-Tenant Database Schema & Seed Data
-- ============================================================

CREATE DATABASE IF NOT EXISTS transport_db;
USE transport_db;

-- ------------------------------------------------------------
-- Drop existing tables to establish clean updated relational structure
-- ------------------------------------------------------------
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS trip;
DROP TABLE IF EXISTS vehicle;
DROP TABLE IF EXISTS driver;
DROP TABLE IF EXISTS company;
DROP TABLE IF EXISTS admin;

-- ------------------------------------------------------------
-- Table: company (Company Admin Tenants)
-- Each company has its own independent admin login
-- ------------------------------------------------------------
CREATE TABLE company (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    company_name    VARCHAR(100) NOT NULL UNIQUE,
    admin_name      VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(100) NOT NULL,
    contact_number  VARCHAR(20),
    address         VARCHAR(200),
    gst_number      VARCHAR(20),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Table: driver (Drivers / Vehicle Owners)
-- Each driver has an individual login account
-- ------------------------------------------------------------
CREATE TABLE driver (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    driver_name     VARCHAR(100) NOT NULL,
    mobile_number   VARCHAR(20)  NOT NULL,
    email           VARCHAR(100),
    licence_number  VARCHAR(30)  NOT NULL,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(100) NOT NULL,
    is_active       TINYINT(1)   DEFAULT 1,
    company_id      INT DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Table: vehicle (Commercial Vehicles)
-- Multiple vehicles can be operated/owned by drivers
-- ------------------------------------------------------------
CREATE TABLE vehicle (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    registration_number VARCHAR(20) UNIQUE NOT NULL,
    vehicle_type        VARCHAR(50) NOT NULL,
    driver_id           INT,
    status              VARCHAR(20) DEFAULT 'Active',
    FOREIGN KEY (driver_id) REFERENCES driver(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Table: trip (Daily Operational Trips)
-- Contains both company_id and driver_id for strict isolation
-- ------------------------------------------------------------
CREATE TABLE trip (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    trip_code           VARCHAR(20) UNIQUE NOT NULL,
    trip_date           DATE NOT NULL,
    company_id          INT NOT NULL,
    driver_id           INT NOT NULL,
    vehicle_id          INT NOT NULL,
    source_location     VARCHAR(100) NOT NULL,
    destination         VARCHAR(100) NOT NULL,
    goods_description   VARCHAR(200),
    amount              DECIMAL(10,2) NOT NULL,
    trip_status         VARCHAR(20) DEFAULT 'Completed',
    remarks             VARCHAR(200),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id)  REFERENCES driver(id)  ON DELETE RESTRICT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE RESTRICT
);

-- ------------------------------------------------------------
-- Table: invoice (Monthly Billing Invoices)
-- Supports both Company-wide and Driver-wise monthly invoices
-- ------------------------------------------------------------
CREATE TABLE invoice (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number  VARCHAR(20) UNIQUE NOT NULL,
    invoice_date    DATE NOT NULL,
    company_id      INT NOT NULL,
    driver_id       INT,
    vehicle_id      INT,
    billing_month   INT NOT NULL,
    billing_year    INT NOT NULL,
    total_trips     INT DEFAULT 0,
    total_amount    DECIMAL(10,2) DEFAULT 0.00,
    amount_paid     DECIMAL(10,2) DEFAULT 0.00,
    remaining_amount DECIMAL(10,2) DEFAULT 0.00,
    invoice_type    VARCHAR(20) DEFAULT 'Company',
    status          VARCHAR(20) DEFAULT 'Generated',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id)  REFERENCES driver(id)  ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Table: payment (Invoice Settlements & Transactions)
-- ------------------------------------------------------------
CREATE TABLE payment (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id       INT NOT NULL UNIQUE,
    company_id       INT NOT NULL,
    total_amount     DECIMAL(10,2) NOT NULL,
    amount_received  DECIMAL(10,2) DEFAULT 0.00,
    remaining_amount DECIMAL(10,2) DEFAULT 0.00,
    payment_date     DATE,
    payment_status   VARCHAR(20) DEFAULT 'Pending',
    notes            VARCHAR(200),
    FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE
);

-- ============================================================
-- SEED DATA - EXACT REQUIREMENTS FROM USER PROMPT
-- ============================================================

-- 1. SEED COMPANIES (Company Admins)
-- Login credentials for each company
INSERT INTO company (id, company_name, admin_name, email, username, password, contact_number, address, gst_number) VALUES
(1, 'Om Sai',      'Rajesh Patil',   'admin@omsai.com',  'omsai',  'omsai123',  '9822011223', 'Plot 42, MIDC Industrial Area, Wada, Maharashtra',     '27AABCO1234F1Z1'),
(2, 'Oracle',      'Sanjay Deshmukh', 'admin@oracle.com', 'oracle', 'oracle123', '9833099887', 'Cyber Park, Pokhran Road No 2, Thane West, Mumbai',    '27AAACT9876Q1Z9'),
(3, 'Company XYZ', 'Vikrant Mehta',  'admin@xyz.com',    'xyz',    'xyz123',    '9819077665', 'Building B, Logistics Super Hub, Bhiwandi, Maharashtra', '27AABCX5566M1Z4');

-- 2. SEED DRIVERS (Vehicle Owners & Operators)
INSERT INTO driver (id, driver_name, mobile_number, email, licence_number, username, password, is_active) VALUES
(1, 'Rahul', '9876500001', 'rahul@transport.com', 'MH4820180012345', 'rahul', 'rahul123', 1),
(2, 'Amit',  '9876500002', 'amit@transport.com',  'MH4820190023456', 'amit',  'amit123',  1),
(3, 'Sagar', '9876500003', 'sagar@transport.com', 'MH4820200034567', 'sagar', 'sagar123', 1);

-- 3. SEED VEHICLES
INSERT INTO vehicle (id, registration_number, vehicle_type, driver_id, status) VALUES
(1, 'MH-48-AB-1234', 'Tempo',       1, 'Active'),
(2, 'MH-02-XY-4321', 'Pickup Van',  1, 'Active'),
(3, 'MH-48-CD-5678', 'Pickup Van',  2, 'Active'),
(4, 'MH-48-EF-9012', 'Heavy Truck', 3, 'Active'),
(5, 'MH-04-GH-7890', 'Trailer',     3, 'Active');

-- 4. SEED TRIPS
-- Exactly matching prompt examples: Rahul works for Om Sai AND Oracle; Amit & Sagar work for Om Sai, Oracle, XYZ.
INSERT INTO trip (id, trip_code, trip_date, company_id, driver_id, vehicle_id, source_location, destination, goods_description, amount, trip_status, remarks) VALUES
-- Rahul's trips for Om Sai
(1,  'TRIP001', '2026-11-01', 1, 1, 1, 'Wada',     'Bhiwandi', 'Machinery Spare Parts',    2500.00, 'Completed', 'On-time delivery'),
(2,  'TRIP003', '2026-11-05', 1, 1, 1, 'Wada',     'Palghar',  'Raw Material Barrels',     2000.00, 'Completed', 'Standard run'),
(3,  'TRIP005', '2026-11-10', 1, 1, 1, 'Wada',     'Thane',    'Electrical Switchgear',    2800.00, 'Completed', 'Fragile cargo handled safely'),

-- Rahul's trips for Oracle
(4,  'TRIP002', '2026-11-03', 2, 1, 1, 'Thane',    'Mumbai',   'Server Rack Assemblies',   3000.00, 'Completed', 'High priority delivery'),
(5,  'TRIP004', '2026-11-08', 2, 1, 1, 'Mumbai',   'Thane',    'Computer Hardware Crates', 1500.00, 'Completed', 'Return haul'),
(6,  'TRIP006', '2026-11-12', 2, 1, 2, 'Thane',    'Navi Mumbai','Office Workstations',    2200.00, 'Completed', 'Bulk cargo'),

-- Amit's trips
(7,  'TRIP007', '2026-11-02', 1, 2, 3, 'Bhiwandi', 'Wada',     'Packaging Materials',      2200.00, 'Completed', 'Bulk delivery'),
(8,  'TRIP008', '2026-11-06', 2, 2, 3, 'Thane',    'Vashi',    'Networking Equipment',     2400.00, 'Completed', 'Secure transport'),
(9,  'TRIP009', '2026-11-09', 3, 2, 3, 'Bhiwandi', 'Pune',     'FMCG Wholesale Pallets',   5500.00, 'Completed', 'Long haul run'),

-- Sagar's trips
(10, 'TRIP010', '2026-11-04', 1, 3, 4, 'Wada',     'JNPT Port','Heavy Industrial Coils',   6500.00, 'Completed', 'Port container clearance'),
(11, 'TRIP011', '2026-11-07', 2, 3, 4, 'Thane',    'Pune',     'Datacenter Cooling Units', 5800.00, 'Completed', 'Escorted heavy haul'),
(12, 'TRIP012', '2026-11-11', 3, 3, 5, 'Bhiwandi', 'Nashik',   'Automotive Stamping Dies', 4800.00, 'Completed', 'Inter-city haulage');

-- 5. SEED INVOICES & PAYMENTS
-- Company-wise invoice for Om Sai (November 2026)
INSERT INTO invoice (id, invoice_number, invoice_date, company_id, driver_id, vehicle_id, billing_month, billing_year, total_trips, total_amount, amount_paid, remaining_amount, invoice_type, status) VALUES
(1, 'INV-2026-001', '2026-11-15', 1, NULL, NULL, 11, 2026, 5, 16000.00, 10000.00, 6000.00, 'Company', 'Partially Paid'),
(2, 'INV-2026-002', '2026-11-15', 2, NULL, NULL, 11, 2026, 5, 14900.00, 14900.00, 0.00,    'Company', 'Paid');

-- Payment records for the seeded invoices
INSERT INTO payment (id, invoice_id, company_id, total_amount, amount_received, remaining_amount, payment_date, payment_status, notes) VALUES
(1, 1, 1, 16000.00, 10000.00, 6000.00, '2026-11-15', 'Partially Paid', 'Part payment received via RTGS #98124'),
(2, 2, 2, 14900.00, 14900.00, 0.00,    '2026-11-15', 'Paid',           'Settled in full via Corporate Transfer');
