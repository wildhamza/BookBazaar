package com.bookshop.models;

/**
 * Model class representing a user in the system.
 */
public class User {
    
    // User roles
    public enum Role {
        CUSTOMER,
        ADMIN
    }
    
    private int id;
    private String username;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phoneNumber;
    private boolean isAdmin;
    private int orderCount;  // Used for loyalty program
    private String loyaltyStatus;  // "Standard", "Regular", "Premium"
    private Role role;
    
    /**
     * Default constructor.
     */
    public User() {
        this.loyaltyStatus = "Standard"; // Default loyalty status
        this.role = Role.CUSTOMER; // Default role
    }
    
    /**
     * Constructor with all fields.
     */
    public User(int id, String username, String passwordHash, String firstName, String lastName, 
                String email, String address, String phoneNumber, boolean isAdmin, int orderCount, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.isAdmin = isAdmin;
        this.orderCount = orderCount;
        this.role = role;
        updateLoyaltyStatus();
    }

    /**
     * Gets the user ID.
     * 
     * @return The user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user ID.
     * 
     * @param id The user ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the username.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * 
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password hash.
     * 
     * @return The password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash.
     * 
     * @param passwordHash The password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the first name.
     * 
     * @return The first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     * 
     * @param firstName The first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name.
     * 
     * @return The last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     * 
     * @param lastName The last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Gets the full name (first name + last name).
     * 
     * @return The full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Sets the full name by parsing it into first name and last name.
     * 
     * @param fullName The full name (format: "First Last")
     */
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            this.firstName = "";
            this.lastName = "";
            return;
        }
        
        String[] parts = fullName.trim().split("\\s+", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }

    /**
     * Gets the email.
     * 
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     * 
     * @param email The email
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Gets the address.
     * 
     * @return The address
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * Sets the address.
     * 
     * @param address The address
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * Gets the phone number.
     * 
     * @return The phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * Sets the phone number.
     * 
     * @param phoneNumber The phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the user role.
     * 
     * @return The user role
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * Sets the user role.
     * 
     * @param role The user role
     */
    public void setRole(Role role) {
        this.role = role;
        this.isAdmin = (role == Role.ADMIN);
    }

    /**
     * Checks if the user is an admin.
     * 
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Sets whether the user is an admin.
     * 
     * @param isAdmin Whether the user is an admin
     */
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        if (isAdmin) {
            this.role = Role.ADMIN;
        } else {
            this.role = Role.CUSTOMER;
        }
    }
    
    /**
     * Gets the order count.
     * 
     * @return The order count
     */
    public int getOrderCount() {
        return orderCount;
    }
    
    /**
     * Sets the order count.
     * 
     * @param orderCount The order count
     */
    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
        updateLoyaltyStatus();
    }
    
    /**
     * Increments the order count by 1.
     */
    public void incrementOrderCount() {
        this.orderCount++;
        updateLoyaltyStatus();
    }
    
    /**
     * Gets the loyalty status.
     * 
     * @return The loyalty status
     */
    public String getLoyaltyStatus() {
        return loyaltyStatus;
    }
    
    /**
     * Checks if the user is a regular loyalty member (5+ orders).
     * 
     * @return true if the user is a regular loyalty member, false otherwise
     */
    public boolean isRegularLoyaltyMember() {
        return orderCount >= 5;
    }
    
    /**
     * Checks if the user is a premium loyalty member (10+ orders).
     * 
     * @return true if the user is a premium loyalty member, false otherwise
     */
    public boolean isPremiumLoyaltyMember() {
        return orderCount >= 10;
    }
    
    /**
     * Alias for isPremiumLoyaltyMember to maintain compatibility.
     * 
     * @return true if the user is a premium member, false otherwise
     */
    public boolean isPremiumMember() {
        return isPremiumLoyaltyMember();
    }
    
    /**
     * Updates the loyalty status based on the order count.
     */
    private void updateLoyaltyStatus() {
        if (orderCount >= 10) {
            loyaltyStatus = "Premium";
        } else if (orderCount >= 5) {
            loyaltyStatus = "Regular";
        } else {
            loyaltyStatus = "Standard";
        }
    }
    
    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + ", address=" + address + ", phoneNumber=" + phoneNumber 
                + ", role=" + role + ", isAdmin=" + isAdmin + ", orderCount=" + orderCount 
                + ", loyaltyStatus=" + loyaltyStatus + "]";
    }
}