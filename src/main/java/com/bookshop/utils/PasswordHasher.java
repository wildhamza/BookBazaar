package com.bookshop.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing passwords with BCrypt.
 */
public class PasswordHasher {
    
    // Default cost factor for BCrypt
    private static final int BCRYPT_COST = 12;
    
    /**
     * Hashes a password using BCrypt.
     * 
     * @param plainTextPassword The password in plain text
     * @return The BCrypt hash of the password
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_COST));
    }
    
    /**
     * Checks if a plain text password matches a stored hash.
     * 
     * @param plainTextPassword The password in plain text
     * @param hashedPassword The stored BCrypt hash
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
