package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.Order;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.CartService;
import com.bookshop.services.CartService.CartUpdateListener;
import com.bookshop.services.PurchaseService;
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
public class CustomerDashboardController implements CartUpdateListener {
    
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
    
    @FXML
    private Label cartItemCountLabel;
    
    @FXML
    private Button viewCartButton;
    
    @FXML
    private Button viewOrdersButton;
    
    @FXML
    private Button refreshOrdersButton;
    
    private BookService bookService;
    private CartService cartService;
    private User currentUser;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        bookService = new BookService();
        cartService = CartService.getInstance();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            // If not logged in, redirect to login page
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        // Register as a listener for cart updates
        cartService.addCartUpdateListener(this);
        
        // Set the welcome message
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        
        // Set the loyalty status
        loyaltyStatusLabel.setText("Status: " + currentUser.getLoyaltyStatus());
        
        // Update cart item count
        updateCartItemCount();
        
        // Configure bookListView with a custom cell factory
        bookListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Book>() {
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
        orderListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Order>() {
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
        
        // Load books
        loadBooks();
        
        // Load orders
        loadOrders();
        
        // Set up double-click handler for book list
        bookListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    // Store the selected book in the session
                    SessionManager.getInstance().setCurrentBook(selectedBook);
                    // Navigate to book details view
                    ViewNavigator.getInstance().navigateTo("book_details.fxml");
                }
            }
        });
        
        // Set up double-click handler for orders list
        orderListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Order selectedOrder = orderListView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    // Navigate to order details view
                    SessionManager.getInstance().setCurrentOrder(selectedOrder);
                    ViewNavigator.getInstance().navigateTo("customer_orders.fxml");
                }
            }
        });
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
        try {
            System.out.println("CustomerDashboardController: Loading orders for user ID " + currentUser.getId());
            
            // Create a PurchaseService to fetch orders
            PurchaseService purchaseService = new PurchaseService();
            List<Order> orders = purchaseService.getOrdersByUserId(currentUser.getId());
            
            System.out.println("CustomerDashboardController: Found " + orders.size() + " orders");
            
            // Clear and update the orderListView
            orderListView.getItems().clear();
            orderListView.getItems().addAll(orders);
            
            // Show a message if no orders are found
            if (orders.isEmpty()) {
                statusLabel.setText("You have no orders yet");
            } else {
                statusLabel.setText("Found " + orders.size() + " orders");
            }
        } catch (SQLException e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error loading orders: " + e.getMessage());
        }
    }
    
    /**
     * Updates the cart item count label.
     */
    private void updateCartItemCount() {
        try {
            int count = cartService.getCartItemCount(currentUser.getId());
            cartItemCountLabel.setText(count + " item" + (count != 1 ? "s" : ""));
            
            // Enable or disable the view cart button based on cart content
            viewCartButton.setDisable(count == 0);
        } catch (SQLException e) {
            statusLabel.setText("Error getting cart count: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the view cart button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleViewCartButton(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("shopping_cart.fxml");
    }
    
    /**
     * Handles the view orders button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleViewOrdersButton(ActionEvent event) {
        System.out.println("Handling View Orders button click");
        try {
            ViewNavigator.getInstance().navigateTo("customer_orders.fxml");
        } catch (Exception e) {
            System.err.println("Error navigating to orders view: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error: Could not load orders view");
        }
    }
    
    /**
     * Handles the book table click action.
     * 
     * @param event The mouse event
     */
    @FXML
    public void handleBookTableClick(javafx.scene.input.MouseEvent event) {
        if (event.getClickCount() == 2) {
            Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                // Store the selected book in the session
                SessionManager.getInstance().setCurrentBook(selectedBook);
                // Navigate to book details view
                ViewNavigator.getInstance().navigateTo("book_details.fxml");
            }
        }
    }
    
    /**
     * Handles the add to cart button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleAddToCartButton(ActionEvent event) {
        Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            try {
                boolean success = cartService.addToCart(currentUser.getId(), selectedBook.getId(), 1);
                
                if (success) {
                    statusLabel.setText(selectedBook.getTitle() + " added to cart");
                    updateCartItemCount();
                } else {
                    statusLabel.setText("Failed to add to cart");
                }
            } catch (SQLException e) {
                statusLabel.setText("Error adding to cart: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Please select a book to add to cart");
        }
    }
    
    /**
     * Handles the logout button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleLogoutButton(ActionEvent event) {
        // Unregister as a listener when logging out
        cartService.removeCartUpdateListener(this);
        
        SessionManager.getInstance().logout();
        ViewNavigator.getInstance().navigateTo("login.fxml");
    }
    
    /**
     * Handles the logout action (used by the logout button in the FXML).
     * 
     * @param event The action event
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        // Call the existing logout handler
        handleLogoutButton(event);
    }
    
    /**
     * Handles cart updates from the CartService.
     * 
     * @param userId The ID of the user whose cart was updated
     */
    @Override
    public void onCartUpdated(int userId) {
        if (currentUser != null && currentUser.getId() == userId) {
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(this::updateCartItemCount);
        }
    }
    
    /**
     * Handles refreshing the orders list.
     * 
     * @param event The action event
     */
    @FXML
    public void handleRefreshOrders(ActionEvent event) {
        loadOrders();
        statusLabel.setText("Orders refreshed");
    }
}