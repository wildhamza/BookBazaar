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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.PrintWriter;
import java.io.StringWriter;


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
    
    @FXML private ImageView bookImageView;
    @FXML private Label imageErrorLabel;
    
    private Book currentBook;
    private User currentUser;
    private BookService bookService;
    private CartService cartService;
    private ReviewService reviewService;
    
    @FXML
    public void initialize() {
        bookService = new BookService();
        cartService = CartService.getInstance();
        reviewService = new ReviewService();
        
        currentUser = SessionManager.getInstance().getCurrentUser();
        currentBook = SessionManager.getInstance().getCurrentBook();
        
        if (currentUser == null) {
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        if (currentBook == null) {
            if (currentUser.isAdmin()) {
                ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
            } else {
                ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
            }
            return;
        }
        
        setupRoleBasedUI();
        
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
        
        displayBookDetails();
        
        loadReviews();
    }
    
    private void setupRoleBasedUI() {
        if (currentUser.isAdmin()) {
            adminActionBox.setVisible(true);
            customerActionBox.setVisible(false);
            addReviewBox.setVisible(false);
        } else {
            adminActionBox.setVisible(false);
            customerActionBox.setVisible(true);
            
            // Initialize the rating spinner for reviews
            SpinnerValueFactory.IntegerSpinnerValueFactory ratingValueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 5);
            ratingSpinner.setValueFactory(ratingValueFactory);
            
            try {
                boolean hasReviewed = reviewService.hasUserReviewedBook(currentUser.getId(), currentBook.getId());
                addReviewBox.setVisible(!hasReviewed);
            } catch (SQLException e) {
                addReviewBox.setVisible(true);
                e.printStackTrace();
            }
            
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, currentBook.getStockQuantity(), 1);
            quantitySpinner.setValueFactory(valueFactory);
            
            addToCartButton.setDisable(!currentBook.isInStock());
        }
    }
    
    private void displayBookDetails() {
        titleLabel.setText(currentBook.getTitle());
        authorLabel.setText(currentBook.getAuthor());
        isbnLabel.setText(currentBook.getIsbn());
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        priceLabel.setText(currencyFormat.format(currentBook.getPrice()));
        
        categoryLabel.setText(currentBook.getCategory());
        
        if (currentBook.isInStock()) {
            stockLabel.setText(currentBook.getStockQuantity() + " in stock");
            stockLabel.setStyle("-fx-text-fill: green;");
        } else {
            stockLabel.setText("Out of stock");
            stockLabel.setStyle("-fx-text-fill: red;");
        }
        
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
            ratingLabel.setText("No ratings yet");
            e.printStackTrace();
        }
        
        descriptionTextArea.setText(currentBook.getDescription());
        
        // Handle book image
        try {
            if (currentBook.getImageUrl() != null && !currentBook.getImageUrl().isEmpty()) {
                // Load image from URL
                Image bookImage = new Image(currentBook.getImageUrl(), 200, 200, true, true, true);
                bookImageView.setImage(bookImage);
                
                // Add error handling in case the image fails to load
                bookImage.errorProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        // Load default image if the URL image fails
                        Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-book.jpg"));
                        bookImageView.setImage(defaultImage);
                        imageErrorLabel.setVisible(true);
                    }
                });
            } else {
                // Load default image if no URL is provided
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-book.jpg"));
                bookImageView.setImage(defaultImage);
                imageErrorLabel.setVisible(true);
            }
        } catch (Exception e) {
            // Fallback to default image if any error occurs
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-book.jpg"));
            bookImageView.setImage(defaultImage);
            imageErrorLabel.setVisible(true);
            e.printStackTrace();
        }
    }
    
    private void loadReviews() {
        try {
            List<Review> reviews = reviewService.getBookReviews(currentBook.getId());
            System.out.println("Loaded " + reviews.size() + " reviews for book ID " + currentBook.getId());
            
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
            if (currentUser.getId() == 2 && "customer".equals(currentUser.getUsername())) {
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO users (id, username, password_hash, full_name, email, address, phone_number, role, order_count) " +
                         "VALUES (2, 'customer', '$2a$12$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Regular Customer', " +
                         "'customer@example.com', '456 Reader Lane', '555-987-6543', 'CUSTOMER', 0) " +
                         "ON CONFLICT (username) DO NOTHING")) {
                    stmt.executeUpdate();
                } catch (Exception e) {
                    System.out.println("Note: Customer user already exists or couldn't be created: " + e.getMessage());
                }
            }
            
            boolean success = cartService.addToCart(currentUser.getId(), currentBook.getId(), quantity);
            
            if (success) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Added to Cart");
                alert.setHeaderText(null);
                alert.setContentText(quantity + " copies of \"" + currentBook.getTitle() + "\" added to your cart.");
                
                ButtonType viewCartButton = new ButtonType("View Cart");
                ButtonType continueShoppingButton = new ButtonType("Continue Shopping");
                alert.getButtonTypes().setAll(viewCartButton, continueShoppingButton);
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == viewCartButton) {
                        ViewNavigator.getInstance().navigateTo("shopping_cart.fxml");
                    } else {
                        ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
                    }
                });
            } else {
                statusLabel.setText("Failed to add to cart");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Error adding to cart");
            alert.setContentText("Error: " + e.getMessage());
            
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
            
            ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
        }
    }
    
    @FXML
    public void handleAddReview(ActionEvent event) {
        String content = reviewTextField.getText().trim();
        
        // Handle potential null value from spinner
        Integer ratingValue = ratingSpinner.getValue();
        if (ratingValue == null) {
            statusLabel.setText("Please select a rating between 1-5");
            return;
        }
        int rating = ratingValue;
        
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
            review.setUsername(currentUser.getUsername());
            
            statusLabel.setText("Adding review...");
            statusLabel.setStyle("-fx-text-fill: black;");
            
            boolean success = reviewService.addReview(review);
            
            if (success) {
                reviewTextField.clear();
                ratingSpinner.getValueFactory().setValue(5);
                
                addReviewBox.setVisible(false);
                
                statusLabel.setText("Review added successfully");
                statusLabel.setStyle("-fx-text-fill: green;");
                
                loadReviews();
                
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
    
    @FXML
    public void handleEditBook(ActionEvent event) {
        if (!currentUser.isAdmin()) {
            return;
        }
        
        ViewNavigator.getInstance().navigateTo("edit_book.fxml");
    }
    
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
    
    @FXML
    public void handleBack(ActionEvent event) {
        if (currentUser.isAdmin()) {
            ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
        } else {
            ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
        }
    }
}