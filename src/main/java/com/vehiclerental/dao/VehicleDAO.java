package com.vehiclerental.dao;

import com.vehiclerental.db.DatabaseConnection;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    /**
     * Adds a new vehicle (Car or Bike) polymorphic entity into the database.
     */
    public boolean addVehicle(Vehicle vehicle) throws DatabaseException {
        String sql = "INSERT INTO vehicles (agency_id, vehicle_type, brand, model, year, license_plate, " +
                     "daily_rate, status, seating_capacity, transmission, fuel_type, engine_cc, bike_type, " +
                     "colour, mileage, tyre_condition, engine_condition, gps_enabled, insurance_verified, " +
                     "is_electric, battery_capacity, range_km, charging_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, vehicle.getAgencyId());
            ps.setString(2, vehicle.getVehicleType());
            ps.setString(3, vehicle.getBrand().trim());
            ps.setString(4, vehicle.getModel().trim());
            ps.setInt(5, vehicle.getYear());
            ps.setString(6, vehicle.getLicensePlate().trim());
            ps.setDouble(7, vehicle.getDailyRate());
            ps.setString(8, vehicle.getStatus().name());

            if (vehicle instanceof Car car) {
                ps.setInt(9, car.getSeatingCapacity());
                ps.setString(10, car.getTransmission());
                ps.setString(11, car.getFuelType());
                ps.setNull(12, Types.INTEGER);
                ps.setNull(13, Types.VARCHAR);
            } else if (vehicle instanceof Bike bike) {
                ps.setNull(9, Types.INTEGER);
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
                ps.setInt(12, bike.getEngineCc());
                ps.setString(13, bike.getBikeType());
            } else {
                ps.setNull(9, Types.INTEGER);
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
                ps.setNull(12, Types.INTEGER);
                ps.setNull(13, Types.VARCHAR);
            }

            // Rich specifications & condition tick marks
            ps.setString(14, vehicle.getColour());
            ps.setString(15, vehicle.getMileage());
            ps.setBoolean(16, vehicle.isTyreConditionGood());
            ps.setBoolean(17, vehicle.isEngineConditionGood());
            ps.setBoolean(18, vehicle.isGpsEnabled());
            ps.setBoolean(19, vehicle.isInsuranceVerified());

            if (vehicle instanceof ElectricCar ec) {
                ps.setBoolean(20, true);
                ps.setDouble(21, ec.getBatteryCapacityKwh());
                ps.setInt(22, ec.getRangeKm());
                ps.setDouble(23, ec.getChargingTimeHours());
            } else if (vehicle instanceof ElectricBike eb) {
                ps.setBoolean(20, true);
                ps.setDouble(21, eb.getBatteryCapacityKwh());
                ps.setInt(22, eb.getRangeKm());
                ps.setNull(23, Types.DECIMAL);
            } else {
                ps.setBoolean(20, false);
                ps.setNull(21, Types.DECIMAL);
                ps.setNull(22, Types.INTEGER);
                ps.setNull(23, Types.DECIMAL);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        vehicle.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add vehicle: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a vehicle by its ID.
     */
    public boolean deleteVehicle(int vehicleId) throws DatabaseException {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete vehicle: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing vehicle's status.
     */
    public boolean updateVehicleStatus(int vehicleId, VehicleStatus status) throws DatabaseException {
        String sql = "UPDATE vehicles SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, vehicleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update vehicle status: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all vehicles belonging to a specific agency.
     */
    public List<Vehicle> getVehiclesByAgency(int agencyId) throws DatabaseException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.*, a.agency_name FROM vehicles v " +
                     "JOIN agencies a ON v.agency_id = a.id " +
                     "WHERE v.agency_id = ? ORDER BY v.id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agencyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVehicle(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load agency vehicles: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Returns all vehicles platform-wide (Admin view).
     */
    public List<Vehicle> getAllVehicles() throws DatabaseException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.*, a.agency_name FROM vehicles v " +
                     "JOIN agencies a ON v.agency_id = a.id ORDER BY v.id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapVehicle(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load all vehicles: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Searches and browses all vehicles from approved agencies for customers.
     * Note: Rented vehicles are NOT removed; their status is displayed so customers can see them.
     */
    public List<Vehicle> searchAvailableVehicles(String typeFilter, String searchKeyword) throws DatabaseException {
        List<Vehicle> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT v.*, a.agency_name FROM vehicles v " +
                "JOIN agencies a ON v.agency_id = a.id " +
                "WHERE a.status = 'APPROVED' ");

        if (typeFilter != null && !typeFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND v.vehicle_type = ? ");
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append("AND (v.brand LIKE ? OR v.model LIKE ? OR v.colour LIKE ?) ");
        }
        sql.append("ORDER BY v.id ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (typeFilter != null && !typeFilter.equalsIgnoreCase("ALL")) {
                ps.setString(paramIndex++, typeFilter.toUpperCase());
            }
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String kw = "%" + searchKeyword.trim() + "%";
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVehicle(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search vehicles: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Maps ResultSet row to polymorphic Vehicle instance (Car or Bike) with rich specifications.
     */
    private Vehicle mapVehicle(ResultSet rs) throws SQLException {
        String type = rs.getString("vehicle_type");
        int id = rs.getInt("id");
        int agencyId = rs.getInt("agency_id");
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        int year = rs.getInt("year");
        String plate = rs.getString("license_plate");
        double dailyRate = rs.getDouble("daily_rate");
        VehicleStatus status = VehicleStatus.valueOf(rs.getString("status"));
        String agencyName = rs.getString("agency_name");

        // Rich specifications & condition tick marks
        String colour = getSafeString(rs, "colour", "White");
        String mileage = getSafeString(rs, "mileage", "18 km/l");
        boolean tyreCond = getSafeBoolean(rs, "tyre_condition", true);
        boolean engineCond = getSafeBoolean(rs, "engine_condition", true);
        boolean gps = getSafeBoolean(rs, "gps_enabled", true);
        boolean insurance = getSafeBoolean(rs, "insurance_verified", true);
        boolean isElectric = getSafeBoolean(rs, "is_electric", false);
        double batteryKwh = getSafeDouble(rs, "battery_capacity", 50.0);
        int rangeKm = getSafeInt(rs, "range_km", 350);
        double chargeHours = getSafeDouble(rs, "charging_time", 1.0);

        if ("CAR".equalsIgnoreCase(type)) {
            int seats = rs.getInt("seating_capacity");
            String trans = rs.getString("transmission");
            String fuel = rs.getString("fuel_type");
            if (isElectric || "Electric".equalsIgnoreCase(fuel)) {
                ElectricCar ec = new ElectricCar(id, agencyId, brand, model, year, plate, dailyRate, status,
                        seats, trans, batteryKwh, rangeKm, chargeHours,
                        colour, mileage, tyreCond, engineCond, gps, insurance);
                ec.setAgencyName(agencyName);
                return ec;
            } else {
                Car car = new Car(id, agencyId, brand, model, year, plate, dailyRate, status, seats, trans, fuel,
                                  colour, mileage, tyreCond, engineCond, gps, insurance);
                car.setAgencyName(agencyName);
                return car;
            }
        } else {
            int cc = rs.getInt("engine_cc");
            String bType = rs.getString("bike_type");
            if (isElectric || (bType != null && bType.toLowerCase().contains("electric"))) {
                ElectricBike eb = new ElectricBike(id, agencyId, brand, model, year, plate, dailyRate, status,
                        batteryKwh, rangeKm, colour, mileage, tyreCond, engineCond, gps, insurance);
                eb.setAgencyName(agencyName);
                return eb;
            } else {
                Bike bike = new Bike(id, agencyId, brand, model, year, plate, dailyRate, status, cc, bType,
                                     colour, mileage, tyreCond, engineCond, gps, insurance);
                bike.setAgencyName(agencyName);
                return bike;
            }
        }
    }

    private double getSafeDouble(ResultSet rs, String col, double def) {
        try {
            double v = rs.getDouble(col);
            return rs.wasNull() ? def : v;
        } catch (SQLException e) {
            return def;
        }
    }

    private int getSafeInt(ResultSet rs, String col, int def) {
        try {
            int v = rs.getInt(col);
            return rs.wasNull() ? def : v;
        } catch (SQLException e) {
            return def;
        }
    }

    private String getSafeString(ResultSet rs, String col, String def) {
        try {
            String val = rs.getString(col);
            return val != null ? val : def;
        } catch (SQLException e) {
            return def;
        }
    }

    private boolean getSafeBoolean(ResultSet rs, String col, boolean def) {
        try {
            return rs.getBoolean(col);
        } catch (SQLException e) {
            return def;
        }
    }
}
