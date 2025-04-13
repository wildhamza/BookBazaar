package com.bookshop.repositories;

import com.bookshop.models.User;
import java.sql.SQLException;

public interface UserRepository extends Repository<User, Integer> {
    
    User findByUsername(String username) throws SQLException;
    
    User findByEmail(String email) throws SQLException;
    
    boolean updateOrderCount(int userId, int newOrderCount) throws SQLException;
    
    boolean incrementOrderCount(int userId) throws SQLException;
} 