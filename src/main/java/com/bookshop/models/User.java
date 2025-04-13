package com.bookshop.models;

public class User {
    
    public static class Role {
        public static final String ADMIN = "admin";
        public static final String CUSTOMER = "customer";
    }
    
    private int id;
    private String username;
    private String password;
    private String passwordHash;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String role;
    private int orderCount;
    
    public User() {
    }
    
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public int getOrderCount() {
        return orderCount;
    }
    
    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
    
    public void incrementOrderCount() {
        this.orderCount++;
    }
    
    public boolean isRegularMember() {
        return orderCount >= 5;
    }
    
    public boolean isPremiumMember() {
        return orderCount >= 10;
    }
    
    public boolean isPremiumLoyaltyMember() {
        return isPremiumMember();
    }
    
    public boolean isRegularLoyaltyMember() {
        return isRegularMember();
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getLoyaltyStatus() {
        if (isPremiumMember()) {
            return "Premium";
        } else if (isRegularMember()) {
            return "Regular";
        } else {
            return "Basic";
        }
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
}