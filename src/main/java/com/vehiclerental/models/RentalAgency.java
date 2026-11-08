package com.vehiclerental.models;

/**
 * Represents a registered vehicle rental service provider.
 */
public class RentalAgency extends User {
    private int agencyId; // ID in agencies table
    private String agencyName;
    private String registrationNo;
    private String address;
    private AgencyStatus status;

    public RentalAgency() {
        super();
        setRole("AGENCY");
        this.status = AgencyStatus.PENDING;
    }

    public RentalAgency(int userId, String username, String password, String fullName, String email, String phone,
                        int agencyId, String agencyName, String registrationNo, String address, AgencyStatus status) {
        super(userId, username, password, fullName, email, phone, "AGENCY");
        this.agencyId = agencyId;
        this.agencyName = agencyName;
        this.registrationNo = registrationNo;
        this.address = address;
        this.status = status;
    }

    public int getAgencyId() { return agencyId; }
    public void setAgencyId(int agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public AgencyStatus getStatus() { return status; }
    public void setStatus(AgencyStatus status) { this.status = status; }
}
