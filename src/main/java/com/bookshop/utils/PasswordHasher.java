package com.bookshop.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing passwords and verifying password hashes.
 * Uses BCrypt for secure password hashing.
 */
public class PasswordHasher {
    
    // Workfactor for BCrypt (higher is more secure but slower)
    private static final int WORKFACTOR = 12;
    
    /**
     * Hashes a password using BCrypt
     * 
     * @param plainTextPassword The plain text password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        // Generate a salt with the specified work factor
        String salt = BCrypt.gensalt(WORKFACTOR);
        
        // Hash the password with the generated salt
        return BCrypt.hashpw(plainTextPassword, salt);
    }
    
    /**
     * Checks if a plain text password matches a hashed password
     * 
     * @param plainTextPassword The plain text password to check
     * @param hashedPassword The hashed password to check against
     * @return true if the passwords match, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        // Check if the password matches the hash
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
    
    /**
     * Migrates a plain text password to a hashed password.
     * This is useful when migrating from a system that stored plain text passwords.
     * 
     * @param plainTextPassword The plain text password to migrate
     * @return The hashed password
     */
    public static String migratePassword(String plainTextPassword) {
        return hashPassword(plainTextPassword);
    }
}