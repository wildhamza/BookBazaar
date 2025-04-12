package com.bookshop.utils;

import com.bookshop.models.Book;
import com.bookshop.models.Order;
import com.bookshop.models.User;

/**
 * Singleton class to manage the current user session.
 * Implements the Singleton design pattern.
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private Book currentBook;
    private Order currentOrder;
    
    /**
     * Private constructor to prevent direct instantiation.
     */
    private SessionManager() {
        // Private constructor
    }
    
    /**
     * Get the singleton instance.
     * 
     * @return The singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Get the current user.
     * 
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Set the current user.
     * 
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get the current book (for editing or viewing details).
     * 
     * @return The current book
     */
    public Book getCurrentBook() {
        return currentBook;
    }
    
    /**
     * Set the current book (for editing or viewing details).
     * 
     * @param book The book to set as current
     */
    public void setCurrentBook(Book book) {
        this.currentBook = book;
    }
    
    /**
     * Get the current order (for viewing details).
     * 
     * @return The current order
     */
    public Order getCurrentOrder() {
        return currentOrder;
    }
    
    /**
     * Set the current order (for viewing details).
     * 
     * @param order The order to set as current
     */
    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
    }
    
    /**
     * Alias for getCurrentBook.
     */
    public Book getSelectedBook() {
        return currentBook;
    }
    
    /**
     * Clear the current session.
     */
    public void clearSession() {
        currentUser = null;
        currentBook = null;
        currentOrder = null;
    }
    
    /**
     * Logout the current user.
     */
    public void logout() {
        clearSession();
    }
    
    /**
     * Check if a user is logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if the current user is an admin.
     * 
     * @return true if the current user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}