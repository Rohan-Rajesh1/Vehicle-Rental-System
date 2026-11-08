package com.vehiclerental.models;

/**
 * Represents a customer who searches vehicles and books rentals.
 */
public class Customer extends User {
    private int customerId; // ID in customers table
    private String licenseNumber;
    private String address;

    public Customer() {
        super();
        setRole("CUSTOMER");
    }

    public Customer(int userId, String username, String password, String fullName, String email, String phone,
                    int customerId, String licenseNumber, String address) {
        super(userId, username, password, fullName, email, phone, "CUSTOMER");
        this.customerId = customerId;
        this.licenseNumber = licenseNumber;
        this.address = address;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
