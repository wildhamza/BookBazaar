package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.Review;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.CartService;
import com.bookshop.services.ReviewService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the book details view.
 */
public class BookDetailsController implements Initializable {
    
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label publisherLabel;
    @FXML private Label priceLabel;
    @FXML private Label categoryLabel;
    @FXML private Label isbnLabel;
    @FXML private Label stockLabel;
    @FXML private Label ratingLabel;
    @FXML private ImageView bookImageView;
    @FXML private Text descriptionText;
    @FXML private Button addToCartButton;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button backButton;
    
    // Reviews section
    @FXML private TableView<Review> reviewsTableView;
    @FXML private TableColumn<Review, String> reviewUserColumn;
    @FXML private TableColumn<Review, String> reviewRatingColumn;
    @FXML private TableColumn<Review, String> reviewDateColumn;
    @FXML private TableColumn<Review, String> reviewCommentColumn;
    @FXML private TextArea reviewCommentArea;
    @FXML private Slider ratingSlider;
    @FXML private Button submitReviewButton;
    
    private Book book;
    private BookService bookService;
    private CartService cartService;
    private ReviewService reviewService;
    private ObservableList<Review> reviewsList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get the selected book from session
        book = SessionManager.getInstance().getSelectedBook();
        if (book == null) {
            ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
            return;
        }
        
        // Initialize services
        bookService = new BookService();
        cartService = new CartService();
        reviewService = new ReviewService();
        
        // Fetch the full book details including reviews
        try {
            book = bookService.getBookById(book.getId());
        } catch (Exception e) {
            showAlert("Error loading book details: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Setup UI
        initializeUI();
        
        // Setup quantity spinner
        initializeQuantitySpinner();
        
        // Setup reviews table
        initializeReviewsTable();
        
        // Load reviews
        loadReviews();
    }
    
    private void initializeUI() {
        titleLabel.setText(book.getTitle());
        authorLabel.setText(book.getAuthor());
        publisherLabel.setText(book.getPublisher());
        priceLabel.setText("$" + book.getPrice());
        categoryLabel.setText(book.getCategory());
        isbnLabel.setText(book.getIsbn());
        stockLabel.setText(book.getStockQuantity() + " in stock");
        ratingLabel.setText(String.format("%.1f ★ (%d reviews)", book.getAverageRating(), book.getReviewCount()));
        descriptionText.setText(book.getDescription());
        
        // Load book image if available
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(book.getImageUrl());
                bookImageView.setImage(image);
            } catch (Exception e) {
                // Use placeholder image on error
                bookImageView.setImage(new Image(getClass().getResourceAsStream("/images/book_placeholder.png")));
            }
        } else {
            // Use placeholder image
            bookImageView.setImage(new Image(getClass().getResourceAsStream("/images/book_placeholder.png")));
        }
        
        // Disable add to cart button if out of stock
        if (book.getStockQuantity() <= 0) {
            addToCartButton.setDisable(true);
            stockLabel.setText("Out of stock");
            stockLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void initializeQuantitySpinner() {
        // Set spinner value factory with min=1, max=stock, initial=1
        int max = Math.max(1, book.getStockQuantity());
        SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1);
        quantitySpinner.setValueFactory(valueFactory);
    }
    
    private void initializeReviewsTable() {
        reviewUserColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        reviewRatingColumn.setCellValueFactory(new PropertyValueFactory<>("starsDisplay"));
        reviewDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        reviewCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        
        // Set wrapping for comment column
        reviewCommentColumn.setCellFactory(tc -> {
            TableCell<Review, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(reviewCommentColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }
    
    private void loadReviews() {
        try {
            List<Review> reviews = reviewService.getReviewsByBookId(book.getId());
            reviewsList = FXCollections.observableArrayList(reviews);
            reviewsTableView.setItems(reviewsList);
        } catch (Exception e) {
            showAlert("Error loading reviews: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddToCart(ActionEvent event) {
        int quantity = quantitySpinner.getValue();
        if (quantity <= 0 || quantity > book.getStockQuantity()) {
            showAlert("Invalid quantity");
            return;
        }
        
        try {
            cartService.addToCart(book, quantity);
            showAlert("Added to cart: " + book.getTitle() + " x " + quantity);
        } catch (Exception e) {
            showAlert("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSubmitReview(ActionEvent event) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("You must be logged in to submit a review");
            return;
        }
        
        String comment = reviewCommentArea.getText().trim();
        int rating = (int) ratingSlider.getValue();
        
        if (comment.isEmpty()) {
            showAlert("Please enter a comment");
            return;
        }
        
        try {
            Review review = new Review();
            review.setBookId(book.getId());
            review.setUserId(currentUser.getId());
            review.setUsername(currentUser.getUsername());
            review.setRating(rating);
            review.setComment(comment);
            
            reviewService.addReview(review);
            
            // Refresh the reviews list
            loadReviews();
            
            // Refresh book details to update average rating
            book = bookService.getBookById(book.getId());
            ratingLabel.setText(String.format("%.1f ★ (%d reviews)", book.getAverageRating(), book.getReviewCount()));
            
            // Clear the form
            reviewCommentArea.clear();
            ratingSlider.setValue(5);
            
            showAlert("Thank you for your review!");
        } catch (Exception e) {
            showAlert("Error submitting review: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
