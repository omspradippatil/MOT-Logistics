# Document 07: Setup & Execution Manual

## 1. System Requirements
- **Java Development Kit (JDK):** Version 8 or higher (OpenJDK / Oracle JDK)
- **Database:** MySQL Server 8.x running locally on port `3306`
- **Operating System:** Windows, macOS, or Linux

---

## 2. Database Initialization
Ensure your local MySQL service is running. Run the following command from the project root directory:

```bash
mysql -u root < database/schema.sql
```
*(If your root account requires a password, use `mysql -u root -p < database/schema.sql`)*

This script creates the database `transport_db`, establishes all multi-tenant tables (`company`, `driver`, `vehicle`, `trip`, `invoice`, `payment`), and seeds realistic test data.

---

## 3. Compilation & Build
To compile the Core Java source code and package the standalone executable JAR:

```bash
./build.sh
```

This compiles all files into the `out/` folder and produces `app.jar`.

---

## 4. Running the Application
Launch the desktop Swing application:

```bash
./run.sh
```
Or directly using Java:
```bash
java -cp "out:lib/*" com.transport.Main
```

---

## 5. Pre-Seeded Login Credentials

### A. Company Admin Accounts (Tenant Login)
Log in on the **Company Admin** tab using either Username or Email:

| Company Tenant | Username | Email | Password | Admin Contact |
|---|---|---|---|---|
| **Om Sai** | `omsai` | `admin@omsai.com` | `omsai123` | Rajesh Patil |
| **Oracle** | `oracle` | `admin@oracle.com` | `oracle123` | Sanjay Deshmukh |
| **Company XYZ** | `xyz` | `admin@xyz.com` | `xyz123` | Vikrant Mehta |

### B. Driver / Vehicle Owner Accounts
Log in on the **Driver / Vehicle Owner** tab using either Username or Email:

| Driver Name | Username | Email | Password | Operating Vehicles |
|---|---|---|---|---|
| **Rahul** | `rahul` | `rahul@transport.com` | `rahul123` | `MH-48-AB-1234` (Tempo), `MH-02-XY-4321` (Pickup Van) |
| **Amit** | `amit` | `amit@transport.com` | `amit123` | `MH-48-CD-5678` (Pickup Van) |
| **Sagar** | `sagar` | `sagar@transport.com` | `sagar123` | `MH-48-EF-9012` (Truck), `MH-04-GH-7890` (Trailer) |

---

## 6. Running the Automated 70-Point Test Suite
To execute the automated regression test suite:

```bash
java -cp "out:lib/*" com.transport.test.ComprehensiveTestSuite
```
