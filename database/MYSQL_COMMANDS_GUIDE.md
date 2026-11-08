# 🗄️ MySQL Setup & Table Display Commands Guide

This guide gives you the exact commands to start MySQL, import the schema, and display all stored data in clean table format in your terminal or MySQL client.

---

## 1. MySQL Status on Your Mac
Good news: **MySQL is already installed on your Mac** via Homebrew at `/opt/homebrew/bin/mysql`! You do not need to download or re-install MySQL.

### Start the MySQL Service:
Open your Terminal and run:
```bash
brew services start mysql
```

*(If you ever need to stop it later: `brew services stop mysql`)*

---

## 2. Load / Import the Project Database

To create the `vehicle_rental_db` database and load all default tables and seed records, run this single command in your terminal from the project folder (`/Users/rohan_rajesh/Desktop/OOPS`):

```bash
mysql -u root < database/schema.sql
```

---

## 3. Display All Stored Data in Table Form (One-Shot Command)

We have created a ready-to-run script [`database/display_tables.sql`](file:///Users/rohan_rajesh/Desktop/OOPS/database/display_tables.sql) that queries and formats all tables:

```bash
mysql -u root -t < database/display_tables.sql
```
*(The `-t` flag ensures MySQL renders the output in full ASCII table box borders `+-----+-----+`)*

---

## 4. Interactive MySQL Terminal & Tagged SQL Commands

If you want to enter MySQL interactively and type commands one by one to show your professor or evaluator:

### Step 1: Open MySQL Terminal
```bash
mysql -u root
```

### Step 2: Select the Database
```sql
USE vehicle_rental_db;
```

### Step 3: Check All Tables
```sql
SHOW TABLES;
```
*Output will list: `users`, `agencies`, `customers`, `vehicles`, `bookings`, `reviews`, `notifications`.*

---

### Step 4: Display Each Table

#### 👥 1. Users Table (Admin, Customers, Agencies)
```sql
SELECT id, username, full_name, role, email, phone, created_at FROM users;
```

#### 🏢 2. Agencies Table (Grouped by Status: PENDING / ACCEPTED / REJECTED)
```sql
SELECT 
    a.id AS 'Agency ID',
    a.agency_name AS 'Agency Name',
    u.full_name AS 'Owner',
    a.registration_no AS 'Reg No',
    u.phone AS 'Phone',
    a.status AS 'Status'
FROM agencies a
JOIN users u ON a.user_id = u.id
ORDER BY 
    CASE 
        WHEN a.status = 'PENDING' THEN 1 
        WHEN a.status = 'APPROVED' THEN 2 
        ELSE 3 
    END;
```

#### 👤 3. Customers Table
```sql
SELECT 
    c.id AS 'Customer ID',
    u.full_name AS 'Full Name',
    u.username AS 'Username',
    c.license_number AS 'Driver License No',
    u.phone AS 'Phone',
    u.email AS 'Email'
FROM customers c
JOIN users u ON c.user_id = u.id;
```

#### 🚗 4. Vehicles Table (Cars, Bikes & EVs with Condition Checkmarks)
```sql
SELECT 
    id, vehicle_type, brand, model, year, license_plate, 
    daily_rate, colour, mileage, status, is_electric 
FROM vehicles;
```

#### 📅 5. Bookings Table (Rentals, Pricing Strategy, Total Cost)
```sql
SELECT 
    b.id AS 'Booking ID',
    u.full_name AS 'Customer',
    CONCAT(v.brand, ' ', v.model) AS 'Vehicle',
    b.start_date AS 'Start',
    b.end_date AS 'Due Date',
    b.total_days AS 'Days',
    b.pricing_strategy AS 'Pricing',
    CONCAT('$', FORMAT(b.total_cost, 2)) AS 'Total Rent',
    b.status AS 'Status'
FROM bookings b
JOIN customers c ON b.customer_id = c.id
JOIN users u ON c.user_id = u.id
JOIN vehicles v ON b.vehicle_id = v.id;
```

#### ⭐ 6. Customer Ratings & Reviews
```sql
SELECT 
    r.id AS 'Review #',
    r.booking_id AS 'Booking #',
    u.full_name AS 'Customer',
    CONCAT(v.brand, ' ', v.model) AS 'Vehicle',
    r.rating AS 'Stars',
    r.comment AS 'Feedback'
FROM reviews r
JOIN customers c ON r.customer_id = c.id
JOIN users u ON c.user_id = u.id
JOIN vehicles v ON r.vehicle_id = v.id;
```

#### 🔔 7. Notifications Table
```sql
SELECT id, user_id, title, message, is_read, created_at FROM notifications;
```

---

## 5. (Optional) Graphical GUI Tool: MySQL Workbench / TablePlus

If you prefer a graphical window with clickable tables:
1. **MySQL Workbench**: Download for macOS from [dev.mysql.com/downloads/workbench](https://dev.mysql.com/downloads/workbench/).
   - Host: `127.0.0.1` or `localhost`
   - Port: `3306`
   - Username: `root`
   - Password: *(leave blank if none)*
2. Or run via Homebrew:
   ```bash
   brew install --cask mysqlworkbench
   ```
