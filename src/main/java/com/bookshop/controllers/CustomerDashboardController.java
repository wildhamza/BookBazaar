package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.Order;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the customer dashboard view.
 */
public class CustomerDashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label loyaltyStatusLabel;
    
    @FXML
    private Button cartButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private ListView<Book> bookListView;
    
    @FXML
    private ListView<Order> orderListView;
    
    @FXML
    private Label statusLabel;
    
    private BookService bookService;
    private User currentUser;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        bookService = new BookService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            // If not logged in, redirect to login page
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        // Set the welcome message
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        
        // Set the loyalty status
        loyaltyStatusLabel.setText("Status: " + currentUser.getLoyaltyStatus());
        
        // Load books
        loadBooks();
        
        // Load orders
        loadOrders();
    }
    
    /**
     * Loads books from the database.
     */
    private void loadBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            bookListView.getItems().clear();
            bookListView.getItems().addAll(books);
        } catch (SQLException e) {
            statusLabel.setText("Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads orders for the current user from the database.
     */
    private void loadOrders() {
        // Implementation would load the user's orders from the database
        // This is a placeholder implementation
    }
    
    /**
     * Handles the view cart button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleViewCart(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("shopping_cart.fxml");
    }
    
    /**
     * Handles the logout button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        ViewNavigator.getInstance().navigateTo("login.fxml");
    }
}