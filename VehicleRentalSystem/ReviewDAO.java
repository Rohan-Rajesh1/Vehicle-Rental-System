package com.vehiclerental.dao;

import com.vehiclerental.db.DatabaseConnection;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer Reviews & Star Ratings.
 */
public class ReviewDAO {

    public boolean addReview(Review review) throws DatabaseException {
        String sql = "INSERT INTO reviews (booking_id, customer_id, agency_id, vehicle_id, rating, comment) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getBookingId());
            ps.setInt(2, review.getCustomerId());
            ps.setInt(3, review.getAgencyId());
            ps.setInt(4, review.getVehicleId());
            ps.setInt(5, review.getRating());
            ps.setString(6, review.getComment());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save review: " + e.getMessage(), e);
        }
    }

    public boolean hasCustomerReviewedBooking(int bookingId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM reviews WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking review status: " + e.getMessage(), e);
        }
        return false;
    }

    public double getAverageRatingForVehicle(int vehicleId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return Math.round(avg * 10.0) / 10.0;
                }
            }
        } catch (Exception ignored) {}
        return 5.0; // Default 5.0 for brand new listings without reviews
    }

    public double getAverageRatingForAgency(int agencyId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE agency_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agencyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return Math.round(avg * 10.0) / 10.0;
                }
            }
        } catch (Exception ignored) {}
        return 5.0;
    }

    public List<Review> getAllReviews() throws DatabaseException {
        String sql = "SELECT r.id, r.booking_id, r.customer_id, u.full_name AS customer_name, " +
                     "r.agency_id, r.vehicle_id, CONCAT(v.brand, ' ', v.model) AS vehicle_info, " +
                     "r.rating, r.comment, r.created_at " +
                     "FROM reviews r " +
                     "JOIN customers c ON r.customer_id = c.id " +
                     "JOIN users u ON c.user_id = u.id " +
                     "JOIN vehicles v ON r.vehicle_id = v.id " +
                     "ORDER BY r.created_at DESC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Review(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getInt("agency_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("vehicle_info"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load reviews: " + e.getMessage(), e);
        }
        return list;
    }
}
