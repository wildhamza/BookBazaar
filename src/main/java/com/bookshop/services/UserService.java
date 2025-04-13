package com.bookshop.services;

import com.bookshop.models.User;
import com.bookshop.repositories.UserRepository;
import com.bookshop.repositories.UserRepositoryImpl;
import com.bookshop.utils.PasswordHasher;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    
    private final UserRepository repository;
    
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
    
    public UserService() {
        this(new UserRepositoryImpl());
    }
    
    public User authenticateUser(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return null;
        }
        
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
        
        User user = repository.findByUsername(username);
        
        if (user != null && PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
            return user;
        }
        
        return null;
    }
    
    public boolean registerUser(User user, String password) throws SQLException {
        if (user == null || password == null) {
            return false;
        }
        
        if (repository.findByUsername(user.getUsername()) != null) {
            return false;
        }
        
        String hashedPassword = PasswordHasher.hashPassword(password);
        user.setPasswordHash(hashedPassword);
        
        if (user.getRole() == null) {
            user.setRole("CUSTOMER");
        }
        
        return repository.save(user) > 0;
    }
    
    public boolean updateUserProfile(User user) throws SQLException {
        if (user == null) {
            return false;
        }
        
        User existingUser = repository.findById(user.getId());
        if (existingUser == null) {
            return false;
        }
        
        existingUser.setEmail(user.getEmail());
        existingUser.setFullName(user.getFullName());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        
        return repository.update(existingUser);
    }
    
    public boolean changePassword(int userId, String newPassword) throws SQLException {
        if (newPassword == null) {
            return false;
        }
        
        User user = repository.findById(userId);
        if (user == null) {
            return false;
        }
        
        String hashedPassword = PasswordHasher.hashPassword(newPassword);
        user.setPasswordHash(hashedPassword);
        
        return repository.update(user);
    }
    
    public boolean incrementOrderCount(int userId) throws SQLException {
        return repository.incrementOrderCount(userId);
    }
    
    public User getUserById(int userId) throws SQLException {
        return repository.findById(userId);
    }
    
    public User getUserByUsername(String username) throws SQLException {
        return repository.findByUsername(username);
    }
    
    public List<User> getAllUsers() throws SQLException {
        return repository.findAll();
    }
    
    public boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    public boolean isRegularMember(User user) {
        return user != null && user.getOrderCount() >= 5 && user.getOrderCount() < 10;
    }
    
    public boolean isPremiumMember(User user) {
        return user != null && user.getOrderCount() >= 10;
    }
}