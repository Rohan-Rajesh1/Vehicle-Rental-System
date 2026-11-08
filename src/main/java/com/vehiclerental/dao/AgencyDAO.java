package com.vehiclerental.dao;

import com.vehiclerental.db.DatabaseConnection;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.AgencyStatus;
import com.vehiclerental.models.RentalAgency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgencyDAO {

    /**
     * Returns all registered rental agencies.
     */
    public List<RentalAgency> getAllAgencies() throws DatabaseException {
        List<RentalAgency> list = new ArrayList<>();
        String sql = "SELECT u.id as user_id, u.username, u.full_name, u.email, u.phone, " +
                     "a.id as agency_id, a.agency_name, a.registration_no, a.address, a.status " +
                     "FROM users u JOIN agencies a ON u.id = a.user_id ORDER BY a.id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new RentalAgency(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        "",
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("agency_id"),
                        rs.getString("agency_name"),
                        rs.getString("registration_no"),
                        rs.getString("address"),
                        AgencyStatus.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch agencies: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Returns agencies filtered by string status (ALL, PENDING, APPROVED, REJECTED).
     */
    public List<RentalAgency> getAgenciesByStatus(String statusFilter) throws DatabaseException {
        if (statusFilter == null || statusFilter.toUpperCase().contains("ALL")) {
            return getAllAgencies();
        }
        String upper = statusFilter.trim().toUpperCase();
        if (upper.contains("ACCEPT") || upper.contains("APPROV")) {
            return getAgenciesByStatus(AgencyStatus.APPROVED);
        } else if (upper.contains("REJECT")) {
            return getAgenciesByStatus(AgencyStatus.REJECTED);
        } else if (upper.contains("PEND")) {
            return getAgenciesByStatus(AgencyStatus.PENDING);
        }
        return getAllAgencies();
    }

    /**
     * Returns agencies filtered by status (e.g. PENDING).
     */
    public List<RentalAgency> getAgenciesByStatus(AgencyStatus status) throws DatabaseException {
        List<RentalAgency> list = new ArrayList<>();
        String sql = "SELECT u.id as user_id, u.username, u.full_name, u.email, u.phone, " +
                     "a.id as agency_id, a.agency_name, a.registration_no, a.address, a.status " +
                     "FROM users u JOIN agencies a ON u.id = a.user_id WHERE a.status = ? ORDER BY a.id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RentalAgency(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            "",
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getInt("agency_id"),
                            rs.getString("agency_name"),
                            rs.getString("registration_no"),
                            rs.getString("address"),
                            AgencyStatus.valueOf(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch agencies by status: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Updates an agency's approval status (APPROVED / REJECTED).
     */
    public boolean updateAgencyStatus(int agencyId, AgencyStatus status) throws DatabaseException {
        String sql = "UPDATE agencies SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, agencyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update agency status: " + e.getMessage(), e);
        }
    }
}
