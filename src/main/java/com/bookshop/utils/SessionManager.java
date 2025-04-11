package com.bookshop.utils;

import com.bookshop.models.Book;
import com.bookshop.models.User;

/**
 * Singleton pattern implementation for managing user session.
 * Stores the current user and other session data.
 */
public class SessionManager {
    private static SessionManager instance;
    
    private User currentUser;
    private Book selectedBook;
    private User selectedCustomer;
    
    // Private constructor
    private SessionManager() {
    }
    
    /**
     * Gets the single instance of SessionManager.
     * 
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Gets the current logged-in user.
     * 
     * @return The current User or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the current user after successful login.
     * 
     * @param user The authenticated User
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Gets the currently selected book for detail view.
     * 
     * @return The selected Book or null if none selected
     */
    public Book getSelectedBook() {
        return selectedBook;
    }
    
    /**
     * Sets the selected book for viewing details.
     * 
     * @param book The Book to view details for
     */
    public void setSelectedBook(Book book) {
        this.selectedBook = book;
    }
    
    /**
     * Gets the selected customer for admin operations.
     * 
     * @return The selected User or null if none selected
     */
    public User getSelectedCustomer() {
        return selectedCustomer;
    }
    
    /**
     * Sets the selected customer for admin operations.
     * 
     * @param customer The User selected by admin
     */
    public void setSelectedCustomer(User customer) {
        this.selectedCustomer = customer;
    }
    
    /**
     * Clears all session data on logout.
     */
    public void clearSession() {
        currentUser = null;
        selectedBook = null;
        selectedCustomer = null;
    }
    
    /**
     * Checks if the current user is an administrator.
     * 
     * @return true if the current user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
