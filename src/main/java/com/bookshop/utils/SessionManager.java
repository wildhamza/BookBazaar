package com.bookshop.utils;

import com.bookshop.models.Book;
import com.bookshop.models.User;

/**
 * Utility class for managing the current user session.
 * Implements the Singleton design pattern.
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private Book currentBook;
    private User selectedCustomer; // For editing/viewing book details
    
    /**
     * Private constructor for Singleton pattern.
     */
    private SessionManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets the singleton instance of SessionManager.
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
     * Gets the current user.
     * 
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the current user.
     * 
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Gets the current book (for edit/view).
     * 
     * @return The current book, or null if none is set
     */
    public Book getCurrentBook() {
        return currentBook;
    }
    
    /**
     * Sets the current book (for edit/view).
     * 
     * @param book The book to set as current
     */
    public void setCurrentBook(Book book) {
        this.currentBook = book;
    }
    
    /**
     * Clears the current book.
     */
    public void clearCurrentBook() {
        this.currentBook = null;
    }
    
    /**
     * Checks if a user is logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Checks if the current user is an admin.
     * 
     * @return true if the current user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }
    
    /**
     * Gets the selected customer (for admin operations).
     * 
     * @return The selected customer, or null if none is set
     */
    public User getSelectedCustomer() {
        return selectedCustomer;
    }
    
    /**
     * Sets the selected customer (for admin operations).
     * 
     * @param customer The customer to set as selected
     */
    public void setSelectedCustomer(User customer) {
        this.selectedCustomer = customer;
    }
    
    /**
     * Alias for getCurrentBook for backward compatibility.
     * 
     * @return The current book
     */
    public Book getSelectedBook() {
        return currentBook;
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        this.currentUser = null;
        this.currentBook = null;
        this.selectedCustomer = null;
    }
}