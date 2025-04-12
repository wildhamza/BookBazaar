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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the admin dashboard view.
 */
public class AdminDashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private ListView<Book> bookListView;
    
    @FXML
    private ListView<Order> orderListView;
    
    @FXML
    private Button addBookButton;
    
    @FXML
    private Button editBookButton;
    
    @FXML
    private Button deleteBookButton;
    
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
        
        if (currentUser == null || !currentUser.isAdmin()) {
            // If not logged in as admin, redirect to login page
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        // Set the welcome message
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        
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
     * Loads all orders from the database.
     */
    private void loadOrders() {
        // Implementation would load all orders from the database
        // This is a placeholder implementation
    }
    
    /**
     * Handles the add book button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleAddBook(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("edit_book.fxml");
    }
    
    /**
     * Handles the edit book button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleEditBook(ActionEvent event) {
        Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
        
        if (selectedBook == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Book Selected");
            alert.setContentText("Please select a book to edit.");
            alert.showAndWait();
            return;
        }
        
        // Store the selected book in the session for the edit view
        SessionManager.getInstance().setCurrentBook(selectedBook);
        ViewNavigator.getInstance().navigateTo("edit_book.fxml");
    }
    
    /**
     * Handles the delete book button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleDeleteBook(ActionEvent event) {
        Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
        
        if (selectedBook == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Book Selected");
            alert.setContentText("Please select a book to delete.");
            alert.showAndWait();
            return;
        }
        
        // Confirm deletion
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Book");
        alert.setContentText("Are you sure you want to delete the book: " + selectedBook.getTitle() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    bookService.deleteBook(selectedBook.getId());
                    loadBooks(); // Refresh the list
                    statusLabel.setText("Book deleted successfully.");
                } catch (SQLException e) {
                    statusLabel.setText("Error deleting book: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
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