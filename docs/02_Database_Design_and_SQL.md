# Document 02: Database Design & Relational Schema

## 1. Database Overview
The application connects to a local MySQL database named `transport_db`. The relational schema enforces **multi-tenant company isolation** using foreign keys with cascading integrity constraints.

```
+---------------------------------------------------------------+
|                       DATABASE CATALOG                        |
|                         transport_db                          |
+---------------------------------------------------------------+
      |                        |                        |
      v                        v                        v
+------------+           +------------+           +------------+
|  company   |<--+   +---|   driver   |           |  vehicle   |
| (Tenants)  |   |   |   | (Operators)|<----------|  (Fleet)   |
+------------+   |   |   +------------+           +------------+
      |          |   |         |                        |
      v          |   |         v                        |
+------------+   |   |   +------------+                 |
|  invoice   |---+   +---|    trip    |<----------------+
+------------+           | (Activity) |
      |                  +------------+
      v
+------------+
|  payment   |
+------------+
```

---

## 2. Table Specifications

### 2.1 Table: `company` (Company Admin Tenants)
Stores corporate client companies. Each company has independent administrative credentials.

```sql
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
```

### 2.2 Table: `driver` (Drivers & Vehicle Owners)
Stores independent commercial drivers who haul consignments for client companies.

```sql
CREATE TABLE driver (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    driver_name     VARCHAR(100) NOT NULL,
    mobile_number   VARCHAR(20)  NOT NULL,
    email           VARCHAR(100),
    licence_number  VARCHAR(30)  NOT NULL,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(100) NOT NULL,
    is_active       TINYINT(1)   DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2.3 Table: `vehicle` (Commercial Fleet Vehicles)
Commercial vehicles owned or operated by drivers. Multiple vehicles can be linked to a single driver.

```sql
CREATE TABLE vehicle (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    registration_number VARCHAR(20) UNIQUE NOT NULL,
    vehicle_type        VARCHAR(50) NOT NULL,
    driver_id           INT,
    status              VARCHAR(20) DEFAULT 'Active',
    FOREIGN KEY (driver_id) REFERENCES driver(id) ON DELETE SET NULL
);
```

### 2.4 Table: `trip` (Daily Operational Trips)
The central operational activity table. Contains both `company_id` and `driver_id` for strict tenant isolation.

```sql
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
```

### 2.5 Table: `invoice` (Monthly Billing Statements)
Stores both Company-wide monthly invoices (`invoice_type = 'Company'`) and Driver-wise invoices (`invoice_type = 'Driver'`).

```sql
CREATE TABLE invoice (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number   VARCHAR(20) UNIQUE NOT NULL,
    invoice_date     DATE NOT NULL,
    company_id       INT NOT NULL,
    driver_id        INT,
    vehicle_id       INT,
    billing_month    INT NOT NULL,
    billing_year     INT NOT NULL,
    total_trips      INT DEFAULT 0,
    total_amount     DECIMAL(10,2) DEFAULT 0.00,
    amount_paid      DECIMAL(10,2) DEFAULT 0.00,
    remaining_amount DECIMAL(10,2) DEFAULT 0.00,
    invoice_type     VARCHAR(20) DEFAULT 'Company',
    status           VARCHAR(20) DEFAULT 'Generated',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id)  REFERENCES driver(id)  ON DELETE SET NULL
);
```

### 2.6 Table: `payment` (Settlement Tracking)
Maintains audit logs of payments, receipts, and outstanding invoice balances.

```sql
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
```

---

## 3. Seeded Default Accounts

### Companies (Company Admins)
| ID | Company Name | Admin Name | Username | Email | Password |
|---|---|---|---|---|---|
| 1 | Om Sai | Rajesh Patil | `omsai` | `admin@omsai.com` | `omsai123` |
| 2 | Oracle | Sanjay Deshmukh | `oracle` | `admin@oracle.com` | `oracle123` |
| 3 | Company XYZ | Vikrant Mehta | `xyz` | `admin@xyz.com` | `xyz123` |

### Drivers / Vehicle Owners
| ID | Driver Name | Username | Email | Password | Operating Vehicles |
|---|---|---|---|---|---|
| 1 | Rahul | `rahul` | `rahul@transport.com` | `rahul123` | `MH-48-AB-1234` (Tempo), `MH-02-XY-4321` (Pickup Van) |
| 2 | Amit | `amit` | `amit@transport.com` | `amit123` | `MH-48-CD-5678` (Pickup Van) |
| 3 | Sagar | `sagar` | `sagar@transport.com` | `sagar123` | `MH-48-EF-9012` (Heavy Truck), `MH-04-GH-7890` (Trailer) |
