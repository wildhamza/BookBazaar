package com.bookshop.services;

import com.bookshop.models.User;
import com.bookshop.utils.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service for handling user authentication and registration.
 */
public class AuthService {
    
    /**
     * Authenticates a user with the given username and password.
     * 
     * @param username The username to authenticate
     * @param password The password to authenticate
     * @return The authenticated user, or null if authentication fails
     * @throws SQLException If a database error occurs
     */
    public User authenticateUser(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return null;
        }
        
        // Implementation would fetch the user from the database and verify the password
        // This is a placeholder implementation for testing
        
        // For testing the admin user with BCrypt hash
        if ("admin".equals(username)) {
            String hashedPassword = PasswordHasher.hashPassword("admin123");
            if (PasswordHasher.checkPassword(password, hashedPassword)) {
                User admin = new User();
                admin.setId(1);
                admin.setUsername("admin");
                admin.setPasswordHash(hashedPassword);
                admin.setRole(User.Role.ADMIN);
                admin.setEmail("admin@bookshop.com");
                admin.setFullName("Admin User");
                return admin;
            }
        }
        
        // TODO: Implement the actual database operation
        return null;
    }
    
    /**
     * Registers a new user with the given information.
     * 
     * @param username The username for the new user
     * @param password The password for the new user
     * @param email The email for the new user
     * @param fullName The full name for the new user
     * @return The registered user, or null if registration fails
     * @throws SQLException If a database error occurs
     * @throws IllegalArgumentException If the username is already taken
     */
    public User registerUser(String username, String password, String email, String fullName) 
            throws SQLException {
        if (username == null || password == null || email == null) {
            throw new IllegalArgumentException("Username, password, and email cannot be null");
        }
        
        // Check if the username is already taken
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        // Hash the password
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        // Create the user object
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hashedPassword);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(User.Role.CUSTOMER);
        
        // Implementation would insert the user into the database and get the generated ID
        // This is a placeholder implementation
        
        // TODO: Implement the actual database operation
        return user;
    }
    
    /**
     * Checks if a username is already taken.
     * 
     * @param username The username to check
     * @return true if the username is taken, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean isUsernameTaken(String username) throws SQLException {
        // Implementation would check if the username exists in the database
        // This is a placeholder implementation
        
        // TODO: Implement the actual database operation
        return false;
    }
    
    /**
     * Updates a user's password.
     * 
     * @param userId The ID of the user to update
     * @param oldPassword The old password (for verification)
     * @param newPassword The new password
     * @return true if the password was updated, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updatePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        // Implementation would verify the old password and update it in the database
        // This is a placeholder implementation
        
        // TODO: Implement the actual database operation
        return false;
    }
    
    /**
     * Gets a user by their ID.
     * 
     * @param userId The ID of the user to get
     * @return The user with the specified ID, or null if not found
     * @throws SQLException If a database error occurs
     */
    public User getUserById(int userId) throws SQLException {
        // Implementation would fetch the user from the database by ID
        // This is a placeholder implementation
        
        // TODO: Implement the actual database operation
        return null;
    }
    
    /**
     * Gets a user by their username.
     * 
     * @param username The username of the user to get
     * @return The user with the specified username, or null if not found
     * @throws SQLException If a database error occurs
     */
    public User getUserByUsername(String username) throws SQLException {
        // Implementation would fetch the user from the database by username
        // This is a placeholder implementation
        
        // TODO: Implement the actual database operation
        return null;
    }
    
    /**
     * Registers a new customer with the given information.
     * 
     * @param username The username for the new customer
     * @param password The password for the new customer
     * @param firstName The first name for the new customer
     * @param lastName The last name for the new customer
     * @param email The email for the new customer
     * @param phoneNumber The phone number for the new customer
     * @param address The address for the new customer
     * @return The registered customer, or null if registration fails
     * @throws SQLException If a database error occurs
     * @throws IllegalArgumentException If the username is already taken
     */
    public User registerCustomer(String username, String password, String firstName, String lastName, 
                               String email, String phoneNumber, String address) throws SQLException {
        if (username == null || password == null || email == null) {
            throw new IllegalArgumentException("Username, password, and email cannot be null");
        }
        
        // Check if the username is already taken
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        // Hash the password
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        // Create the user object
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hashedPassword);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setRole(User.Role.CUSTOMER);
        
        // Implementation would insert the user into the database and get the generated ID
        // This is a placeholder implementation
        
        // TODO: Implement the actual database operation
        return user;
    }
}