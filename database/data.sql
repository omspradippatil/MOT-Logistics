-- ============================================================
-- MOT LOGISTICS - Full Production Seed Data
-- Transport Management & Automated Invoice System
-- ============================================================

USE transport_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- Clear existing data safely
-- ------------------------------------------------------------
DELETE FROM payment;
DELETE FROM invoice;
DELETE FROM trip;
DELETE FROM vehicle;
DELETE FROM driver;
DELETE FROM company;

-- ------------------------------------------------------------
-- 1. SEED COMPANIES (Enterprise Logistics Clients & Operators)
-- ------------------------------------------------------------
INSERT INTO company (id, company_name, admin_name, email, username, password, contact_number, address, gst_number) VALUES
(1, 'Om Sai Logistics & Supply Chain', 'Rajesh Patil',     'admin@omsai.com',               'omsai',    'omsai123',    '+91 98220 11223', 'Plot 42, MIDC Industrial Area, Wada, Palghar - 421303',              '27AABCO1234F1Z1'),
(2, 'Oracle Supply Chain Solutions',  'Sanjay Deshmukh',  'admin@oracle.com',              'oracle',   'oracle123',   '+91 98330 99887', 'Cyber Park, Pokhran Road No 2, Thane West, Mumbai - 400601',        '27AAACT9876Q1Z9'),
(3, 'Company XYZ Transport',          'Vikrant Mehta',    'admin@xyz.com',                 'xyz',      'xyz123',      '+91 98190 77665', 'Building B, Logistics Super Hub, Mankoli, Bhiwandi - 421302',       '27AABCX5566M1Z4'),
(4, 'Mahindra Integrated Logistics',  'Rajeev Mahindra',  'admin@mahindra-logistics.com',  'mahindra', 'mahindra123', '+91 98190 88776', 'Logistics Park, Chakan, Pune',                                      '27AABCM5566M1Z4'),
(5, 'Tata Supply Chain Solutions',    'Pooja Sharma',     'admin@tatalogistics.com',       'tata',     'tata123',     '+91 98200 44556', 'Tata Industrial Complex, Phase 2, Chakan MIDC, Pune - 410501',       '27AAACT2233P1Z2'),
(6, 'MOT Logistics Central Operations','Operations Head', 'admin@motlogistics.in',        'admin',    'admin123',    '+91 98200 00100', 'MOT Corporate Tower, Senapati Bapat Marg, Lower Parel, Mumbai - 400013','27AAACM0001A1Z0');

-- ------------------------------------------------------------
-- 2. SEED DRIVERS (Licensed Commercial Fleet Operators)
-- ------------------------------------------------------------
INSERT INTO driver (id, driver_name, mobile_number, email, licence_number, username, password, is_active) VALUES
(1, 'Rahul R. Shinde',    '9876500001', 'rahul.shinde@motlogistics.in',    'MH4820180012345', 'rahul',  'rahul123',  1),
(2, 'Amit K. Verma',      '9876500002', 'amit.verma@motlogistics.in',      'MH4820190023456', 'amit',   'amit123',   1),
(3, 'Sagar P. Gaikwad',   '9876500003', 'sagar.gaikwad@motlogistics.in',   'MH4820200034567', 'sagar',  'sagar123',  1),
(4, 'Vikramaditya Rao',   '9876500004', 'vikram.rao@motlogistics.in',      'MH4820210045678', 'vikram', 'vikram123', 1),
(5, 'Dinesh S. Chauhan',  '9876500005', 'dinesh.chauhan@motlogistics.in',  'MH4820220056789', 'dinesh', 'dinesh123', 1);

