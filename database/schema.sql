-- ====================================================================
-- VEHICLE RENTAL SYSTEM - DATABASE SCHEMA
-- For Java 21 / Swing Desktop Application with MySQL & JDBC
-- ====================================================================

CREATE DATABASE IF NOT EXISTS vehicle_rental_db;
USE vehicle_rental_db;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER', 'AGENCY') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. AGENCIES TABLE
CREATE TABLE IF NOT EXISTS agencies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    agency_name VARCHAR(100) NOT NULL,
    registration_no VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. CUSTOMERS TABLE
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. VEHICLES TABLE (Polymorphic table supporting Car and Bike with rich specifications)
CREATE TABLE IF NOT EXISTS vehicles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    agency_id INT NOT NULL,
    vehicle_type ENUM('CAR', 'BIKE') NOT NULL,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    license_plate VARCHAR(30) NOT NULL UNIQUE,
    daily_rate DECIMAL(10,2) NOT NULL,
    status ENUM('AVAILABLE', 'RENTED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    -- Car specific fields
    seating_capacity INT DEFAULT 5,
    transmission VARCHAR(20) DEFAULT 'Manual',
    fuel_type VARCHAR(20) DEFAULT 'Petrol',
    -- Bike specific fields
    engine_cc INT DEFAULT 150,
    bike_type VARCHAR(30) DEFAULT 'Standard',
    -- Rich specifications & condition tick marks
    colour VARCHAR(50) DEFAULT 'Pearl White',
    mileage VARCHAR(50) DEFAULT '18 km/l',
    tyre_condition BOOLEAN DEFAULT TRUE,
    engine_condition BOOLEAN DEFAULT TRUE,
    gps_enabled BOOLEAN DEFAULT TRUE,
    insurance_verified BOOLEAN DEFAULT TRUE,
    -- Electric vehicle specifications
    is_electric BOOLEAN DEFAULT FALSE,
    battery_capacity DECIMAL(6,2) DEFAULT NULL,
    range_km INT DEFAULT NULL,
    charging_time DECIMAL(4,2) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (agency_id) REFERENCES agencies(id) ON DELETE CASCADE
);

-- 5. BOOKINGS TABLE
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'RETURNED') DEFAULT 'PENDING',
    notes VARCHAR(255),
    pricing_strategy VARCHAR(100) DEFAULT 'Standard Daily Rate',
    penalty_fee DECIMAL(10,2) DEFAULT 0.00,
    fuel_fee DECIMAL(10,2) DEFAULT 0.00,
    platform_fee DECIMAL(10,2) DEFAULT 0.00,
    inspection_notes TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- 6. REVIEWS TABLE
CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    customer_id INT NOT NULL,
    agency_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (agency_id) REFERENCES agencies(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- 7. NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ====================================================================
-- SEED DATA FOR TESTING AND DEMONSTRATION
-- ====================================================================

-- 1. Pre-seeded Admin
INSERT IGNORE INTO users (id, username, password, full_name, email, phone, role) 
VALUES (1, 'admin', 'admin123', 'System Administrator', 'admin@vehiclerental.com', '9876543210', 'ADMIN');

-- 2. Pre-seeded Approved Agency
INSERT IGNORE INTO users (id, username, password, full_name, email, phone, role)
VALUES (2, 'apex', 'apex123', 'Apex Motors Owner', 'contact@apexrentals.com', '9876543211', 'AGENCY');

INSERT IGNORE INTO agencies (id, user_id, agency_name, registration_no, address, status)
VALUES (1, 2, 'Apex Vehicle Rentals', 'REG-APX-2024-01', 'MG Road, Kochi, Kerala', 'APPROVED');

-- 3. Pre-seeded Pending Agency (for testing Admin review/approval workflow)
INSERT IGNORE INTO users (id, username, password, full_name, email, phone, role)
VALUES (3, 'speedy', 'agency123', 'Speedy Wheels Owner', 'support@speedywheels.com', '9876543212', 'AGENCY');

INSERT IGNORE INTO agencies (id, user_id, agency_name, registration_no, address, status)
VALUES (2, 3, 'Speedy Wheels Co.', 'REG-SPD-2024-02', 'Marine Drive, Kochi, Kerala', 'PENDING');

-- 4. Pre-seeded Customer
INSERT IGNORE INTO users (id, username, password, full_name, email, phone, role)
VALUES (4, 'john', 'john123', 'John Doe', 'john.doe@example.com', '9876543213', 'CUSTOMER');

INSERT IGNORE INTO customers (id, user_id, license_number, address)
VALUES (1, 4, 'KL-07-20220001234', 'Panampilly Nagar, Kochi');

-- 5. Pre-seeded Vehicles (Cars, Bikes & EVs for Apex Rentals with rich condition ratings)
-- Car 1: Toyota Camry Hybrid
INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, 
                            seating_capacity, transmission, fuel_type, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified, is_electric)
