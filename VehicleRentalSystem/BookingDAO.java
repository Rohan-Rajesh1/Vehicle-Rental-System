package com.vehiclerental.dao;

import com.vehiclerental.db.DatabaseConnection;
import com.vehiclerental.exceptions.BookingException;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.Booking;
import com.vehiclerental.models.BookingStatus;
import com.vehiclerental.models.VehicleStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    /**
     * Checks whether a vehicle is available for the specified date range.
     * Prevents overlapping confirmed/approved bookings.
     */
    public boolean isVehicleAvailableForDates(int vehicleId, Date startDate, Date endDate) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM bookings " +
                     "WHERE vehicle_id = ? AND status = 'APPROVED' " +
                     "AND NOT (end_date < ? OR start_date > ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setDate(2, startDate);
            ps.setDate(3, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check vehicle availability: " + e.getMessage(), e);
        }
    }

    /**
     * Submits a new customer booking request.
     */
    public boolean createBooking(Booking booking) throws BookingException, DatabaseException {
        // Validate dates
        if (booking.getStartDate().after(booking.getEndDate())) {
            throw new BookingException("Start date cannot be after end date.");
        }
        if (!isVehicleAvailableForDates(booking.getVehicleId(), booking.getStartDate(), booking.getEndDate())) {
            throw new BookingException("This vehicle is already booked for the selected dates. Please choose another date range.");
        }

        String sql = "INSERT INTO bookings (customer_id, vehicle_id, start_date, end_date, total_days, total_cost, status, notes, pricing_strategy, platform_fee) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getCustomerId());
            ps.setInt(2, booking.getVehicleId());
            ps.setDate(3, booking.getStartDate());
            ps.setDate(4, booking.getEndDate());
            ps.setInt(5, booking.getTotalDays());
            ps.setDouble(6, booking.getTotalCost());
            ps.setString(7, booking.getNotes());
            ps.setString(8, booking.getPricingStrategyName());
            ps.setDouble(9, booking.getPlatformFee());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        booking.setId(rs.getInt(1));
                    }
                }
                booking.setStatus(BookingStatus.PENDING);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    /**
     * Completes return inspection, recording odometer/fuel/penalty settlement,
     * setting status to RETURNED, and restoring vehicle status to AVAILABLE.
     */
    public boolean completeReturnInspection(int bookingId, double fuelFee, double penaltyFee, String inspectionNotes) throws DatabaseException {
        String sql = "UPDATE bookings SET status = 'RETURNED', fuel_fee = ?, penalty_fee = ?, inspection_notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, fuelFee);
                ps.setDouble(2, penaltyFee);
                ps.setString(3, inspectionNotes);
                ps.setInt(4, bookingId);
                int affected = ps.executeUpdate();

                if (affected > 0) {
                    int vehicleId = getVehicleIdForBooking(conn, bookingId);
                    if (vehicleId > 0) {
                        vehicleDAO.updateVehicleStatus(vehicleId, VehicleStatus.AVAILABLE);
                    }
                }
                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to complete return inspection: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database transaction error: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the status of a booking (APPROVED, REJECTED, RETURNED).
     */
    public boolean updateBookingStatus(int bookingId, BookingStatus newStatus) throws DatabaseException {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus.name());
                ps.setInt(2, bookingId);
                int affected = ps.executeUpdate();

                // If approved, update vehicle status to RENTED; if returned, update to AVAILABLE
                if (affected > 0) {
                    int vehicleId = getVehicleIdForBooking(conn, bookingId);
                    if (vehicleId > 0) {
                        if (newStatus == BookingStatus.APPROVED) {
                            vehicleDAO.updateVehicleStatus(vehicleId, VehicleStatus.RENTED);
                        } else if (newStatus == BookingStatus.RETURNED) {
                            vehicleDAO.updateVehicleStatus(vehicleId, VehicleStatus.AVAILABLE);
                        }
                    }
                }
                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to update booking status: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database transaction error: " + e.getMessage(), e);
        }
    }

    private int getVehicleIdForBooking(Connection conn, int bookingId) throws SQLException {
        String sql = "SELECT vehicle_id FROM bookings WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Retrieves all bookings for a given customer.
     */
    public List<Booking> getBookingsByCustomer(int customerId) throws DatabaseException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name as cust_name, u.phone as cust_phone, " +
                     "v.brand, v.model, v.license_plate, v.vehicle_type, a.id as agency_id, a.agency_name " +
                     "FROM bookings b " +
                     "JOIN customers c ON b.customer_id = c.id " +
                     "JOIN users u ON c.user_id = u.id " +
                     "JOIN vehicles v ON b.vehicle_id = v.id " +
                     "JOIN agencies a ON v.agency_id = a.id " +
                     "WHERE b.customer_id = ? ORDER BY b.id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get customer bookings: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Retrieves all booking requests for a given agency's vehicles.
     */
    public List<Booking> getBookingsByAgency(int agencyId) throws DatabaseException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name as cust_name, u.phone as cust_phone, " +
                     "v.brand, v.model, v.license_plate, v.vehicle_type, a.id as agency_id, a.agency_name " +
                     "FROM bookings b " +
                     "JOIN customers c ON b.customer_id = c.id " +
                     "JOIN users u ON c.user_id = u.id " +
                     "JOIN vehicles v ON b.vehicle_id = v.id " +
                     "JOIN agencies a ON v.agency_id = a.id " +
                     "WHERE a.id = ? ORDER BY b.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agencyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get agency bookings: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Retrieves all platform bookings (Admin view).
     */
    public List<Booking> getAllBookings() throws DatabaseException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name as cust_name, u.phone as cust_phone, " +
                     "v.brand, v.model, v.license_plate, v.vehicle_type, a.id as agency_id, a.agency_name " +
                     "FROM bookings b " +
                     "JOIN customers c ON b.customer_id = c.id " +
                     "JOIN users u ON c.user_id = u.id " +
                     "JOIN vehicles v ON b.vehicle_id = v.id " +
                     "JOIN agencies a ON v.agency_id = a.id " +
                     "ORDER BY b.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load all bookings: " + e.getMessage(), e);
        }
        return list;
    }

    public Booking getBookingById(int bookingId) throws DatabaseException {
        String sql = "SELECT b.*, u.full_name as cust_name, u.phone as cust_phone, " +
                     "v.brand, v.model, v.license_plate, v.vehicle_type, a.id as agency_id, a.agency_name " +
                     "FROM bookings b " +
                     "JOIN customers c ON b.customer_id = c.id " +
                     "JOIN users u ON c.user_id = u.id " +
                     "JOIN vehicles v ON b.vehicle_id = v.id " +
                     "JOIN agencies a ON v.agency_id = a.id " +
                     "WHERE b.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBooking(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load booking details: " + e.getMessage(), e);
        }
        return null;
    }

    public int getCustomerUserId(int customerId) {
        String sql = "SELECT user_id FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public int getAgencyUserId(int agencyId) {
        String sql = "SELECT user_id FROM agencies WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agencyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setCustomerName(rs.getString("cust_name"));
        b.setCustomerPhone(rs.getString("cust_phone"));
        b.setVehicleId(rs.getInt("vehicle_id"));
        b.setVehicleInfo(rs.getString("brand") + " " + rs.getString("model") + " (" + rs.getString("license_plate") + ")");
        b.setVehicleType(rs.getString("vehicle_type"));
        b.setAgencyId(rs.getInt("agency_id"));
        b.setAgencyName(rs.getString("agency_name"));
        b.setStartDate(rs.getDate("start_date"));
        b.setEndDate(rs.getDate("end_date"));
        b.setTotalDays(rs.getInt("total_days"));
        b.setTotalCost(rs.getDouble("total_cost"));
        b.setStatus(BookingStatus.valueOf(rs.getString("status")));
        b.setNotes(rs.getString("notes"));
        b.setCreatedAt(rs.getTimestamp("created_at"));

        b.setPricingStrategyName(getSafeString(rs, "pricing_strategy", "Standard Rate"));
        b.setPenaltyFee(getSafeDouble(rs, "penalty_fee", 0.0));
        b.setFuelFee(getSafeDouble(rs, "fuel_fee", 0.0));
        b.setPlatformFee(getSafeDouble(rs, "platform_fee", Math.round(b.getTotalCost() * 0.05 * 100.0) / 100.0));
        b.setInspectionNotes(getSafeString(rs, "inspection_notes", ""));
        return b;
    }

    private String getSafeString(ResultSet rs, String col, String def) {
        try {
            String val = rs.getString(col);
            return val != null ? val : def;
        } catch (SQLException e) {
            return def;
        }
    }

    private double getSafeDouble(ResultSet rs, String col, double def) {
        try {
            double val = rs.getDouble(col);
            return rs.wasNull() ? def : val;
        } catch (SQLException e) {
            return def;
        }
    }
}