-- ------------------------------------------------------------
-- 3. SEED VEHICLES (Commercial Cargo Fleet)
-- ------------------------------------------------------------
INSERT INTO vehicle (id, registration_number, vehicle_type, driver_id, status) VALUES
(1, 'MH-48-AB-1234', 'Tata 407 LPT (3.5T)',        1, 'Active'),
(2, 'MH-02-XY-4321', 'Mahindra Bolero Maxi Truck',  1, 'Active'),
(3, 'MH-48-CD-5678', 'Eicher Pro 2049 (4.9T)',      2, 'Active'),
(4, 'MH-12-PQ-8899', 'Tata Ace Gold CNG Van',       2, 'Active'),
(5, 'MH-48-EF-9012', 'BharatBenz 1617R (16T)',      3, 'Active'),
(6, 'MH-04-GH-7890', 'Ashok Leyland 2820 Trailer',  3, 'Active'),
(7, 'MH-46-JK-3412', 'Eicher Pro 3019 Heavy (19T)', 4, 'Active'),
(8, 'MH-05-MN-6754', 'Tata Signa 4825.TK (48T)',    5, 'Active');

-- ------------------------------------------------------------
-- 4. SEED TRIPS (55 Realistic Corridors Across Multiple Months)
-- ------------------------------------------------------------
INSERT INTO trip (id, trip_code, trip_date, company_id, driver_id, vehicle_id, source_location, destination, goods_description, amount, trip_status, remarks) VALUES

-- NOVEMBER 2026 TRIPS (Standard Core Verification Corridor)
-- Rahul for Om Sai (Total ₹ 7,300)
(1,  'MOT-2611-0101', '2026-11-01', 1, 1, 1, 'Wada MIDC',         'Bhiwandi Hub',    'Precision CNC Machinery Parts',   2500.00, 'Completed', 'Delivered on schedule; digital POD signed'),
(2,  'MOT-2611-0102', '2026-11-05', 1, 1, 1, 'Wada MIDC',         'Palghar MIDC',    'Industrial Raw Material Barrels',  2000.00, 'Completed', 'Standard industrial run'),
(3,  'MOT-2611-0103', '2026-11-10', 1, 1, 1, 'Wada MIDC',         'Thane West',      'Electrical Switchgear Panels',    2800.00, 'Completed', 'Fragile electrical cargo handled safely'),

-- Rahul for Oracle (Total ₹ 6,700)
(4,  'MOT-2611-0104', '2026-11-03', 2, 1, 1, 'Thane West',        'Mumbai Central',  'Server Rack High-Density Assemblies', 3000.00, 'Completed', 'High priority datacenter equipment'),
(5,  'MOT-2611-0105', '2026-11-08', 2, 1, 1, 'Mumbai Central',    'Thane West',      'Networking Hardware Crates',      1500.00, 'Completed', 'Return equipment transit'),
(6,  'MOT-2611-0106', '2026-11-12', 2, 1, 2, 'Thane West',        'Navi Mumbai Hub', 'Enterprise Workstations & Displays', 2200.00, 'Completed', 'Corporate IT relocation run'),

-- Amit for Om Sai, Oracle, Company XYZ (Nov 2026)
(7,  'MOT-2611-0107', '2026-11-02', 1, 2, 3, 'Bhiwandi Hub',      'Wada MIDC',       'Heavy Corrugated Packaging Rolls',2200.00, 'Completed', 'Bulk warehouse distribution'),
(8,  'MOT-2611-0108', '2026-11-06', 2, 2, 3, 'Thane West',        'Vashi Infotech',  'Fiber Optic Cable Drums',         2400.00, 'Completed', 'Secure telecom transit'),
(9,  'MOT-2611-0109', '2026-11-09', 3, 2, 3, 'Bhiwandi Hub',      'Chakan, Pune',    'FMCG Wholesale Palletized Cartons',5500.00, 'Completed', 'Inter-city express haul'),

