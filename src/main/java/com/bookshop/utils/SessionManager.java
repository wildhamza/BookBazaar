package com.bookshop.utils;

import com.bookshop.models.Book;
import com.bookshop.models.Order;
import com.bookshop.models.User;
 
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private Book currentBook;
    private Order currentOrder;
     
    private SessionManager() { 
    }
     
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    } 
    public User getCurrentUser() {
        return currentUser;
    }
     
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public Book getCurrentBook() {
        return currentBook;
    }
     
    public void setCurrentBook(Book book) {
        this.currentBook = book;
    }
     
    public Order getCurrentOrder() {
        return currentOrder;
    }
     
    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
    }
     
    public Book getSelectedBook() {
        return currentBook;
    }
     
    public void clearSession() {
        currentUser = null;
        currentBook = null;
        currentOrder = null;
    }
     
    public void logout() {
        clearSession();
    }
     
    public boolean isLoggedIn() {
        return currentUser != null;
    }
     
    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}