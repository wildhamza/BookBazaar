package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.Review;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.CartService;
import com.bookshop.services.ReviewService;
import com.bookshop.utils.DatabaseConnection;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Controller for the book details view.
 * Implements role-based UI that shows different controls depending on 
 * whether the user is a customer or administrator.
 */
public class BookDetailsController {
    
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label isbnLabel;
    @FXML private Label priceLabel;
    @FXML private Label categoryLabel;
    @FXML private Label stockLabel;
    @FXML private Label ratingLabel;
    @FXML private TextArea descriptionTextArea;
    
    @FXML private Label imageUrlLabel;
    @FXML private Label bookImageLabel;
    @FXML private Label statusLabel;
    
    @FXML private HBox customerActionBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addToCartButton;
    
    @FXML private HBox adminActionBox;
    @FXML private Button editBookButton;
    @FXML private Button deleteBookButton;
    
    @FXML private TableView<Review> reviewsTableView;
    @FXML private TableColumn<Review, String> reviewUserColumn;
    @FXML private TableColumn<Review, String> reviewDateColumn;
    @FXML private TableColumn<Review, String> reviewRatingColumn;
    @FXML private TableColumn<Review, String> reviewContentColumn;
    
    @FXML private HBox addReviewBox;
    @FXML private TextField reviewTextField;
    @FXML private Spinner<Integer> ratingSpinner;
    @FXML private Button addReviewButton;
    
    private Book currentBook;
    private User currentUser;
    private BookService bookService;
    private CartService cartService;
    private ReviewService reviewService;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        bookService = new BookService();
        cartService = CartService.getInstance();
        reviewService = new ReviewService();
        
        currentUser = SessionManager.getInstance().getCurrentUser();
        currentBook = SessionManager.getInstance().getCurrentBook();
        