-- Sagar for Om Sai, Oracle, Company XYZ (Nov 2026)
(10, 'MOT-2611-0110', '2026-11-04', 1, 3, 5, 'Wada MIDC',         'JNPT Port Terminal', 'Heavy Steel Wire Rod Coils',   6500.00, 'Completed', 'Port customs container clearance'),
(11, 'MOT-2611-0111', '2026-11-07', 2, 3, 5, 'Thane West',        'Hinjewadi, Pune', 'Datacenter Liquid Cooling Chiller', 5800.00, 'Completed', 'Escorted heavy logistics shipment'),
(12, 'MOT-2611-0112', '2026-11-11', 3, 3, 6, 'Bhiwandi Hub',      'Satpur, Nashik',  'Automotive Stamping Press Dies',  4800.00, 'Completed', 'Factory retooling haul'),

-- Additional November Trips
(13, 'MOT-2611-0113', '2026-11-14', 3, 1, 2, 'Bhiwandi Hub',      'Panvel CFS',      'Automobile OEM Spare Parts',      3200.00, 'Completed', 'Just-in-time delivery for assembly'),
(14, 'MOT-2611-0114', '2026-11-16', 4, 4, 7, 'Chakan MIDC, Pune', 'Bhiwandi Hub',    'Commercial Vehicle Axle Castings', 6800.00, 'Completed', 'Factory to regional warehouse'),
(15, 'MOT-2611-0115', '2026-11-18', 3, 4, 7, 'Bhiwandi Hub',      'Waluj, Aurangabad','Hydraulic Transmission Units',    7200.00, 'Completed', 'Direct industrial plant transfer'),
(16, 'MOT-2611-0116', '2026-11-20', 4, 5, 8, 'JNPT Port Terminal', 'Chakan MIDC, Pune','Heavy Machinery Import Containers',11500.00,'Completed', 'Trailer movement with port gate pass'),
(17, 'MOT-2610-0218', '2026-10-27', 1, 2, 4, 'Bhiwandi Hub',      'Tarapur MIDC',    'Pharmaceutical Packaging Blisters',2600.00, 'Completed', 'Pharma zone delivery'),
(18, 'MOT-2610-0219', '2026-10-28', 2, 1, 1, 'Thane West',        'Vapi GIDC, Gujarat','Semiconductor Test Equipment',   4800.00, 'Completed', 'Inter-state bonded cargo delivery'),
(19, 'MOT-2611-0119', '2026-11-27', 3, 3, 6, 'Bhiwandi Hub',      'JNPT Port Terminal', 'Export Grade Textile Bales (40ft)', 8400.00,'Completed', 'Container stuffing acknowledged'),
(20, 'MOT-2611-0120', '2026-11-29', 4, 5, 8, 'Pimpri, Pune',      'Kurla Cargo Yard', 'Engine Crankshaft Billets',       9200.00, 'Completed', 'Heavy logistics express route'),

-- OCTOBER 2026 TRIPS
(21, 'MOT-2610-0201', '2026-10-02', 1, 1, 1, 'Wada MIDC',         'Bhiwandi Hub',    'Aluminum Ingot Bundles',          2400.00, 'Completed', 'Smooth transit; client receipt verified'),
(22, 'MOT-2610-0202', '2026-10-04', 1, 2, 3, 'Wada MIDC',         'Thane West',      'Industrial Control Panels',       2700.00, 'Completed', 'Delivered undamaged'),
(23, 'MOT-2610-0203', '2026-10-07', 1, 3, 5, 'Wada MIDC',         'JNPT Port',       'Copper Rod Spools',               6400.00, 'Completed', 'Customs documentation stamped'),
(24, 'MOT-2610-0204', '2026-10-10', 1, 1, 2, 'Wada MIDC',         'Palghar MIDC',    'Chemical Plastic Granules',       1900.00, 'Completed', 'Material safety sheet accompanied'),
(25, 'MOT-2610-0205', '2026-10-14', 1, 4, 7, 'Wada MIDC',         'Chakan, Pune',    'Precision Tooling Machinery',     6800.00, 'Completed', 'Long-haul industrial route'),
(26, 'MOT-2610-0206', '2026-10-18', 1, 1, 1, 'Bhiwandi Hub',      'Wada MIDC',       'Cardboard Packing Cartons',       2100.00, 'Completed', 'Return distribution run'),
(27, 'MOT-2610-0207', '2026-10-24', 1, 2, 4, 'Wada MIDC',         'Bhiwandi Hub',    'Automated Conveyor Rollers',      2500.00, 'Completed', 'Dispatched on time'),

