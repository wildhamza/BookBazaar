package com.bookshop.services;

import com.bookshop.db.DatabaseConnection;
import com.bookshop.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for user-related operations.
 */
public class UserService {
    
    private Connection conn;
    
    /**
     * Constructor that initializes the database connection.
     * 
     * @throws SQLException If a database error occurs
     */
    public UserService() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }
    
    /**
     * Gets a user by ID.
     * 
     * @param id The user ID
     * @return The user with the specified ID
     * @throws SQLException If a database error occurs
     */
    public User getUserById(int id) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            
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
                    user.setRole(rs.getString("role").equals("ADMIN") ? User.Role.ADMIN : User.Role.CUSTOMER);
                    
                    return user;
                }
            }
        }
        
        return null; // User not found
    }
    
    /**
     * Gets all customers (non-admin users).
     * 
     * @return A list of all customers
     * @throws SQLException If a database error occurs
     */
    public List<User> getAllCustomers() throws SQLException {
        String query = "SELECT * FROM users WHERE role = 'CUSTOMER' ORDER BY username";
        
        List<User> customers = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setAddress(rs.getString("address"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setRole(User.Role.CUSTOMER);
                
                customers.add(user);
            }
        }
        
        return customers;
    }
    
    /**
     * Searches for customers by name or username.
     * 
     * @param searchTerm The search term
     * @return A list of matching customers
     * @throws SQLException If a database error occurs
     */
    public List<User> searchCustomers(String searchTerm) throws SQLException {
        String query = "SELECT * FROM users WHERE role = 'CUSTOMER' AND " +
                       "(LOWER(username) LIKE LOWER(?) OR LOWER(full_name) LIKE LOWER(?)) " +
                       "ORDER BY username";
        
        List<User> customers = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setString(2, "%" + searchTerm + "%");
            
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
                    user.setRole(User.Role.CUSTOMER);
                    
                    customers.add(user);
                }
            }
        }
        
        return customers;
    }
    
    /**
     * Updates a user's information.
     * 
     * @param user The user to update
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateUser(User user) throws SQLException {
        String query = "UPDATE users SET full_name = ?, email = ?, address = ?, " +
                       "phone_number = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getAddress());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setInt(5, user.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}
