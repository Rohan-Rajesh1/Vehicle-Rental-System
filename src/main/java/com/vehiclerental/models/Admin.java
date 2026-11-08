package com.vehiclerental.models;

/**
 * Represents the platform administrator.
 */
public class Admin extends User {
    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int id, String username, String password, String fullName, String email, String phone) {
        super(id, username, password, fullName, email, phone, "ADMIN");
    }
}
