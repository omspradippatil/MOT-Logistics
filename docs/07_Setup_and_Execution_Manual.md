# 🚀 Document 7: Setup & Execution Manual

A comprehensive step-by-step setup guide for running and testing the application across macOS, Linux, and Windows.

---

## 1. System Requirements

- **Operating System:** macOS (Apple Silicon / Intel), Windows 10/11, or Linux
- **Java:** JDK 8, 11, 17, 21, or 27
- **Database:** MySQL Server 5.7+ or 8.0+ / 9.x
- **JDBC Driver:** `mysql-connector-j-8.x.jar` (pre-bundled in `lib/`)

---

## 2. Docker Setup (Recommended for Instant Cross-Platform Run)

If you have Docker installed on any PC (Windows, Mac, Linux), you don't need to install Java or MySQL:

```bash
docker compose up --build
```

- **Web GUI:** Open [http://localhost:6080](http://localhost:6080) in your web browser. The entire Swing desktop interface will load with auto-scaling and window management!
- **Database:** MariaDB with all seed data (`database/schema.sql`) runs automatically.
- **Port Mappings:**
  - `6080` → noVNC Web Browser Interface
  - `3306` → Direct MySQL access
  - `5900` → Native VNC clients
- **Invoices:** Saved to `./invoices/` on your host PC automatically.

---

## 3. Fast-Track Manual Setup (3 Easy Steps)

### Step 1: Initialize Database
Ensure MySQL is running, then execute the schema file:
```bash
# On macOS / Linux terminal:
mysql -u root < database/schema.sql
```
*(If your root account requires a password, add `-p`):*
```bash
mysql -u root -p < database/schema.sql
```

Verify that all tables and sample seed records are in place:
```bash
mysql -u root -e "SHOW TABLES IN transport_db;"
```

### Step 2: Configure Password (if needed)
Open [`src/com/transport/db/DatabaseConnection.java`](file:///Users/om/Desktop/Projects/MOT-Logistics/src/com/transport/db/DatabaseConnection.java) and set your MySQL password:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/transport_db";
private static final String USER     = "root";
private static final String PASSWORD = ""; // <-- Enter your MySQL password here
```

### Step 3: Compile and Launch
Using the pre-configured executable shell scripts:
```bash
# Compile all source files into out/
./build.sh

# Run the desktop application
./run.sh
```

---

## 3. Manual Compilation & Execution Commands

### On macOS / Linux:
```bash
# Create build directory
mkdir -p out

# Compile with classpath to lib jars
javac -cp "lib/*" -d out -sourcepath src src/com/transport/Main.java

# Run application
java -cp "out:lib/*" com.transport.Main
```

### On Windows (Command Prompt / PowerShell):
```cmd
mkdir out
javac -cp "lib/*" -d out -sourcepath src src/com/transport/Main.java
java -cp "out;lib/*" com.transport.Main
```
*(Notice the semicolon `;` classpath separator on Windows instead of `:` on Unix).*

---

## 4. Default Login Credentials

| Role | Username | Password | Privileges |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full system access: Companies, Drivers, Vehicles, Invoices, Payments |
| **Driver (Sample)** | `rahul` | `rahul123` | Operational access: Log daily trips, view personal trip history |

---

## 5. End-to-End Testing Walkthrough

Follow this 5-minute walkthrough to test every feature:

1. **Start the App:** Run `./run.sh`.
2. **Login as Driver:**
   - Click the "Driver Login" tab.
   - Enter `rahul` / `rahul123`. Click "Login as Driver".
   - The driver panel opens.
   - Enter trip details:
     - Date: Current date
     - Company: Company A
     - Vehicle: MH-48-AB-1234
     - Source: Wada
     - Destination: Thane
     - Amount: 2500
     - Remarks: Urgent cargo
   - Click **Submit Trip**. Note the generated `TRIP001` confirmation.
   - Click the **My Trip History** tab to view your saved trip.
   - Click **Logout** at top right.
3. **Login as Admin:**
   - Enter `admin` / `admin123`. Click "Login as Admin".
   - View the **Dashboard**: see the updated trip count and revenue cards!
   - Click **🏢 Companies**: Add a second company (e.g. "Tata Logistics").
   - Click **👨 Drivers**: Register a new driver (e.g. "Suresh Patil").
   - Click **🚛 Vehicles**: Register a new vehicle and assign it to Suresh.
   - Click **📋 Trip History**: Search trips by month or company.
   - Click **📄 Invoice**:
     - Select Company: "Company A"
     - Select Month: Current month
     - Click **Generate Invoice**.
     - Review the trip table and grand total!
     - Click **Export PDF**. Save the file to your desktop or inspect the auto-saved copy in `invoices/`.
   - Click **💰 Payments**:
     - Select the invoice from the table.
     - Enter amount received (e.g. `2500.00`).
     - Set status to `Paid` or `Partially Paid` and click **Update Payment**.
     - The row color dynamically updates!
