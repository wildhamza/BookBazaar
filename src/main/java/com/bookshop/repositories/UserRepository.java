package com.bookshop.repositories;

import com.bookshop.models.User;
import java.sql.SQLException;

/**
 * Repository interface for User entities.
 */
public interface UserRepository extends Repository<User, Integer> {
    
    /**
     * Find a user by username.
     * 
     * @param username The username to search for
     * @return The user, or null if not found
     * @throws SQLException If a database error occurs
     */
    User findByUsername(String username) throws SQLException;
    
    /**
     * Find a user by email.
     * 
     * @param email The email to search for
     * @return The user, or null if not found
     * @throws SQLException If a database error occurs
     */
    User findByEmail(String email) throws SQLException;
    
    /**
     * Update a user's order count.
     * 
     * @param userId The user ID
     * @param newOrderCount The new order count
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean updateOrderCount(int userId, int newOrderCount) throws SQLException;
    
    /**
     * Increment a user's order count by one.
     * 
     * @param userId The user ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean incrementOrderCount(int userId) throws SQLException;
} 