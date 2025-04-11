package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.CartService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the customer dashboard view.
 */
public class CustomerDashboardController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private TableView<Book> booksTableView;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> publisherColumn;
    @FXML private TableColumn<Book, BigDecimal> priceColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, Double> ratingColumn;
    @FXML private Button viewCartButton;
    @FXML private Button viewOrdersButton;
    @FXML private Button logoutButton;
    @FXML private Label cartItemCountLabel;
    
    private BookService bookService;
    private CartService cartService;
    private ObservableList<Book> bookList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Check if user is logged in
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        
        // Initialize services
        bookService = new BookService();
        cartService = new CartService();
        
        // Initialize ComboBoxes
        initializeComboBoxes();
        
        // Setup TableView columns
        initializeTableColumns();
        
        // Load all books initially
        loadAllBooks();
        
        // Update cart item count
        updateCartItemCount();
        
        // Register as observer to cart
        cartService.addObserver(() -> updateCartItemCount());
    }
    
    private void initializeComboBoxes() {
        // Categories
        ObservableList<String> categories = FXCollections.observableArrayList(
            "All Categories", "Fiction", "Non-Fiction", "Science", "Technology", 
            "History", "Biography", "Fantasy", "Mystery", "Thriller", "Romance"
        );
        categoryComboBox.setItems(categories);
        categoryComboBox.setValue("All Categories");
        
        // Sort options
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
            "Title (A-Z)", "Title (Z-A)", "Price (Low-High)", "Price (High-Low)", 
            "Author (A-Z)", "Author (Z-A)", "Rating (High-Low)"
        );
        sortByComboBox.setItems(sortOptions);
        sortByComboBox.setValue("Title (A-Z)");
        
        // Add change listeners
        categoryComboBox.setOnAction(e -> applyFilters());
        sortByComboBox.setOnAction(e -> applyFilters());
    }
    
    private void initializeTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        
        // Custom formatter for rating
        ratingColumn.setCellFactory(col -> new TableCell<Book, Double>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f ★", rating));
                }
            }
        });
    }
    
    private void loadAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            bookList = FXCollections.observableArrayList(books);
            booksTableView.setItems(bookList);
        } catch (Exception e) {
            showAlert("Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateCartItemCount() {
        int count = cartService.getCartItemCount();
        cartItemCountLabel.setText(count + " items");
    }
    
    @FXML
    private void handleSearchButton(ActionEvent event) {
        applyFilters();
    }
    
    private void applyFilters() {
        try {
            String searchTerm = searchField.getText().trim();
            String category = categoryComboBox.getValue();
            String sortBy = sortByComboBox.getValue();
            
            // If All Categories is selected, pass empty string to search all
            if ("All Categories".equals(category)) {
                category = "";
            }
            
            // Determine sort parameters
            String sortField = "title";
            boolean ascending = true;
            
            switch (sortBy) {
                case "Title (A-Z)":
                    sortField = "title";
                    ascending = true;
                    break;
                case "Title (Z-A)":
                    sortField = "title";
                    ascending = false;
                    break;
                case "Price (Low-High)":
                    sortField = "price";
                    ascending = true;
                    break;
                case "Price (High-Low)":
                    sortField = "price";
                    ascending = false;
                    break;
                case "Author (A-Z)":
                    sortField = "author";
                    ascending = true;
                    break;
                case "Author (Z-A)":
                    sortField = "author";
                    ascending = false;
                    break;
                case "Rating (High-Low)":
                    sortField = "averageRating";
                    ascending = false;
                    break;
            }
            
            List<Book> filteredBooks = bookService.searchBooks(searchTerm, category, sortField, ascending);
            bookList = FXCollections.observableArrayList(filteredBooks);
            booksTableView.setItems(bookList);
            
        } catch (Exception e) {
            showAlert("Error applying filters: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBookTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) { // Double click
            Book selectedBook = booksTableView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                // Store selected book in session and navigate to details
                SessionManager.getInstance().setSelectedBook(selectedBook);
                ViewNavigator.getInstance().navigateTo("book_details.fxml");
            }
        }
    }
    
    @FXML
    private void handleAddToCartButton(ActionEvent event) {
        Book selectedBook = booksTableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Please select a book to add to cart");
            return;
        }
        
        if (selectedBook.getStockQuantity() <= 0) {
            showAlert("Sorry, this book is out of stock");
            return;
        }
        
        try {
            cartService.addToCart(selectedBook, 1);
            showAlert("Book added to cart");
        } catch (Exception e) {
            showAlert("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewCartButton(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("shopping_cart.fxml");
    }
    
    @FXML
    private void handleViewOrdersButton(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("customer_orders.fxml");
    }
    
    @FXML
    private void handleLogoutButton(ActionEvent event) {
        // Clear session and return to login
        SessionManager.getInstance().clearSession();
        ViewNavigator.getInstance().navigateTo("login.fxml");
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
