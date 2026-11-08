package com.vehiclerental.db;

import com.vehiclerental.exceptions.DatabaseException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Singleton database connection manager utilizing JDBC.
 */
public class DatabaseConnection {
    private static String url = "jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "";
    private static boolean driverLoaded = false;
    private static volatile boolean schemaMigrated = false;

    static {
        loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("WARNING: MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    private static void loadProperties() {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                if (props.getProperty("db.url") != null) url = props.getProperty("db.url").trim();
                if (props.getProperty("db.user") != null) user = props.getProperty("db.user").trim();
                if (props.getProperty("db.password") != null) password = props.getProperty("db.password").trim();
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load db.properties, using defaults: " + e.getMessage());
        }
    }

    /**
     * Establishes and returns a new database connection.
     * 
     * @return Connection object
     * @throws DatabaseException if connection fails
     */
    public static Connection getConnection() throws DatabaseException {
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            if (!schemaMigrated) {
                schemaMigrated = true;
                autoMigrateSchema(conn);
            }
            return conn;
        } catch (SQLException e) {
            throw new DatabaseException("Database connection error: Unable to connect to MySQL at " + url + 
                    ". Please ensure MySQL server is running and database 'vehicle_rental_db' exists. Error: " + e.getMessage(), e);
        }
    }

    /**
     * Checks whether connection to MySQL succeeds and auto-migrates schema columns if needed.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static void autoMigrateSchema(Connection conn) {
        if (conn == null) return;

        // 1. Migrate vehicle rich specs & EV columns
        ensureColumn(conn, "vehicles", "colour", "VARCHAR(50) DEFAULT 'Pearl White'");
        ensureColumn(conn, "vehicles", "mileage", "VARCHAR(50) DEFAULT '18 km/l'");
        ensureColumn(conn, "vehicles", "tyre_condition", "BOOLEAN DEFAULT TRUE");
        ensureColumn(conn, "vehicles", "engine_condition", "BOOLEAN DEFAULT TRUE");
        ensureColumn(conn, "vehicles", "gps_enabled", "BOOLEAN DEFAULT TRUE");
        ensureColumn(conn, "vehicles", "insurance_verified", "BOOLEAN DEFAULT TRUE");
        ensureColumn(conn, "vehicles", "is_electric", "BOOLEAN DEFAULT FALSE");
        ensureColumn(conn, "vehicles", "battery_capacity", "DECIMAL(6,2) DEFAULT NULL");
        ensureColumn(conn, "vehicles", "range_km", "INT DEFAULT NULL");
        ensureColumn(conn, "vehicles", "charging_time", "DECIMAL(4,2) DEFAULT NULL");

        // 2. Migrate booking financial & inspection columns
        ensureColumn(conn, "bookings", "pricing_strategy", "VARCHAR(100) DEFAULT 'Standard Daily Rate'");
        ensureColumn(conn, "bookings", "penalty_fee", "DECIMAL(10,2) DEFAULT 0.00");
        ensureColumn(conn, "bookings", "fuel_fee", "DECIMAL(10,2) DEFAULT 0.00");
        ensureColumn(conn, "bookings", "platform_fee", "DECIMAL(10,2) DEFAULT 0.00");
        ensureColumn(conn, "bookings", "inspection_notes", "TEXT DEFAULT NULL");

        // 3. Create reviews and notifications tables
        String[] tableStatements = new String[]{
            "CREATE TABLE IF NOT EXISTS reviews (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "booking_id INT NOT NULL, " +
                "customer_id INT NOT NULL, " +
                "agency_id INT NOT NULL, " +
                "vehicle_id INT NOT NULL, " +
                "rating INT NOT NULL, " +
                "comment TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",

            "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "title VARCHAR(150) NOT NULL, " +
                "message TEXT NOT NULL, " +
                "is_read BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",

            // Seed sample EV Car if not present (Tesla Model 3 for Apex Rentals, agency_id=1)
            "INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, seating_capacity, transmission, fuel_type, is_electric, battery_capacity, range_km, charging_time, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified) " +
            "VALUES (6, 1, 'CAR', 'Tesla', 'Model 3 EV', 2024, 'KL-07-EV-9999', 85.00, 'AVAILABLE', 5, 'Automatic', 'Electric', TRUE, 75.0, 520, 0.5, 'Midnight Silver', 'Zero Emission', TRUE, TRUE, TRUE, TRUE)",

            // Seed sample EV Bike (Ather 450X)
            "INSERT IGNORE INTO vehicles (id, agency_id, vehicle_type, brand, model, year, license_plate, daily_rate, status, engine_cc, bike_type, is_electric, battery_capacity, range_km, charging_time, colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified) " +
            "VALUES (7, 1, 'BIKE', 'Ather', '450X Gen 3', 2024, 'KL-07-EV-1111', 25.00, 'AVAILABLE', 0, 'Electric Scooter', TRUE, 3.7, 145, 0.8, 'Space Grey', 'Zero Emission', TRUE, TRUE, TRUE, TRUE)",

            // Ensure demo customer john / john123 exists
            "INSERT IGNORE INTO users (id, username, password, full_name, email, phone, role) " +
            "VALUES (4, 'john', 'john123', 'John Doe', 'john.doe@example.com', '9876543213', 'CUSTOMER')",
            "UPDATE users SET username = 'john', password = 'john123' WHERE id = 4 OR username = 'john_doe'",
            "INSERT IGNORE INTO customers (id, user_id, license_number, address) " +
            "VALUES (1, 4, 'KL-07-20220001234', 'Panampilly Nagar, Kochi')",

            // Ensure demo agency apex / apex123 exists and is approved
            "INSERT IGNORE INTO users (id, username, password, full_name, email, phone, role) " +
            "VALUES (2, 'apex', 'apex123', 'Apex Motors Owner', 'contact@apexrentals.com', '9876543211', 'AGENCY')",
            "UPDATE users SET username = 'apex', password = 'apex123' WHERE id = 2 OR username = 'apex'",
            "INSERT IGNORE INTO agencies (id, user_id, agency_name, registration_no, address, status) " +
            "VALUES (1, 2, 'Apex Vehicle Rentals', 'REG-APX-2024-01', 'MG Road, Kochi, Kerala', 'APPROVED')",
            "UPDATE agencies SET status = 'APPROVED' WHERE id = 1"
        };

        for (String sql : tableStatements) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException ignored) {
            }
        }
    }

    private static void ensureColumn(Connection conn, String table, String column, String definition) {
        if (!hasColumn(conn, table, column)) {
            String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("-> Schema migration: added column " + table + "." + column);
            } catch (SQLException e) {
                // Ignore if dialect variant or already present
            }
        }
    }

    private static boolean hasColumn(Connection conn, String table, String column) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table, column)) {
                if (rs.next()) return true;
            }
            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table.toUpperCase(), column)) {
                if (rs.next()) return true;
            }
            String q = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setString(1, table);
                ps.setString(2, column);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static String getUrl() { return url; }
    public static String getUser() { return user; }
    public static boolean isDriverLoaded() { return driverLoaded; }

    public static void setCredentials(String newUrl, String newUser, String newPassword) {
        url = newUrl;
        user = newUser;
        password = newPassword;
    }
}
