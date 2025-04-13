package com.bookshop.services;

import com.bookshop.models.User; 
import com.bookshop.utils.SessionManager;

import java.sql.SQLException;

/**
 * Service class for authentication-related operations.
 */
public class AuthService {
    
    private final UserService userService;
    
    /**
     * Constructor with UserService dependency.
     * 
     * @param userService The UserService to use
     */
    public AuthService(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Default constructor that creates a default UserService.
     */
    public AuthService() {
        this(new UserService());
    }
    
    /**
     * Login a user.
     * 
     * @param username The username
     * @param password The password
     * @return The authenticated user, or null if authentication fails
     */
    public User login(String username, String password) {
        try {
            User user = userService.authenticateUser(username, password);
            
            if (user != null) {
                // Set the current user in the session
                SessionManager.getInstance().setCurrentUser(user);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error during login: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Register a new user.
     * 
     * @param username The username
     * @param password The password
     * @param fullName The full name
     * @param email The email
     * @param address The address
     * @param phoneNumber The phone number
     * @return The registered user, or null if registration fails
     */
    public User register(String username, String password, String fullName, String email, 
                        String address, String phoneNumber) {
        // Create a new user object
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhoneNumber(phoneNumber);
        user.setRole("CUSTOMER"); // Default role for new registrations
        user.setOrderCount(0); // New users start with 0 orders
        
        try {
            // Try to register the user
            boolean success = userService.registerUser(user, password);
            
            if (success) {
                // Retrieve the user to get the generated ID
                User registeredUser = userService.getUserByUsername(username);
                if (registeredUser != null) {
                    // Set the current user in the session
                    SessionManager.getInstance().setCurrentUser(registeredUser);
                }
                return registeredUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error during registration: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Logout the current user.
     */
    public void logout() {
        SessionManager.getInstance().setCurrentUser(null);
    }
    
    /**
     * Check if the current session has an active user.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return SessionManager.getInstance().getCurrentUser() != null;
    }
    
    /**
     * Check if the current user is an administrator.
     * 
     * @return true if the current user is an admin, false otherwise
     */
    public boolean isAdmin() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return userService.isAdmin(currentUser);
    }
    
    /**
     * Get the current user.
     * 
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }
    
    /**
     * Check if a user is a regular loyalty member (has made 5 or more orders).
     * 
     * @param user The user to check
     * @return true if the user is a regular member, false otherwise
     */
    public boolean isRegularMember(User user) {
        return userService.isRegularMember(user);
    }
    
    /**
     * Check if a user is a premium loyalty member (has made 10 or more orders).
     * 
     * @param user The user to check
     * @return true if the user is a premium member, false otherwise
     */
    public boolean isPremiumMember(User user) {
        return userService.isPremiumMember(user);
    }
    
    /**
     * Update a user's profile information.
     * 
     * @param user The user with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateProfile(User user) {
        try {
            boolean success = userService.updateUserProfile(user);
            
            if (success) {
                // Update the session with the latest user data
                User updatedUser = userService.getUserById(user.getId());
                if (updatedUser != null) {
                    SessionManager.getInstance().setCurrentUser(updatedUser);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating profile: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Change a user's password.
     * 
     * @param userId The user ID
     * @param newPassword The new password
     * @return true if the password was changed successfully, false otherwise
     */
    public boolean changePassword(int userId, String newPassword) {
        try {
            return userService.changePassword(userId, newPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        }
    }
}