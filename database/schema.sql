-- ============================================================
-- Transport Trip Management System - Database Schema
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
-- Stores companies that the transport serves
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
-- Default admin account
-- username: admin | password: admin123
-- -----------------------------------------------
INSERT IGNORE INTO admin (username, password, full_name)
VALUES ('admin', 'admin123', 'Administrator');

-- -----------------------------------------------
-- Sample data for testing
-- -----------------------------------------------
INSERT IGNORE INTO company (company_name, address, contact_number, gst_number)
VALUES ('Company A', 'Thane, Maharashtra', '9876543210', 'GST123456');

INSERT IGNORE INTO driver (driver_name, mobile_number, licence_number, username, password)
VALUES ('Rahul Sharma', '9876500001', 'MH1234567', 'rahul', 'rahul123');

INSERT IGNORE INTO vehicle (registration_number, vehicle_type, driver_id)
VALUES ('MH-48-AB-1234', 'Tempo', 1);