(28, 'MOT-2610-0208', '2026-10-03', 2, 1, 1, 'Thane West',        'Mumbai BKC',      'Corporate IT Server Towers',      2900.00, 'Completed', 'Secure transport POD uploaded'),
(29, 'MOT-2610-0209', '2026-10-06', 2, 2, 3, 'Thane West',        'Vashi Infotech',  'Uninterruptible Power Supplies',  2300.00, 'Completed', 'Battery cargo handled per protocol'),
(30, 'MOT-2610-0210', '2026-10-11', 2, 3, 5, 'Thane West',        'Hinjewadi, Pune', 'Modular Server Enclosures',       5600.00, 'Completed', 'Datacenter security clearance OK'),
(31, 'MOT-2610-0211', '2026-10-15', 2, 1, 2, 'Mumbai Port',       'Thane West',      'Imported Router Consignments',    3100.00, 'Completed', 'Port clearance complete'),
(32, 'MOT-2610-0212', '2026-10-22', 2, 4, 7, 'Thane West',        'Gandhinagar GIFT','Fiber Backbone Switchboards',     5300.00, 'Completed', 'Inter-state high value transport'),

(33, 'MOT-2610-0213', '2026-10-05', 3, 2, 3, 'Bhiwandi Hub',      'Chakan, Pune',    'Commercial Braking Systems',      5400.00, 'Completed', 'Plant intake inspection passed'),
(34, 'MOT-2610-0214', '2026-10-09', 3, 3, 6, 'Bhiwandi Hub',      'Satpur, Nashik',  'Alloy Engine Blocks',             4900.00, 'Completed', 'Foundry parts delivered'),
(35, 'MOT-2610-0215', '2026-10-16', 3, 5, 8, 'Bhiwandi Hub',      'JNPT Port Terminal', 'Export Vehicle Assembly CKD',  9800.00, 'Completed', 'Port terminal gate pass signed'),
(36, 'MOT-2610-0216', '2026-10-25', 3, 1, 1, 'Bhiwandi Hub',      'Panvel CFS',      'Automotive Electrical Harnesses', 3500.00, 'Completed', 'Urgent assembly line order'),
(37, 'MOT-2610-0217', '2026-10-29', 3, 4, 7, 'Bhiwandi Hub',      'Waluj, Aurangabad','Tractor Transmission Gearing',   5000.00, 'Completed', 'Delivered to central depot'),

-- SEPTEMBER 2026 TRIPS
(38, 'MOT-2609-0301', '2026-09-03', 1, 1, 1, 'Wada MIDC',         'Bhiwandi Hub',    'Forged Pipe Fittings',            2300.00, 'Completed', 'Factory to warehouse transfer'),
(39, 'MOT-2609-0302', '2026-09-08', 1, 2, 3, 'Wada MIDC',         'Thane West',      'Industrial Lubricant Drums',      2600.00, 'Completed', 'Hazmat protocols observed'),
(40, 'MOT-2609-0303', '2026-09-12', 1, 3, 5, 'Wada MIDC',         'JNPT Port Terminal', 'Structural Steel Angles',      6200.00, 'Completed', 'Heavy transit POD signed'),
(41, 'MOT-2609-0304', '2026-09-17', 1, 1, 2, 'Wada MIDC',         'Palghar MIDC',    'Industrial Paint Drums',          1800.00, 'Completed', 'Standard local run'),
(42, 'MOT-2609-0305', '2026-09-23', 1, 4, 7, 'Wada MIDC',         'Chakan, Pune',    'Cast Iron Flywheels',             5600.00, 'Completed', 'Plant delivery confirmed'),

