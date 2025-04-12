package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
import com.bookshop.utils.SceneManager;
import com.bookshop.services.OrderService;
import com.bookshop.utils.DatabaseInitializer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

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
    private ListView<Order> completedOrdersListView;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TextField bookSearchField;
    
    @FXML
    private TextField customerSearchField;
    
    @FXML
    private TabPane mainTabPane;
    
    @FXML
    private TextArea completedOrderDetailsTextArea;
    
    private BookService bookService;
    private User currentUser;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        bookService = new BookService();
        UserService userService = new UserService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null || !currentUser.isAdmin()) {
            // If not logged in as admin, redirect to login page
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        // Set the welcome message
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        
        // Ensure we have test data
        try {
            DatabaseInitializer.createTestOrders();
        } catch (SQLException e) {
            System.err.println("Error creating test orders: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Configure bookListView with a custom cell factory to display book titles
        bookListView.setCellFactory(lv -> new ListCell<Book>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (empty || book == null) {
                    setText(null);
                } else {
                    setText(book.getTitle() + " by " + book.getAuthor() + " - $" + book.getPrice());
                }
            }
        });
        
        // Configure orderListView with a custom cell factory
        orderListView.setCellFactory(lv -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setText(null);
                } else {
                    setText("Order #" + order.getId() + " - $" + order.getTotalAmount() + " - " + order.getStatus());
                }
            }
        });
        
        // Configure completedOrdersListView with a custom cell factory
        completedOrdersListView.setCellFactory(lv -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setText(null);
                } else {
                    setText("Order #" + order.getId() + " - $" + order.getTotalAmount() + " - Completed on: " + order.getOrderDate());
                }
            }
        });
        
        // Set up double-click handler for orders list
        orderListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Order selectedOrder = orderListView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    showOrderDetailsDialog(selectedOrder);
                }
            }
        });
        
        // Add selection listener for completed orders
        completedOrdersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayCompletedOrderDetails(newSelection);
            } else {
                completedOrderDetailsTextArea.setText("");
            }
        });
        
        // Load books
        loadBooks();
        
        // Load orders
        loadOrders();
        
        // Load completed orders
        loadCompletedOrders();
        
        // Set up double-click handler for book list
        bookListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    try {
                        // Store the selected book in the session
                        SessionManager.getInstance().setCurrentBook(selectedBook);
                        // Navigate to book details view
                        ViewNavigator navigator = ViewNavigator.getInstance();
                        if (navigator.getStage() != null) {
                            navigator.navigateTo("book_details.fxml");
                        } else {
                            System.err.println("Warning: ViewNavigator stage is not set. Cannot navigate to book details.");
                            // Show fallback alert
                            Alert alert = new Alert(AlertType.WARNING);
                            alert.setTitle("Navigation Error");
                            alert.setHeaderText("Cannot navigate to book details");
                            alert.setContentText("The application is not properly initialized for navigation. Please restart the application.");
                            alert.showAndWait();
                        }
                    } catch (Exception e) {
                        System.err.println("Error navigating to book details: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    
    /**
     * Loads books from the database.
     */
    private void loadBooks() {
        try {
            System.out.println("AdminDashboardController: loadBooks() called");
            List<Book> books = bookService.getAllBooks();
            System.out.println("AdminDashboardController: Retrieved " + books.size() + " books from database");
            
            bookListView.getItems().clear();
            bookListView.getItems().addAll(books);
            
            System.out.println("AdminDashboardController: Added " + bookListView.getItems().size() + " books to bookListView");
            statusLabel.setText("Loaded " + books.size() + " books");
        } catch (SQLException e) {
            statusLabel.setText("Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads orders from the database.
     */
    private void loadOrders() {
        try {
            OrderService orderService = new OrderService();
            List<Order> orders = orderService.getAllOrders();
            orderListView.getItems().clear();
            orderListView.getItems().addAll(orders);
            System.out.println("Loaded " + orders.size() + " orders");
        } catch (SQLException e) {
            statusLabel.setText("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads completed orders from the database.
     */
    private void loadCompletedOrders() {
        try {
            OrderService orderService = new OrderService();
            List<Order> allOrders = orderService.getAllOrders();
            
            // Filter for completed orders (status is DELIVERED)
            List<Order> completedOrders = allOrders.stream()
                .filter(order -> order.getStatus() == Order.Status.DELIVERED)
                .collect(Collectors.toList());
            
            completedOrdersListView.getItems().clear();
            completedOrdersListView.getItems().addAll(completedOrders);
            System.out.println("Loaded " + completedOrders.size() + " completed orders");
        } catch (SQLException e) {
            statusLabel.setText("Error loading completed orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Displays details for the selected completed order.
     * 
     * @param order The selected order
     */
    private void displayCompletedOrderDetails(Order order) {
        try {
            StringBuilder details = new StringBuilder();
            details.append("Order #").append(order.getId()).append("\n");
            details.append("Date: ").append(order.getOrderDate()).append("\n");
            details.append("Customer ID: ").append(order.getUserId()).append("\n");
            details.append("Total Amount: $").append(order.getTotalAmount()).append("\n");
            details.append("Payment Method: ").append(order.getPaymentMethod()).append("\n\n");
            
            // Get order items
            OrderService orderService = new OrderService();
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            
            details.append("Items:").append("\n");
            for (OrderItem item : items) {
                // Get book details
                Book book = bookService.getBookById(item.getBookId());
                if (book != null) {
                    details.append(" - ").append(book.getTitle())
                           .append(" (").append(item.getQuantity()).append(" x $")
                           .append(item.getPrice()).append(") = $")
                           .append(item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                           .append("\n");
                } else {
                    details.append(" - Unknown Book (ID: ").append(item.getBookId())
                           .append(") - ").append(item.getQuantity()).append(" x $")
                           .append(item.getPrice()).append("\n");
                }
            }
            
            completedOrderDetailsTextArea.setText(details.toString());
        } catch (Exception e) {
            completedOrderDetailsTextArea.setText("Error loading order details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows a dialog with order details and option to mark as completed.
     * 
     * @param order The order to display and potentially update
     */
    private void showOrderDetailsDialog(Order order) {
        try {
            // Create dialog
            Alert dialog = new Alert(AlertType.INFORMATION);
            dialog.setTitle("Order Details");
            dialog.setHeaderText("Order #" + order.getId() + " Details");
            
            // Build order details
            StringBuilder details = new StringBuilder();
            details.append("Date: ").append(order.getOrderDate()).append("\n");
            details.append("Customer ID: ").append(order.getUserId()).append("\n");
            details.append("Status: ").append(order.getStatus()).append("\n");
            details.append("Total Amount: $").append(order.getTotalAmount()).append("\n");
            details.append("Payment Method: ").append(order.getPaymentMethod()).append("\n\n");
            
            // Get order items
            OrderService orderService = new OrderService();
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            
            details.append("Items:").append("\n");
            for (OrderItem item : items) {
                Book book = bookService.getBookById(item.getBookId());
                if (book != null) {
                    details.append(" - ").append(book.getTitle())
                           .append(" (").append(item.getQuantity()).append(" x $")
                           .append(item.getPrice()).append(") = $")
                           .append(item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                           .append("\n");
                } else {
                    details.append(" - Unknown Book (ID: ").append(item.getBookId())
                           .append(") - ").append(item.getQuantity()).append(" x $")
                           .append(item.getPrice()).append("\n");
                }
            }
            
            dialog.setContentText(details.toString());
            
            // Add mark as completed button if order is not already DELIVERED
            if (order.getStatus() != Order.Status.DELIVERED) {
                dialog.getButtonTypes().clear();
                dialog.getButtonTypes().addAll(
                    ButtonType.OK, 
                    new ButtonType("Mark as Completed", ButtonBar.ButtonData.APPLY)
                );
                
                // Handle button press
                dialog.showAndWait().ifPresent(response -> {
                    if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                        try {
                            orderService.updateOrderStatus(order.getId(), "DELIVERED");
                            statusLabel.setText("Order #" + order.getId() + " marked as completed");
                            
                            // Refresh both order lists
                            loadOrders();
                            loadCompletedOrders();
                        } catch (SQLException e) {
                            statusLabel.setText("Error updating order status: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                dialog.showAndWait();
            }
        } catch (Exception e) {
            statusLabel.setText("Error showing order details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles book search button click.
     * 
     * @param event The action event
     */
    @FXML
    public void handleBookSearch(ActionEvent event) {
        String searchText = bookSearchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadBooks(); // If search is empty, load all books
            return;
        }
        
        try {
            List<Book> allBooks = bookService.getAllBooks();
            List<Book> filteredBooks = allBooks.stream()
                .filter(book -> 
                    book.getTitle().toLowerCase().contains(searchText) || 
                    book.getAuthor().toLowerCase().contains(searchText))
                .collect(java.util.stream.Collectors.toList());
            
            bookListView.getItems().clear();
            bookListView.getItems().addAll(filteredBooks);
        } catch (SQLException e) {
            statusLabel.setText("Error searching books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles customer search button click.
     * 
     * @param event The action event
     */
    @FXML
    public void handleCustomerSearch(ActionEvent event) {
        String searchText = customerSearchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            // No need to reload customers if search is empty
            return;
        }
        
        try {
            UserService userService = new UserService();
            List<User> allUsers = userService.getAllUsers();
            // Filter users based on search text
            List<User> filteredUsers = allUsers.stream()
                .filter(user -> 
                    user.getUsername().toLowerCase().contains(searchText) || 
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchText)))
                .collect(java.util.stream.Collectors.toList());
            
            // Update customers list view
            // Since we removed the customers view, this method is no longer needed
            // We'll leave it in for future implementation
        } catch (SQLException e) {
            statusLabel.setText("Error searching customers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the add new book button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleAddNewBook(ActionEvent event) {
        // Clear any existing book in the session
        SessionManager.getInstance().setCurrentBook(null);
        try {
            // Navigate to edit book view for a new book
            ViewNavigator.getInstance().navigateTo("edit_book.fxml");
        } catch (Exception e) {
            System.err.println("Error navigating to edit book view: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Cannot navigate to edit book view");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
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
        try {
            ViewNavigator.getInstance().navigateTo("edit_book.fxml");
        } catch (Exception e) {
            System.err.println("Error navigating to edit book view: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Cannot navigate to edit book view");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
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
    
    /**
     * Handles the view orders button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleViewOrders(ActionEvent event) {
        // Simply switch to the Orders tab (tab index 1) and refresh orders
        mainTabPane.getSelectionModel().select(1); // Index 1 is the View Orders tab
        loadOrders();
    }
    
    /**
     * Handles the view completed orders button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleViewCompletedOrders(ActionEvent event) {
        // Switch to the Completed Orders tab (tab index 2) and refresh completed orders
        mainTabPane.getSelectionModel().select(2); // Index 2 is the Completed Orders tab
        loadCompletedOrders();
    }
    
    /**
     * Handles the manage books button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleManageBooks(ActionEvent event) {
        // Simply switch to the Books tab
        mainTabPane.getSelectionModel().select(0); // Index 0 is the Books tab
    }
}