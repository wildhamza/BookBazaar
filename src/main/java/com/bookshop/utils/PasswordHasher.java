package com.bookshop.utils;

import org.mindrot.jbcrypt.BCrypt;
 
public class PasswordHasher {
     
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
     
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}