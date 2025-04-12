package com.bookshop.models;

/**
 * Model class for users.
 */
public class User {
    
    /**
     * User roles.
     */
    public static class Role {
        public static final String ADMIN = "admin";
        public static final String CUSTOMER = "customer";
    }
    
    private int id;
    private String username;
    private String password;
    private String passwordHash; // For storing hashed password
    private String email;
    private String firstName;
    private String lastName;
    private String fullName; // Computed field
    private String phoneNumber;
    private String address;
    private String role; // "admin" or "customer"
    private int orderCount; // Used for discount eligibility
    
    /**
     * Default constructor.
     */
    public User() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor.
     * 
     * @param id The user ID
     * @param username The username
     * @param password The hashed password
     * @param email The email address
     * @param firstName The first name
     * @param lastName The last name
     * @param role The role ("admin" or "customer")
     */
    public User(int id, String username, String password, String email, 
            String firstName, String lastName, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.orderCount = 0;
    }
    
    /**
     * Get the user ID.
     * 
     * @return The user ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set the user ID.
     * 
     * @param id The user ID to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get the username.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Set the username.
     * 
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Get the password (hashed).
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Set the password (should be hashed).
     * 
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Get the email address.
     * 
     * @return The email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Set the email address.
     * 
     * @param email The email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Get the first name.
     * 
     * @return The first name
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * Set the first name.
     * 
     * @param firstName The first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * Get the last name.
     * 
     * @return The last name
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Set the last name.
     * 
     * @param lastName The last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Get the full name (first name + last name).
     * 
     * @return The full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Get the role.
     * 
     * @return The role
     */
    public String getRole() {
        return role;
    }
    
    /**
     * Set the role.
     * 
     * @param role The role to set
     */
    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * Check if the user is an admin.
     * 
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    /**
     * Get the order count.
     * 
     * @return The order count
     */
    public int getOrderCount() {
        return orderCount;
    }
    
    /**
     * Set the order count.
     * 
     * @param orderCount The order count to set
     */
    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
    
    /**
     * Increment the order count.
     */
    public void incrementOrderCount() {
        this.orderCount++;
    }
    
    /**
     * Check if the user is eligible for regular member discount (5+ orders).
     * 
     * @return true if the user is eligible, false otherwise
     */
    public boolean isRegularMember() {
        return orderCount >= 5;
    }
    
    /**
     * Check if the user is eligible for premium member discount (10+ orders).
     * 
     * @return true if the user is eligible, false otherwise
     */
    public boolean isPremiumMember() {
        return orderCount >= 10;
    }
    
    /**
     * Alias for isPremiumMember()
     */
    public boolean isPremiumLoyaltyMember() {
        return isPremiumMember();
    }
    
    /**
     * Alias for isRegularMember()
     */
    public boolean isRegularLoyaltyMember() {
        return isRegularMember();
    }
    
    /**
     * Set the full name directly.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
        
        // Try to split the full name into first and last name
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            this.firstName = parts[0];
            this.lastName = parts[1];
        } else {
            this.firstName = fullName;
            this.lastName = "";
        }
    }
    
    /**
     * Get the user's loyalty status as a string.
     */
    public String getLoyaltyStatus() {
        if (isPremiumLoyaltyMember()) {
            return "Premium Member";
        } else if (isRegularLoyaltyMember()) {
            return "Regular Member";
        } else {
            return "Standard Customer";
        }
    }
    
    /**
     * Get the password hash.
     */
    public String getPasswordHash() {
        return passwordHash;
    }
    
    /**
     * Set the password hash.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    /**
     * Get the phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * Set the phone number.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    /**
     * Get the address.
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * Set the address.
     */
    public void setAddress(String address) {
        this.address = address;
    }
}