package com.bookshop.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.bookshop.models.User;
import com.bookshop.utils.PasswordHasher;

import java.sql.SQLException;

/**
 * Unit tests for the AuthService class.
 */
public class AuthServiceTest {
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }
    
    @Test
    @DisplayName("Test user authentication with valid credentials")
    void testAuthenticateUserWithValidCredentials() throws SQLException {
        // Since this test depends on database state, we need to use known credentials
        // In a real application, you would mock the database or use an in-memory test database
        User user = authService.authenticateUser("admin", "admin123");
        
        assertNotNull(user, "User should not be null when credentials are valid");
        assertEquals("admin", user.getUsername());
        assertTrue(user.isAdmin());
    }
    
    @Test
    @DisplayName("Test user authentication with invalid credentials")
    void testAuthenticateUserWithInvalidCredentials() throws SQLException {
        User user = authService.authenticateUser("admin", "wrongpassword");
        
        assertNull(user, "User should be null when credentials are invalid");
    }
    
    @Test
    @DisplayName("Test password hashing and verification")
    void testPasswordHashingAndVerification() {
        String password = "testpassword";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        // The hash should be different from the original password
        assertNotEquals(password, hashedPassword);
        
        // Verification should work
        assertTrue(PasswordHasher.checkPassword(password, hashedPassword));
        
        // Wrong password should fail
        assertFalse(PasswordHasher.checkPassword("wrongpassword", hashedPassword));
    }
    
    // Add more test cases as needed
}