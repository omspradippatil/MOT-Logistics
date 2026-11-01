-- ============================================================
-- MOT Logistics - Database Schema & Rich Seed Data
-- ============================================================

CREATE DATABASE IF NOT EXISTS transport_db;
USE transport_db;

-- -----------------------------------------------
-- Table: admin
-- Stores admin login credentials
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS admin (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    password    VARCHAR(100) NOT NULL,
    full_name   VARCHAR(100)
);

-- -----------------------------------------------
-- Table: company
-- Stores companies that MOT Logistics serves
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS company (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    company_name    VARCHAR(100) NOT NULL,
    address         VARCHAR(200),
    contact_number  VARCHAR(15),
    gst_number      VARCHAR(20),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------
-- Table: driver
-- Stores driver info and login credentials
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS driver (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    driver_name     VARCHAR(100) NOT NULL,
    mobile_number   VARCHAR(15),
    licence_number  VARCHAR(20),
    username        VARCHAR(50) UNIQUE NOT NULL,
    password        VARCHAR(100) NOT NULL,
    is_active       TINYINT(1) DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------
-- Table: vehicle
-- Stores vehicle info and driver assignment
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS vehicle (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    registration_number VARCHAR(20) UNIQUE NOT NULL,
    vehicle_type        VARCHAR(50),
    driver_id           INT,
    status              VARCHAR(20) DEFAULT 'Active',
    FOREIGN KEY (driver_id) REFERENCES driver(id) ON DELETE SET NULL
);

-- -----------------------------------------------
-- Table: trip
-- Stores daily trip records entered by drivers
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS trip (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    trip_code           VARCHAR(20) UNIQUE NOT NULL,
    trip_date           DATE NOT NULL,
    company_id          INT NOT NULL,
    driver_id           INT NOT NULL,
    vehicle_id          INT NOT NULL,
    source_location     VARCHAR(100) NOT NULL,
    destination         VARCHAR(100) NOT NULL,
    amount              DECIMAL(10,2) NOT NULL,
    remarks             VARCHAR(200),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id),
    FOREIGN KEY (driver_id)  REFERENCES driver(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id)
);

-- -----------------------------------------------
-- Table: invoice
-- Stores generated monthly invoices
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS invoice (
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
    status          VARCHAR(20) DEFAULT 'Generated',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id)
);

-- -----------------------------------------------
-- Table: payment
-- Stores payment status for each invoice
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS payment (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id       INT NOT NULL UNIQUE,
    total_amount     DECIMAL(10,2) NOT NULL,
    amount_received  DECIMAL(10,2) DEFAULT 0.00,
    remaining_amount DECIMAL(10,2) DEFAULT 0.00,
    payment_date     DATE,
    payment_status   VARCHAR(20) DEFAULT 'Pending',
    notes            VARCHAR(200),
    FOREIGN KEY (invoice_id) REFERENCES invoice(id)
);

-- -----------------------------------------------
-- 1. Default Admin Account
-- username: admin | password: admin123
-- -----------------------------------------------
INSERT INTO admin (username, password, full_name)
VALUES ('admin', 'admin123', 'System Administrator')
ON DUPLICATE KEY UPDATE full_name='System Administrator';

-- -----------------------------------------------
-- 2. Sample Client Companies
-- -----------------------------------------------
INSERT INTO company (id, company_name, address, contact_number, gst_number) VALUES
(1, 'Company A', 'Plot 42, Wagle Estate, Thane, Maharashtra', '9876543210', '27AABCT1234F1Z5'),
(2, 'Tata Motors Logistics', 'MIDC Industrial Area, Pune, Maharashtra', '9822012345', '27AAACT2727Q1ZW'),
(3, 'Reliance Retail Supply Hub', 'Logistics Park, Bhiwandi, Maharashtra', '9819056789', '27AABCR8901N1ZK'),
(4, 'Mahindra Logistics Ltd', 'Goregaon East, Mumbai, Maharashtra', '9833098765', '27AABCM5432P1ZX'),
(5, 'Flipkart Wholesale Depot', 'Kalyan-Bhiwandi Road, Thane', '9821034567', '27AABCF9876M1ZQ'),
(6, 'Godrej Consumer Products', 'Vikhroli West, Mumbai, Maharashtra', '9811076543', '27AAACG1122D1ZT')
ON DUPLICATE KEY UPDATE company_name=VALUES(company_name);

-- -----------------------------------------------
-- 3. Sample Drivers (all password: <username>123)
-- -----------------------------------------------
INSERT INTO driver (id, driver_name, mobile_number, licence_number, username, password, is_active) VALUES
(1, 'Rahul Sharma', '9876500001', 'MH0420180012345', 'rahul', 'rahul123', 1),
(2, 'Amit Singh', '9876500002', 'MH1220190023456', 'amit', 'amit123', 1),
(3, 'Vikram Shinde', '9876500003', 'MH4320200034567', 'vikram', 'vikram123', 1),
(4, 'Deepak Yadav', '9876500004', 'MH0220210045678', 'deepak', 'deepak123', 1),
(5, 'Ramesh Patil', '9876500005', 'MH4620170056789', 'ramesh', 'ramesh123', 1)
ON DUPLICATE KEY UPDATE driver_name=VALUES(driver_name);

