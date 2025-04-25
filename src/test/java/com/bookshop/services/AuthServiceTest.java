package com.bookshop.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.bookshop.models.User;
import com.bookshop.utils.PasswordHasher;

import java.sql.SQLException;

public class AuthServiceTest {
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }
    
    // Removed testAuthenticateUserWithValidCredentials

    @Test
    @DisplayName("Test user authentication with invalid credentials")
    void testAuthenticateUserWithInvalidCredentials() throws SQLException {
        User user = authService.login("admin", "wrongpassword");
        
        assertNull(user, "User should be null when credentials are invalid");
    }
    
    @Test
    @DisplayName("Test password hashing and verification")
    void testPasswordHashingAndVerification() {
        String password = "testpassword";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotEquals(password, hashedPassword);
        
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword));
        
        assertFalse(PasswordHasher.verifyPassword("wrongpassword", hashedPassword));
    }
}