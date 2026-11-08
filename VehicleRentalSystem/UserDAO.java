package com.vehiclerental.dao;

import com.vehiclerental.db.DatabaseConnection;
import com.vehiclerental.exceptions.AuthenticationException;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Authenticates a user with username, password, and expected role.
     */
    public User authenticate(String username, String password, String expectedRole) 
            throws AuthenticationException, DatabaseException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, expectedRole);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AuthenticationException("Invalid username or password for role " + expectedRole);
                }

                int userId = rs.getInt("id");
                String uname = rs.getString("username");
                String pass = rs.getString("password");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String role = rs.getString("role");

                switch (role) {
                    case "ADMIN":
                        return new Admin(userId, uname, pass, fullName, email, phone);

                    case "CUSTOMER":
                        return loadCustomerDetails(conn, userId, uname, pass, fullName, email, phone);

                    case "AGENCY":
                        RentalAgency agency = loadAgencyDetails(conn, userId, uname, pass, fullName, email, phone);
                        if (agency.getStatus() == AgencyStatus.PENDING) {
                            throw new AuthenticationException("Your agency registration is pending approval by the Admin. Please wait for confirmation.");
                        } else if (agency.getStatus() == AgencyStatus.REJECTED) {
                            throw new AuthenticationException("Your agency registration was rejected. Please contact administration.");
                        }
                        return agency;

                    default:
                        throw new AuthenticationException("Unknown user role: " + role);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during authentication: " + e.getMessage(), e);
        }
    }

    private Customer loadCustomerDetails(Connection conn, int userId, String uname, String pass,
                                         String fullName, String email, String phone) throws SQLException {
        String sql = "SELECT * FROM customers WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(userId, uname, pass, fullName, email, phone,
                            rs.getInt("id"), rs.getString("license_number"), rs.getString("address"));
                }
            }
        }
        return new Customer(userId, uname, pass, fullName, email, phone, 0, "", "");
    }

    private RentalAgency loadAgencyDetails(Connection conn, int userId, String uname, String pass,
                                           String fullName, String email, String phone) throws SQLException {
        String sql = "SELECT * FROM agencies WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AgencyStatus status = AgencyStatus.valueOf(rs.getString("status"));
                    return new RentalAgency(userId, uname, pass, fullName, email, phone,
                            rs.getInt("id"), rs.getString("agency_name"), rs.getString("registration_no"),
                            rs.getString("address"), status);
                }
            }
        }
        return new RentalAgency(userId, uname, pass, fullName, email, phone, 0, "", "", "", AgencyStatus.PENDING);
    }

    /**
     * Registers a new customer into both users and customers tables in a transaction.
     */
    public boolean registerCustomer(Customer customer) throws DatabaseException {
        String userSql = "INSERT INTO users (username, password, full_name, email, phone, role) VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";
        String custSql = "INSERT INTO customers (user_id, license_number, address) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, customer.getUsername().trim());
                psUser.setString(2, customer.getPassword());
                psUser.setString(3, customer.getFullName().trim());
                psUser.setString(4, customer.getEmail().trim());
                psUser.setString(5, customer.getPhone().trim());
                psUser.executeUpdate();

                int userId = 0;
                try (ResultSet rs = psUser.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }

                try (PreparedStatement psCust = conn.prepareStatement(custSql, Statement.RETURN_GENERATED_KEYS)) {
                    psCust.setInt(1, userId);
                    psCust.setString(2, customer.getLicenseNumber().trim());
                    psCust.setString(3, customer.getAddress().trim());
                    psCust.executeUpdate();

                    try (ResultSet rsCust = psCust.getGeneratedKeys()) {
                        if (rsCust.next()) {
                            customer.setCustomerId(rsCust.getInt(1));
                        }
                    }
                }

                conn.commit();
                customer.setId(userId);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to register customer: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database transaction error: " + e.getMessage(), e);
        }
    }

    /**
     * Registers a new rental agency in users and agencies tables with PENDING status.
     */
    public boolean registerAgency(RentalAgency agency) throws DatabaseException {
        String userSql = "INSERT INTO users (username, password, full_name, email, phone, role) VALUES (?, ?, ?, ?, ?, 'AGENCY')";
        String agencySql = "INSERT INTO agencies (user_id, agency_name, registration_no, address, status) VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, agency.getUsername().trim());
                psUser.setString(2, agency.getPassword());
                psUser.setString(3, agency.getFullName().trim());
                psUser.setString(4, agency.getEmail().trim());
                psUser.setString(5, agency.getPhone().trim());
                psUser.executeUpdate();

                int userId = 0;
                try (ResultSet rs = psUser.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }

                try (PreparedStatement psAgency = conn.prepareStatement(agencySql, Statement.RETURN_GENERATED_KEYS)) {
                    psAgency.setInt(1, userId);
                    psAgency.setString(2, agency.getAgencyName().trim());
                    psAgency.setString(3, agency.getRegistrationNo().trim());
                    psAgency.setString(4, agency.getAddress().trim());
                    psAgency.executeUpdate();

                    try (ResultSet rsAg = psAgency.getGeneratedKeys()) {
                        if (rsAg.next()) {
                            agency.setAgencyId(rsAg.getInt(1));
                        }
                    }
                }

                conn.commit();
                agency.setId(userId);
                agency.setStatus(AgencyStatus.PENDING);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to register rental agency: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database transaction error: " + e.getMessage(), e);
        }
    }

    /**
     * Registers an Admin user.
     */
    public boolean registerAdmin(Admin admin) throws DatabaseException {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, role) VALUES (?, ?, ?, ?, ?, 'ADMIN')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, admin.getUsername().trim());
            ps.setString(2, admin.getPassword());
            ps.setString(3, admin.getFullName().trim());
            ps.setString(4, admin.getEmail().trim());
            ps.setString(5, admin.getPhone().trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    admin.setId(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to register admin: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all customers for Admin view.
     */
    public List<Customer> getAllCustomers() throws DatabaseException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT u.id as user_id, u.username, u.full_name, u.email, u.phone, " +
                     "c.id as customer_id, c.license_number, c.address " +
                     "FROM users u JOIN customers c ON u.id = c.user_id ORDER BY c.id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        "",
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("customer_id"),
                        rs.getString("license_number"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load customers: " + e.getMessage(), e);
        }
        return list;
    }
}
