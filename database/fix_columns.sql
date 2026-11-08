-- ====================================================================
-- VEHICLE RENTAL SYSTEM - INSTANT SCHEMA FIX SCRIPT
-- Safely adds missing columns without syntax or duplicate errors
-- ====================================================================

USE vehicle_rental_db;

DROP PROCEDURE IF EXISTS add_col_if_missing;

DELIMITER $$
CREATE PROCEDURE add_col_if_missing(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = tbl_name 
          AND COLUMN_NAME = col_name
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl_name, ' ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 1. Vehicle columns
CALL add_col_if_missing('vehicles', 'colour', 'VARCHAR(50) DEFAULT "Pearl White"');
CALL add_col_if_missing('vehicles', 'mileage', 'VARCHAR(50) DEFAULT "18 km/l"');
CALL add_col_if_missing('vehicles', 'tyre_condition', 'BOOLEAN DEFAULT TRUE');
CALL add_col_if_missing('vehicles', 'engine_condition', 'BOOLEAN DEFAULT TRUE');
CALL add_col_if_missing('vehicles', 'gps_enabled', 'BOOLEAN DEFAULT TRUE');
CALL add_col_if_missing('vehicles', 'insurance_verified', 'BOOLEAN DEFAULT TRUE');
CALL add_col_if_missing('vehicles', 'is_electric', 'BOOLEAN DEFAULT FALSE');
CALL add_col_if_missing('vehicles', 'battery_capacity', 'DECIMAL(6,2) DEFAULT NULL');
CALL add_col_if_missing('vehicles', 'range_km', 'INT DEFAULT NULL');
CALL add_col_if_missing('vehicles', 'charging_time', 'DECIMAL(4,2) DEFAULT NULL');

-- 2. Booking columns
CALL add_col_if_missing('bookings', 'pricing_strategy', 'VARCHAR(100) DEFAULT "Standard Daily Rate"');
CALL add_col_if_missing('bookings', 'penalty_fee', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_col_if_missing('bookings', 'fuel_fee', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_col_if_missing('bookings', 'platform_fee', 'DECIMAL(10,2) DEFAULT 0.00');
CALL add_col_if_missing('bookings', 'inspection_notes', 'TEXT DEFAULT NULL');

-- Clean up temporary procedure
DROP PROCEDURE IF EXISTS add_col_if_missing;

-- 3. Create reviews and notifications tables
CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    customer_id INT NOT NULL,
    agency_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
