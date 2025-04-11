package com.bookshop.services;

import com.bookshop.db.DatabaseConnection;
import com.bookshop.models.User;
import com.bookshop.utils.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service for authentication-related operations.
 */
public class AuthService {
    
    private Connection conn;
    
    public AuthService() {
        conn = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Authenticates a user by username and password.
     * 
     * @param username The username
     * @param password The password in plain text
     * @return The authenticated User object, or null if authentication fails
     * @throws SQLException If a database error occurs
     */
    public User authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    
                    // Check if password matches
                    if (PasswordHasher.checkPassword(password, storedHash)) {
                        // Create user object
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setPasswordHash(storedHash); // Store hash for security checks
                        user.setFullName(rs.getString("full_name"));
                        user.setEmail(rs.getString("email"));
                        user.setAddress(rs.getString("address"));
                        user.setPhoneNumber(rs.getString("phone_number"));
                        user.setRole(rs.getString("role").equals("ADMIN") ? User.Role.ADMIN : User.Role.CUSTOMER);
                        
                        return user;
                    }
                }
            }
        }
        
        return null; // Authentication failed
    }
    
    /**
     * Registers a new customer user.
     * 
     * @param username The username
     * @param password The password in plain text
     * @param fullName The user's full name
     * @param email The user's email
     * @param address The user's shipping address
     * @param phoneNumber The user's phone number
     * @return The newly created User object
     * @throws SQLException If a database error occurs
     */
    public User registerCustomer(String username, String password, String fullName, 
                                 String email, String address, String phoneNumber) throws SQLException {
        
        // Check if username already exists
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Hash the password
        String passwordHash = PasswordHasher.hashPassword(password);
        
        // Insert the new user
        String query = "INSERT INTO users (username, password_hash, full_name, email, address, phone_number, role) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setString(5, address);
            stmt.setString(6, phoneNumber);
            stmt.setString(7, "CUSTOMER"); // Default role for new registrations
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            int userId;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            
            // Create and return the new user object
            User newUser = new User();
            newUser.setId(userId);
            newUser.setUsername(username);
            newUser.setPasswordHash(passwordHash);
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setAddress(address);
            newUser.setPhoneNumber(phoneNumber);
            newUser.setRole(User.Role.CUSTOMER);
            
            return newUser;
        }
    }
    
    /**
     * Checks if a username already exists in the database.
     * 
     * @param username The username to check
     * @return true if the username exists, false otherwise
     * @throws SQLException If a database error occurs
     */
    private boolean usernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
}