(43, 'MOT-2609-0306', '2026-09-04', 2, 1, 1, 'Thane West',        'Mumbai Central',  'High Performance Servers',        3200.00, 'Completed', 'White glove delivery executed'),
(44, 'MOT-2609-0307', '2026-09-09', 2, 2, 3, 'Thane West',        'Vashi Infotech',  'Telecom Router Racks',            2500.00, 'Completed', 'Safely offloaded at facility'),
(45, 'MOT-2609-0308', '2026-09-15', 2, 3, 5, 'Thane West',        'Hinjewadi, Pune', 'Precision Cooling Equipment',     6100.00, 'Completed', 'Specialized handling verified'),
(46, 'MOT-2609-0309', '2026-09-21', 2, 1, 2, 'Mumbai Central',    'Thane West',      'Spare Component Replacements',    1600.00, 'Completed', 'Warranty return dispatch'),
(47, 'MOT-2609-0310', '2026-09-28', 2, 4, 7, 'Thane West',        'Ahmedabad GIDC',  'Optic Fiber Transceivers',        8000.00, 'Completed', 'Long-haul inter-state run'),

(48, 'MOT-2609-0311', '2026-09-06', 3, 2, 3, 'Bhiwandi Hub',      'Chakan, Pune',    'Automotive Starter Motors',       5200.00, 'Completed', 'OEM receipt acknowledged'),
(49, 'MOT-2609-0312', '2026-09-14', 3, 3, 6, 'Bhiwandi Hub',      'Satpur, Nashik',  'Alloy Cylinder Heads',            4700.00, 'Completed', 'Delivered without delays'),
(50, 'MOT-2609-0313', '2026-09-22', 3, 5, 8, 'Bhiwandi Hub',      'JNPT Port Terminal', 'Automotive Axles for Export',   9600.00, 'Completed', 'Export shipping line handed over'),

-- DECEMBER 2026 TRIPS
(51, 'MOT-2612-0401', '2026-12-01', 1, 1, 1, 'Wada MIDC',         'Bhiwandi Hub',    'Hydraulic Control Valves',        2500.00, 'Completed', 'December opening delivery'),
(52, 'MOT-2612-0402', '2026-12-03', 2, 1, 1, 'Thane West',        'Mumbai Central',  'Data Storage SAN Arrays',         3100.00, 'Completed', 'Priority enterprise migration'),
(53, 'MOT-2612-0403', '2026-12-05', 3, 2, 3, 'Bhiwandi Hub',      'Chakan, Pune',    'Commercial Truck Suspension Kits',5600.00, 'Completed', 'Fast-track supply chain run'),
(54, 'MOT-2612-0404', '2026-12-07', 4, 4, 7, 'Chakan MIDC, Pune', 'Bhiwandi Hub',    'Commercial Bus Transmission Kits',7100.00, 'Completed', 'Direct plant supply'),
(55, 'MOT-2612-0405', '2026-12-10', 1, 3, 5, 'Wada MIDC',         'JNPT Port Terminal', 'Galvanized Sheet Coils (25T)',  6600.00, 'Completed', 'Harbor terminal gate in complete');

-- ------------------------------------------------------------
-- 5. SEED INVOICES (Monthly & Annual Real Financial Ledger)
-- ------------------------------------------------------------
INSERT INTO invoice (id, invoice_number, invoice_date, company_id, driver_id, vehicle_id, billing_month, billing_year, total_trips, total_amount, amount_paid, remaining_amount, invoice_type, status) VALUES
-- September 2026 Invoices (Fully Settled)
(1, 'INV-2026-0901', '2026-09-30', 1, NULL, NULL, 9,  2026, 5, 18500.00, 18500.00, 0.00,    'Company', 'Paid'),
(2, 'INV-2026-0902', '2026-09-30', 2, NULL, NULL, 9,  2026, 5, 21400.00, 21400.00, 0.00,    'Company', 'Paid'),
(3, 'INV-2026-0903', '2026-09-30', 3, NULL, NULL, 9,  2026, 3, 19500.00, 19500.00, 0.00,    'Company', 'Paid'),

