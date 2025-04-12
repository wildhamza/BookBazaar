package com.bookshop.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for securely hashing and verifying passwords using BCrypt.
 * BCrypt is a password hashing function designed to be slow and resistant to brute-force attacks.
 */
public class PasswordHasher {
    
    /**
     * Hash a password using BCrypt.
     * 
     * @param password The plain text password to hash
     * @return The hashed password
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Verify a plain text password against a hashed password.
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to verify against
     * @return true if the passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}