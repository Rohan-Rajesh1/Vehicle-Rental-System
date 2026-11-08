-- ====================================================================
-- VEHICLE RENTAL SYSTEM - DISPLAY STORED DATA SCRIPT
-- Run this in your MySQL terminal or GUI to display all stored data in table form
-- ====================================================================

USE vehicle_rental_db;

-- --------------------------------------------------------------------
-- 1. ALL REGISTERED USERS (Admin, Customers, Agencies)
-- --------------------------------------------------------------------
SELECT 
    id AS 'User ID',
    username AS 'Username',
    full_name AS 'Full Name',
    role AS 'Role',
    email AS 'Email',
    phone AS 'Phone Number',
    created_at AS 'Registered At'
FROM users;

-- --------------------------------------------------------------------
-- 2. RENTAL AGENCIES (Grouped with Status: PENDING / APPROVED / REJECTED)
-- --------------------------------------------------------------------
SELECT 
    a.id AS 'Agency ID',
    a.agency_name AS 'Agency Name',
    u.full_name AS 'Owner Name',
    u.username AS 'Login Username',
    a.registration_no AS 'Registration No',
    u.phone AS 'Phone',
    u.email AS 'Email',
    a.address AS 'Address',
    CASE 
        WHEN a.status = 'APPROVED' THEN 'ACCEPTED'
        WHEN a.status = 'REJECTED' THEN 'REJECTED'
        ELSE 'PENDING'
    END AS 'Status'
FROM agencies a
JOIN users u ON a.user_id = u.id
ORDER BY 
    CASE 
        WHEN a.status = 'PENDING' THEN 1
        WHEN a.status = 'APPROVED' THEN 2
        ELSE 3
    END, a.id;

-- --------------------------------------------------------------------
-- 3. REGISTERED CUSTOMERS
-- --------------------------------------------------------------------
SELECT 
    c.id AS 'Customer ID',
    u.full_name AS 'Full Name',
    u.username AS 'Username',
    c.license_number AS 'Driver License No',
    u.phone AS 'Phone',
    u.email AS 'Email',
    c.address AS 'Address'
FROM customers c
JOIN users u ON c.user_id = u.id;

-- --------------------------------------------------------------------
-- 4. ALL VEHICLES (Cars, Bikes, EVs with Specs & Condition Checkmarks)
-- --------------------------------------------------------------------
SELECT 
    v.id AS 'Vehicle ID',
    a.agency_name AS 'Agency',
    v.vehicle_type AS 'Type',
    CONCAT(v.brand, ' ', v.model, ' (', v.year, ')') AS 'Vehicle Name',
    v.license_plate AS 'Plate Number',
    v.colour AS 'Colour',
    v.mileage AS 'Mileage',
    CONCAT('$', FORMAT(v.daily_rate, 2)) AS 'Daily Rate',
    v.status AS 'Status',
    CASE WHEN v.is_electric = TRUE THEN 'YES (EV)' ELSE 'NO' END AS 'Electric EV',
    CASE WHEN v.tyre_condition = TRUE THEN 'GOOD' ELSE 'CHECK' END AS 'Tyres',
    CASE WHEN v.engine_condition = TRUE THEN 'GOOD' ELSE 'CHECK' END AS 'Engine',
    CASE WHEN v.insurance_verified = TRUE THEN 'VERIFIED' ELSE 'EXPIRED' END AS 'Insurance'
FROM vehicles v
JOIN agencies a ON v.agency_id = a.id
ORDER BY v.id;

-- --------------------------------------------------------------------
-- 5. BOOKINGS & RENTALS (Detailed Breakdown & Pricing Strategy)
-- --------------------------------------------------------------------
SELECT 
    b.id AS 'Booking #',
    u.full_name AS 'Customer Name',
    u.phone AS 'Customer Phone',
    CONCAT(v.brand, ' ', v.model) AS 'Rented Vehicle',
    a.agency_name AS 'Rental Agency',
    b.start_date AS 'Start Date',
    b.end_date AS 'Return Due Date',
    b.total_days AS 'Days',
    b.pricing_strategy AS 'Pricing Strategy',
    CONCAT('$', FORMAT(b.total_cost, 2)) AS 'Total Rent',
    b.status AS 'Booking Status'
FROM bookings b
JOIN customers c ON b.customer_id = c.id
JOIN users u ON c.user_id = u.id
JOIN vehicles v ON b.vehicle_id = v.id
JOIN agencies a ON v.agency_id = a.id
ORDER BY b.id DESC;

-- --------------------------------------------------------------------
-- 6. CUSTOMER RATINGS & VEHICLE REVIEWS
-- --------------------------------------------------------------------
SELECT 
    r.id AS 'Review #',
    r.booking_id AS 'Booking ID',
    u.full_name AS 'Customer Name',
    CONCAT(v.brand, ' ', v.model) AS 'Vehicle',
    a.agency_name AS 'Agency',
    REPEAT('* ', r.rating) AS 'Star Rating',
    r.comment AS 'Feedback',
    r.created_at AS 'Date'
FROM reviews r
JOIN customers c ON r.customer_id = c.id
JOIN users u ON c.user_id = u.id
JOIN vehicles v ON r.vehicle_id = v.id
JOIN agencies a ON r.agency_id = a.id;

-- --------------------------------------------------------------------
-- 7. SYSTEM NOTIFICATIONS
-- --------------------------------------------------------------------
SELECT 
    n.id AS 'Notification #',
    u.username AS 'Recipient',
    u.role AS 'Role',
    n.title AS 'Title',
    n.message AS 'Message Text',
    CASE WHEN n.is_read = TRUE THEN 'READ' ELSE 'UNREAD' END AS 'Status',
    n.created_at AS 'Timestamp'
FROM notifications n
JOIN users u ON n.user_id = u.id
ORDER BY n.id DESC;