-- October 2026 Invoices (Closed / Partially Settled)
(4, 'INV-2026-1001', '2026-10-31', 1, NULL, NULL, 10, 2026, 7, 24800.00, 24800.00, 0.00,    'Company', 'Paid'),
(5, 'INV-2026-1002', '2026-10-31', 2, NULL, NULL, 10, 2026, 5, 19200.00, 12000.00, 7200.00, 'Company', 'Partially Paid'),
(6, 'INV-2026-1003', '2026-10-31', 3, NULL, NULL, 10, 2026, 5, 28600.00, 28600.00, 0.00,    'Company', 'Paid'),

-- November 2026 Invoices (Active Operations Cycle)
(7, 'INV-2026-1101', '2026-11-15', 1, NULL, NULL, 11, 2026, 5, 16000.00, 10000.00, 6000.00, 'Company', 'Partially Paid'),
(8, 'INV-2026-1102', '2026-11-15', 2, NULL, NULL, 11, 2026, 5, 14900.00, 14900.00, 0.00,    'Company', 'Paid'),
(9, 'INV-2026-1103', '2026-11-15', 3, NULL, NULL, 11, 2026, 5, 32200.00, 15000.00, 17200.00,'Company', 'Partially Paid'),

-- Driver Earnings Vouchers for November 2026
(10,'INV-DRV-2611-01', '2026-11-15', 1, 1, 1, 11, 2026, 3, 7300.00,  7300.00,  0.00,    'Driver',  'Paid'),
(11,'INV-DRV-2611-02', '2026-11-15', 2, 1, 1, 11, 2026, 3, 6700.00,  6700.00,  0.00,    'Driver',  'Paid'),
(12,'INV-DRV-2611-03', '2026-11-15', 1, 2, 3, 11, 2026, 1, 2200.00,  2200.00,  0.00,    'Driver',  'Paid');

-- ------------------------------------------------------------
-- 6. SEED PAYMENTS (Audit Trail of Bank Settlements)
-- ------------------------------------------------------------
INSERT INTO payment (id, invoice_id, company_id, total_amount, amount_received, remaining_amount, payment_date, payment_status, notes) VALUES
(1, 1, 1, 18500.00, 18500.00, 0.00,    '2026-09-30', 'Paid',           'Settled in full via HDFC Corporate RTGS #HDFC2609301142'),
(2, 2, 2, 21400.00, 21400.00, 0.00,    '2026-09-30', 'Paid',           'Treasury wire clearance #CITI9988231 from Oracle India'),
(3, 3, 3, 19500.00, 19500.00, 0.00,    '2026-09-30', 'Paid',           'Vendor payment batch #ICICI2609289123 processed in full'),
(4, 4, 1, 24800.00, 24800.00, 0.00,    '2026-10-31', 'Paid',           'Cleared via HDFC NetBanking #HDFC2610319801'),
(5, 5, 2, 19200.00, 12000.00, 7200.00, '2026-10-31', 'Partially Paid', '60% milestone advance cleared; balance payable in next cycle'),
(6, 6, 3, 28600.00, 28600.00, 0.00,    '2026-10-31', 'Paid',           'Automated vendor clearing settlement #ICICI2610287714'),
(7, 7, 1, 16000.00, 10000.00, 6000.00, '2026-11-15', 'Partially Paid', 'Part payment received via RTGS #HDFC2611158912; balance scheduled'),
(8, 8, 2, 14900.00, 14900.00, 0.00,    '2026-11-15', 'Paid',           'Full invoice settlement via Corporate Wire #CITI1115449'),
(9, 9, 3, 32200.00, 15000.00, 17200.00,'2026-11-15', 'Partially Paid', '50% progress billing paid via Axis Bank Corporate #AXIS991204');

SET FOREIGN_KEY_CHECKS = 1;
