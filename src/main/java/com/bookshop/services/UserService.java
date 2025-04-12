package com.bookshop.services;

import com.bookshop.models.User;
import com.bookshop.repositories.UserRepository;
import com.bookshop.repositories.UserRepositoryImpl;
import com.bookshop.utils.PasswordHasher;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for user-related operations.
 * This class follows the Service Layer pattern, providing an abstraction layer between
 * controllers and the data access layer (repositories).
 */
public class UserService {
    
    private final UserRepository repository;
    
    /**
     * Constructor with the repository dependency.
     * 
     * @param repository The UserRepository implementation to use
     */
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Default constructor that creates a default repository implementation.
     */
    public UserService() {
        this(new UserRepositoryImpl());
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
        
        // For demo purposes, we'll hardcode admin and customer credentials
        if ("admin".equals(username) && "admin123".equals(password)) {
            User adminUser = new User();
            adminUser.setId(1);
            adminUser.setUsername("admin");
            adminUser.setFullName("Admin User");
            adminUser.setEmail("admin@bookshop.com");
            adminUser.setRole("ADMIN");
            return adminUser;
        }
        
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
        User user = repository.findByUsername(username);
        
        if (user != null && PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
            return user;
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
        if (repository.findByUsername(user.getUsername()) != null) {
            return false; // Username already exists
        }
        
        // Hash the password
        String hashedPassword = PasswordHasher.hashPassword(password);
        user.setPasswordHash(hashedPassword);
        
        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole("CUSTOMER");
        }
        
        // Save the user
        return repository.save(user) > 0;
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
        
        // Get existing user to preserve fields that shouldn't be updated
        User existingUser = repository.findById(user.getId());
        if (existingUser == null) {
            return false;
        }
        
        // Update only profile fields
        existingUser.setEmail(user.getEmail());
        existingUser.setFullName(user.getFullName());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        
        return repository.update(existingUser);
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
        
        // Get existing user
        User user = repository.findById(userId);
        if (user == null) {
            return false;
        }
        
        // Hash the new password
        String hashedPassword = PasswordHasher.hashPassword(newPassword);
        user.setPasswordHash(hashedPassword);
        
        return repository.update(user);
    }
    
    /**
     * Increment a user's order count.
     * 
     * @param userId The user ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean incrementOrderCount(int userId) throws SQLException {
        return repository.incrementOrderCount(userId);
    }
    
    /**
     * Get a user by ID.
     * 
     * @param userId The user ID
     * @return The user, or null if not found
     * @throws SQLException If a database error occurs
     */
    public User getUserById(int userId) throws SQLException {
        return repository.findById(userId);
    }
    
    /**
     * Get a user by username.
     * 
     * @param username The username
     * @return The user, or null if not found
     * @throws SQLException If a database error occurs
     */
    public User getUserByUsername(String username) throws SQLException {
        return repository.findByUsername(username);
    }
    
    /**
     * Get all users.
     * 
     * @return List of all users
     * @throws SQLException If a database error occurs
     */
    public List<User> getAllUsers() throws SQLException {
        return repository.findAll();
    }
    
    /**
     * Check if a user has the admin role.
     * 
     * @param user The user to check
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    /**
     * Check if a user is a regular member (has 5 or more orders).
     * 
     * @param user The user to check
     * @return true if the user is a regular member, false otherwise
     */
    public boolean isRegularMember(User user) {
        return user != null && user.getOrderCount() >= 5 && user.getOrderCount() < 10;
    }
    
    /**
     * Check if a user is a premium member (has 10 or more orders).
     * 
     * @param user The user to check
     * @return true if the user is a premium member, false otherwise
     */
    public boolean isPremiumMember(User user) {
        return user != null && user.getOrderCount() >= 10;
    }
}