package com.bookshop.models;

/**
 * Model class representing a user in the system.
 */
public class User {
    public enum Role {
        CUSTOMER,
        ADMIN
    }
    
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String address;
    private String phoneNumber;
    private Role role;
    
    // Default constructor
    public User() {
    }
    
    // Constructor with essential fields
    public User(int id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }
    
    // Full constructor
    public User(int id, String username, String passwordHash, String fullName, 
                String email, String address, String phoneNumber, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
