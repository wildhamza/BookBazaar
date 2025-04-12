package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.Review;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.CartService;
import com.bookshop.services.ReviewService;
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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        cartService = new CartService();
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
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, currentBook.getQuantity(), 1);
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
            stockLabel.setText(currentBook.getQuantity() + " in stock");
            stockLabel.setStyle("-fx-text-fill: green;");
        } else {
            stockLabel.setText("Out of stock");
            stockLabel.setStyle("-fx-text-fill: red;");
        }
        
        // Format rating
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        ratingLabel.setText(decimalFormat.format(currentBook.getAverageRating()) + 
                            " ★ (" + currentBook.getReviewCount() + " reviews)");
        
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
            reviewsTableView.setItems(FXCollections.observableArrayList(reviews));
        } catch (SQLException e) {
            statusLabel.setText("Error loading reviews: " + e.getMessage());
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
        
        if (quantity > currentBook.getQuantity()) {
            statusLabel.setText("Not enough books in stock");
            return;
        }
        
        try {
            boolean success = cartService.addToCart(currentUser.getId(), currentBook.getId(), quantity);
            
            if (success) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Added to Cart");
                alert.setHeaderText(null);
                alert.setContentText(quantity + " copies of \"" + currentBook.getTitle() + "\" added to your cart.");
                alert.showAndWait();
                
                // Return to the dashboard
                ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
            } else {
                statusLabel.setText("Failed to add to cart");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
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
            
            boolean success = reviewService.addReview(review);
            
            if (success) {
                // Refresh reviews
                loadReviews();
                
                // Clear input
                reviewTextField.clear();
                ratingSpinner.getValueFactory().setValue(5);
                
                // Hide review box
                addReviewBox.setVisible(false);
                
                // Show success message
                statusLabel.setText("Review added successfully");
                statusLabel.setStyle("-fx-text-fill: green;");
                
                // Reload book to update rating
                try {
                    currentBook = bookService.getBookById(currentBook.getId());
                    SessionManager.getInstance().setCurrentBook(currentBook);
                    displayBookDetails();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                statusLabel.setText("Failed to add review");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error adding review: " + e.getMessage());
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