-- -----------------------------------------------
-- 4. Sample Fleet Vehicles
-- -----------------------------------------------
INSERT INTO vehicle (id, registration_number, vehicle_type, driver_id, status) VALUES
(1, 'MH-48-AB-1234', 'Tempo', 1, 'Active'),
(2, 'MH-04-CD-5678', 'Pickup Van', 2, 'Active'),
(3, 'MH-12-EF-9012', 'Light Commercial Truck', 3, 'Active'),
(4, 'MH-43-GH-3456', 'Heavy Truck', 4, 'Active'),
(5, 'MH-46-JK-7890', 'Trailer', 5, 'Active'),
(6, 'MH-02-XY-4321', 'Tempo', 1, 'Active')
ON DUPLICATE KEY UPDATE vehicle_type=VALUES(vehicle_type);

-- -----------------------------------------------
-- 5. Sample Daily Trip Records
-- (October 2026 & November 2026)
-- -----------------------------------------------
INSERT INTO trip (id, trip_code, trip_date, company_id, driver_id, vehicle_id, source_location, destination, amount, remarks) VALUES
-- Trips for Company A (Oct 2026)
(1, 'TRIP001', '2026-10-01', 1, 1, 1, 'Wada Warehouse', 'Thane Godown', 2500.00, 'Standard delivery'),
(2, 'TRIP002', '2026-10-03', 1, 1, 1, 'Thane Godown', 'Mumbai Central', 3000.00, 'Express distribution'),
(3, 'TRIP003', '2026-10-05', 1, 1, 1, 'Wada Warehouse', 'Bhiwandi Hub', 2000.00, 'Bulk cartons'),
(4, 'TRIP004', '2026-10-08', 1, 1, 1, 'Wada Warehouse', 'Palghar Station', 1500.00, 'Local dispatch'),

-- Trips for Tata Motors (Oct 2026)
(5, 'TRIP005', '2026-10-10', 2, 3, 3, 'Pune Plant', 'Bhiwandi Logistics Hub', 5500.00, 'Auto parts transit'),
(6, 'TRIP006', '2026-10-14', 2, 3, 3, 'Bhiwandi Logistics Hub', 'Nashik Depot', 4800.00, 'Chassis components'),
(7, 'TRIP007', '2026-10-18', 2, 4, 4, 'Pune Plant', 'JNPT Port, Navi Mumbai', 7200.00, 'Export container haulage'),

-- Trips for Reliance Retail (Oct 2026)
(8, 'TRIP008', '2026-10-12', 3, 2, 2, 'Bhiwandi Supply Hub', 'Kalyan Mall', 2200.00, 'FMCG stock delivery'),
(9, 'TRIP009', '2026-10-16', 3, 2, 2, 'Bhiwandi Supply Hub', 'Vashi APMC', 2800.00, 'Grocery replenishment'),
(10, 'TRIP010', '2026-10-22', 3, 2, 2, 'Bhiwandi Supply Hub', 'Panvel Superstore', 3100.00, 'Perishables transit'),

-- Trips for Mahindra Logistics (Oct 2026)
(11, 'TRIP011', '2026-10-20', 4, 5, 5, 'Chakan, Pune', 'JNPT Port', 8500.00, 'Heavy industrial machinery'),
(12, 'TRIP012', '2026-10-25', 4, 4, 4, 'Thane Hub', 'Vapi Industrial Zone', 6200.00, 'Steel coils haulage'),

-- Current Month Trips (November 2026)
(13, 'TRIP013', '2026-11-01', 1, 1, 1, 'Wada Warehouse', 'Thane Godown', 2500.00, 'November first run'),
(14, 'TRIP014', '2026-11-01', 2, 3, 3, 'Pune Plant', 'Bhiwandi Logistics Hub', 5500.00, 'Priority component transit'),
(15, 'TRIP015', '2026-11-01', 5, 2, 2, 'Bhiwandi Wholesale', 'Dadar West', 3200.00, 'Apparel crates dispatch')
ON DUPLICATE KEY UPDATE amount=VALUES(amount);

-- -----------------------------------------------
-- 6. Sample Generated Invoices
-- -----------------------------------------------
INSERT INTO invoice (id, invoice_number, invoice_date, company_id, driver_id, vehicle_id, billing_month, billing_year, total_trips, total_amount, status) VALUES
(1, 'INV-0001', '2026-10-31', 1, 1, 1, 10, 2026, 4, 9000.00, 'Generated'),
(2, 'INV-0002', '2026-10-31', 2, 3, 3, 10, 2026, 3, 17500.00, 'Generated'),
(3, 'INV-0003', '2026-10-31', 3, 2, 2, 10, 2026, 3, 8100.00, 'Generated')
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

-- -----------------------------------------------
-- 7. Sample Invoice Payments
-- -----------------------------------------------
INSERT INTO payment (id, invoice_id, total_amount, amount_received, remaining_amount, payment_date, payment_status, notes) VALUES
(1, 1, 9000.00, 9000.00, 0.00, '2026-10-31', 'Paid', 'NEFT Ref #TXN98721345 settled in full'),
(2, 2, 17500.00, 10000.00, 7500.00, '2026-10-31', 'Partially Paid', 'Cheque #432101 cleared. Balance due in 15 days'),
(3, 3, 8100.00, 0.00, 8100.00, NULL, 'Pending', 'Invoice dispatched. Net-30 payment terms')
ON DUPLICATE KEY UPDATE amount_received=VALUES(amount_received);
