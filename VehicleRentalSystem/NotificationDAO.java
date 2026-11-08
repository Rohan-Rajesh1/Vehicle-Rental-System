package com.vehiclerental.dao;

import com.vehiclerental.db.DatabaseConnection;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.notifications.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for in-app user notifications.
 */
public class NotificationDAO {

    public boolean sendNotification(int userId, String title, String message) {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read) VALUES (?, ?, ?, FALSE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return false;
        }
    }

    public List<Notification> getNotificationsForUser(int userId) throws DatabaseException {
        String sql = "SELECT id, user_id, title, message, is_read, created_at FROM notifications " +
                     "WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("title"),
                            rs.getString("message"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load notifications: " + e.getMessage(), e);
        }
        return list;
    }

    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public void markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}
