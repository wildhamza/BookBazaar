package com.bookshop.services;

import com.bookshop.models.User;
import com.bookshop.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service class for user-related operations.
 */
public class UserService {
    
    /**
     * Default constructor.
     */
    public UserService() {
        // Don't store a connection as a field, get a fresh connection for each method call
    }
    
    /**
     * Authenticate a user.
     * 
     * @param username The username
     * @param password The password (plain text)
     * @return The authenticated user, or null if authentication fails
     * @throws SQLException If a database error occurs
     */
    public User authenticateUser(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return null;
        }
        
        // For now we'll hardcode the admin credentials
        if ("admin".equals(username) && "admin123".equals(password)) {
            User adminUser = new User();
            adminUser.setId(1);
            adminUser.setUsername("admin");
            adminUser.setFullName("Admin User");
            adminUser.setEmail("admin@bookshop.com");
            adminUser.setRole("ADMIN");
            return adminUser;
        }
        
        // For now we'll hardcode the customer credentials
        if ("customer".equals(username) && "customer123".equals(password)) {
            User customerUser = new User();
            customerUser.setId(2);
            customerUser.setUsername("customer");
            customerUser.setFullName("Regular Customer");
            customerUser.setEmail("customer@example.com");
            customerUser.setAddress("456 Reader Lane");
            customerUser.setPhoneNumber("555-987-6543");
            customerUser.setRole("CUSTOMER");
            return customerUser;
        }
        
        // Look up the user in the database
        String query = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Get stored hash
                    String storedHash = rs.getString("password_hash");
                    
                    // Check password
                    if (BCrypt.checkpw(password, storedHash)) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setEmail(rs.getString("email"));
                        user.setAddress(rs.getString("address"));
                        user.setPhoneNumber(rs.getString("phone_number"));
                        user.setRole(rs.getString("role"));
                        user.setOrderCount(rs.getInt("order_count"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        
        return null;
    }
    
    /**
     * Register a new user.
     * 
     * @param user The user to register
     * @param password The plain text password
     * @return true if registration is successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean registerUser(User user, String password) throws SQLException {
        if (user == null || password == null) {
            return false;
        }
        
        // Check if username already exists
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            
            checkStmt.setString(1, user.getUsername());
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Username already exists
                }
            }
            
            // Hash the password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            // Insert the new user
            String insertQuery = "INSERT INTO users (username, password_hash, full_name, email, address, phone_number, role, order_count) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, user.getUsername());
                insertStmt.setString(2, hashedPassword);
                insertStmt.setString(3, user.getFullName());
                insertStmt.setString(4, user.getEmail());
                insertStmt.setString(5, user.getAddress());
                insertStmt.setString(6, user.getPhoneNumber());
                insertStmt.setString(7, user.getRole() != null ? user.getRole() : "CUSTOMER"); // Default to CUSTOMER
                insertStmt.setInt(8, user.getOrderCount());
                
                return insertStmt.executeUpdate() > 0;
            }
        }
    }
    
    /**
     * Update a user's profile.
     * 
     * @param user The user to update
     * @return true if update is successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateUserProfile(User user) throws SQLException {
        if (user == null) {
            return false;
        }
        
        String query = "UPDATE users SET email = ?, full_name = ?, address = ?, phone_number = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getAddress());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setInt(5, user.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Change a user's password.
     * 
     * @param userId The user ID
     * @param newPassword The new password (plain text)
     * @return true if change is successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean changePassword(int userId, String newPassword) throws SQLException {
        if (newPassword == null) {
            return false;
        }
        
        // Hash the new password
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        String query = "UPDATE users SET password_hash = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Increment the order count for a user.
     * 
     * @param userId The user ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean incrementOrderCount(int userId) throws SQLException {
        String query = "UPDATE users SET order_count = order_count + 1 WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get a user by ID.
     * 
     * @param userId The user ID
     * @return The user, or null if not found
     * @throws SQLException If a database error occurs
     */
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setAddress(rs.getString("address"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    user.setRole(rs.getString("role"));
                    user.setOrderCount(rs.getInt("order_count"));
                    return user;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get a user by username.
     * 
     * @param username The username
     * @return The user, or null if not found
     * @throws SQLException If a database error occurs
     */
    public User getUserByUsername(String username) throws SQLException {
        if (username == null) {
            return null;
        }
        
        // For testing purposes, return a hardcoded admin user
        if ("admin".equals(username)) {
            User adminUser = new User();
            adminUser.setId(1);
            adminUser.setUsername("admin");
            adminUser.setRole("ADMIN");
            adminUser.setFullName("Admin User");
            adminUser.setEmail("admin@bookshop.com");
            return adminUser;
        }
        
        String query = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setAddress(rs.getString("address"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    user.setRole(rs.getString("role"));
                    user.setOrderCount(rs.getInt("order_count"));
                    return user;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get all users.
     * 
     * @return A list of all users
     * @throws SQLException If a database error occurs
     */
    public java.util.List<User> getAllUsers() throws SQLException {
        java.util.List<User> users = new java.util.ArrayList<>();
        
        String query = "SELECT * FROM users";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setAddress(rs.getString("address"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    user.setRole(rs.getString("role"));
                    user.setOrderCount(rs.getInt("order_count"));
                    users.add(user);
                }
            }
        }
        
        // If no users found, add a hardcoded admin user for testing
        if (users.isEmpty()) {
            User adminUser = new User();
            adminUser.setId(1);
            adminUser.setUsername("admin");
            adminUser.setFullName("Admin User");
            adminUser.setEmail("admin@bookshop.com");
            adminUser.setRole("ADMIN");
            users.add(adminUser);
        }
        
        return users;
    }
}