VALUES (1, 1, 'CAR', 'Toyota', 'Camry Hybrid', 2023, 'KL-07-CD-1001', 50.00, 'AVAILABLE', 
        5, 'Automatic', 'Hybrid', 'Pearl White', '23 km/l', TRUE, TRUE, TRUE, TRUE, FALSE);

-- Car 2: Hyundai Tucson AWD
INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, 
                            seating_capacity, transmission, fuel_type, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified, is_electric)
VALUES (2, 1, 'CAR', 'Hyundai', 'Tucson AWD', 2022, 'KL-07-CD-1002', 65.00, 'AVAILABLE', 
        5, 'Automatic', 'Diesel', 'Phantom Black', '16 km/l', TRUE, TRUE, TRUE, TRUE, FALSE);

-- Car 3: Honda City ZX
INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, 
                            seating_capacity, transmission, fuel_type, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified, is_electric)
VALUES (3, 1, 'CAR', 'Honda', 'City ZX', 2023, 'KL-07-CD-1003', 42.00, 'AVAILABLE', 
        5, 'CVT Automatic', 'Petrol', 'Radiant Red', '18 km/l', TRUE, TRUE, TRUE, TRUE, FALSE);

-- Bike 1: Yamaha MT-15
INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, 
                            engine_cc, bike_type, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified, is_electric)
VALUES (4, 1, 'BIKE', 'Yamaha', 'MT-15 V2', 2023, 'KL-07-CD-2001', 20.00, 'AVAILABLE', 
        155, 'Sport', 'Metallic Cyan', '48 km/l', TRUE, TRUE, FALSE, TRUE, FALSE);

-- Bike 2: Royal Enfield Classic 350
INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, 
                            engine_cc, bike_type, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified, is_electric)
VALUES (5, 1, 'BIKE', 'Royal Enfield', 'Classic 350', 2022, 'KL-07-CD-2002', 28.00, 'AVAILABLE', 
        350, 'Cruiser', 'Gunmetal Grey', '36 km/l', TRUE, TRUE, FALSE, TRUE, FALSE);

-- EV Car: Tesla Model 3 EV
INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, 
                            seating_capacity, transmission, fuel_type, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified, 
                            is_electric, battery_capacity, range_km, charging_time)
VALUES (6, 1, 'CAR', 'Tesla', 'Model 3 EV', 2024, 'KL-07-EV-9999', 85.00, 'AVAILABLE', 
        5, 'Automatic', 'Electric', 'Midnight Silver', 'Zero Emission', TRUE, TRUE, TRUE, TRUE,
        TRUE, 75.0, 520, 0.5);

-- EV Bike: Ather 450X
INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, 
                            engine_cc, bike_type, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified, 
                            is_electric, battery_capacity, range_km, charging_time)
VALUES (7, 1, 'BIKE', 'Ather', '450X Gen 3', 2024, 'KL-07-EV-1111', 25.00, 'AVAILABLE', 
        0, 'Electric Scooter', 'Space Grey', 'Zero Emission', TRUE, TRUE, TRUE, TRUE,
        TRUE, 3.7, 145, 0.8);

-- 6. Sample Booking (John Doe renting Toyota Camry)
INSERT IGNORE INTO bookings (id, customer_id, vehicle_id, start_date, end_date, total_days, total_cost, status, notes, pricing_strategy)
VALUES (1, 1, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 3, 150.00, 'APPROVED', 'Standard booking for 3 days', 'Standard Daily Rate');

-- 7. Sample Notification
INSERT IGNORE INTO notifications (id, user_id, title, message)
VALUES (1, 4, 'Booking Approved', 'Your booking #1 for Toyota Camry Hybrid has been APPROVED by Apex Vehicle Rentals.');