        if (currentUser == null) {
            // If not logged in, redirect to login page
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        if (currentBook == null) {
            // If no book selected, go back to dashboard
            if (currentUser.isAdmin()) {
                ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
            } else {
                ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
            }
            return;
        }
        
        // Setup UI based on role
        setupRoleBasedUI();
        
        // Setup table columns for reviews
        reviewUserColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        
        reviewDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = cellData.getValue().getReviewDate().format(formatter);
            return new SimpleStringProperty(formattedDate);
        });
        
        reviewRatingColumn.setCellValueFactory(cellData -> {
            int rating = cellData.getValue().getRating();
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < rating; i++) {
                stars.append("★");
            }
            return new SimpleStringProperty(stars.toString());
        });
        
        reviewContentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContent()));
        
        // Display book details
        displayBookDetails();
        
        // Load reviews
        loadReviews();
    }
    
    /**
     * Sets up the UI based on the user's role.
     */
    private void setupRoleBasedUI() {
        if (currentUser.isAdmin()) {
            // Admin view
            adminActionBox.setVisible(true);
            customerActionBox.setVisible(false);
            addReviewBox.setVisible(false);
        } else {
            // Customer view
            adminActionBox.setVisible(false);
            customerActionBox.setVisible(true);
            
            // Check if customer has already reviewed this book
            try {
                boolean hasReviewed = reviewService.hasUserReviewedBook(currentUser.getId(), currentBook.getId());
                addReviewBox.setVisible(!hasReviewed);
            } catch (SQLException e) {
                addReviewBox.setVisible(true); // Default to allowing reviews if check fails
                e.printStackTrace();
            }
            
            // Configure quantity spinner
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, currentBook.getStockQuantity(), 1);
            quantitySpinner.setValueFactory(valueFactory);
            
            // Disable add to cart if no stock
            addToCartButton.setDisable(!currentBook.isInStock());
        }
    }
    
    /**
     * Displays book details.
     */
    private void displayBookDetails() {
        titleLabel.setText(currentBook.getTitle());
        authorLabel.setText(currentBook.getAuthor());
        isbnLabel.setText(currentBook.getIsbn());
        
        // Format price
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        priceLabel.setText(currencyFormat.format(currentBook.getPrice()));
        
        categoryLabel.setText(currentBook.getCategory());
        
        // Format stock status
        if (currentBook.isInStock()) {
            stockLabel.setText(currentBook.getStockQuantity() + " in stock");
            stockLabel.setStyle("-fx-text-fill: green;");
        } else {
            stockLabel.setText("Out of stock");
            stockLabel.setStyle("-fx-text-fill: red;");
        }
        
        // Format rating - count reviews dynamically for now
        try {
            List<Review> reviews = reviewService.getBookReviews(currentBook.getId());
            double averageRating = 0;
            if (!reviews.isEmpty()) {
                int totalRating = 0;
                for (Review review : reviews) {
                    totalRating += review.getRating();
                }
                averageRating = (double) totalRating / reviews.size();
            }
            
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            ratingLabel.setText(decimalFormat.format(averageRating) + 
                               " ★ (" + reviews.size() + " reviews)");
        } catch (SQLException e) {
            // Default if rating can't be calculated
            ratingLabel.setText("No ratings yet");
            e.printStackTrace();
        }
        
        descriptionTextArea.setText(currentBook.getDescription());
        
        // Display image URL or placeholder
        if (currentBook.getImageUrl() != null && !currentBook.getImageUrl().isEmpty()) {
            bookImageLabel.setText("Image: " + currentBook.getImageUrl());
        } else {
            bookImageLabel.setText("No image available");
        }
    }
    
    /**
     * Loads reviews for the current book.
     */
    private void loadReviews() {
        try {
            List<Review> reviews = reviewService.getBookReviews(currentBook.getId());
            System.out.println("Loaded " + reviews.size() + " reviews for book ID " + currentBook.getId());
            
            // Debug review contents
            for (Review review : reviews) {
                System.out.println("Review #" + review.getId() + 
                                 " by User:" + review.getUserId() + "/" + review.getUsername() + 
                                 " Rating:" + review.getRating() + 
                                 " Content: " + (review.getContent() != null ? review.getContent().substring(0, Math.min(20, review.getContent().length())) + "..." : "null"));
            }
            
            reviewsTableView.setItems(FXCollections.observableArrayList(reviews));
        } catch (SQLException e) {
            statusLabel.setText("Error loading reviews: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the add to cart button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleAddToCart(ActionEvent event) {
        int quantity = quantitySpinner.getValue();
        
        if (quantity <= 0) {
            statusLabel.setText("Quantity must be greater than 0");
            return;
        }
        
        if (quantity > currentBook.getStockQuantity()) {
            statusLabel.setText("Not enough books in stock");
            return;
        }
        
        try {
            // Ensure user exists in database for hardcoded customer
            if (currentUser.getId() == 2 && "customer".equals(currentUser.getUsername())) {
                // Try to create the customer user if not exists
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO users (id, username, password_hash, full_name, email, address, phone_number, role, order_count) " +
                         "VALUES (2, 'customer', '$2a$12$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Regular Customer', " +
                         "'customer@example.com', '456 Reader Lane', '555-987-6543', 'CUSTOMER', 0) " +
                         "ON CONFLICT (username) DO NOTHING")) {
                    stmt.executeUpdate();
                } catch (Exception e) {
                    System.out.println("Note: Customer user already exists or couldn't be created: " + e.getMessage());
                    // Continue anyway, as the user might already exist
                }
            }
            
            boolean success = cartService.addToCart(currentUser.getId(), currentBook.getId(), quantity);
            
            if (success) {
                // Show success alert
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Added to Cart");
                alert.setHeaderText(null);
                alert.setContentText(quantity + " copies of \"" + currentBook.getTitle() + "\" added to your cart.");
                
                // Add a little more detail to the dialog
                ButtonType viewCartButton = new ButtonType("View Cart");
                ButtonType continueShoppingButton = new ButtonType("Continue Shopping");
                alert.getButtonTypes().setAll(viewCartButton, continueShoppingButton);
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == viewCartButton) {
                        // Navigate to shopping cart
                        ViewNavigator.getInstance().navigateTo("shopping_cart.fxml");
                    } else {
                        // Return to customer dashboard
                        ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
                    }
                });
            } else {
                statusLabel.setText("Failed to add to cart");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            
            // Show detailed error in an alert for debugging
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Error adding to cart");
            alert.setContentText("Error: " + e.getMessage());
            
            // Add exception stacktrace to the alert
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();
            
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            alert.getDialogPane().setExpandableContent(textArea);
            alert.showAndWait();
            
            // Return to dashboard
            ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
        }
    }
    
    /**
     * Handles the add review button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleAddReview(ActionEvent event) {
        String content = reviewTextField.getText().trim();
        int rating = ratingSpinner.getValue();
        
        if (content.isEmpty()) {
            statusLabel.setText("Review content cannot be empty");
            return;
        }
        
        try {
            Review review = new Review();
            review.setUserId(currentUser.getId());
            review.setBookId(currentBook.getId());
            review.setRating(rating);
            review.setContent(content);
            review.setUsername(currentUser.getUsername()); // For display
            
            statusLabel.setText("Adding review...");
            statusLabel.setStyle("-fx-text-fill: black;");
            
            boolean success = reviewService.addReview(review);
            
            if (success) {
                // Clear input
                reviewTextField.clear();
                ratingSpinner.getValueFactory().setValue(5);
                
                // Hide review box
                addReviewBox.setVisible(false);
                
                // Show success message
                statusLabel.setText("Review added successfully");
                statusLabel.setStyle("-fx-text-fill: green;");
                
                // Refresh reviews directly instead of reloading book
                loadReviews();
                
                // Update the book display - recalculate rating based on latest reviews
                displayBookDetails();
            } else {
                statusLabel.setText("Failed to add review");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error adding review: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("Unexpected error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the edit book button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleEditBook(ActionEvent event) {
        if (!currentUser.isAdmin()) {
            return;
        }
        
        // Navigate to edit book view
        ViewNavigator.getInstance().navigateTo("edit_book.fxml");
    }
    
    /**
     * Handles the delete book button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleDeleteBook(ActionEvent event) {
        if (!currentUser.isAdmin()) {
            return;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Book");
        alert.setContentText("Are you sure you want to delete the book: " + currentBook.getTitle() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = bookService.deleteBook(currentBook.getId());
                    
                    if (success) {
                        ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
                    } else {
                        statusLabel.setText("Failed to delete book");
                    }
                } catch (SQLException e) {
                    statusLabel.setText("Error deleting book: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Handles the back button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleBack(ActionEvent event) {
        if (currentUser.isAdmin()) {
            ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
        } else {
            ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
        }
    }
}