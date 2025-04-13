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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    
    // New FXML fields for table view
    @FXML
    private TableView<Book> booksTableView;
    
    @FXML
    private TableColumn<Book, String> titleColumn;
    
    @FXML
    private TableColumn<Book, String> authorColumn;
    
    @FXML
    private TableColumn<Book, String> publisherColumn;
    
    @FXML
    private TableColumn<Book, Double> priceColumn;
    
    @FXML
    private TableColumn<Book, String> categoryColumn;
    
    @FXML
    private TableColumn<Book, Double> ratingColumn;
    
    // Search and filter fields
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private ComboBox<String> sortByComboBox;
    
    private BookService bookService;
    private CartService cartService;
    private User currentUser;
    private ObservableList<Book> allBooks = FXCollections.observableArrayList();
    private String currentCategory = "All Categories";
    private String currentSortBy = "Title";
    
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
        
        // Configure bookListView with a custom cell factory if it exists
        if (bookListView != null) {
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
        }
        
        // Configure orderListView with a custom cell factory if it exists
        if (orderListView != null) {
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
        }
        
        // Set up the table columns if they exist
        if (booksTableView != null) {
            setupTableColumns();
        }
        
        // Initialize combo boxes if they exist
        if (categoryComboBox != null && sortByComboBox != null) {
            initializeComboBoxes();
        }
        
        // Load books
        loadBooks();
        
        // Load orders
        loadOrders();
        
        // Set up double-click handler for book list if it exists
        if (bookListView != null) {
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
        }
        
        // Set up double-click handler for orders list if it exists
        if (orderListView != null) {
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
    }
    
    /**
     * Sets up the table columns by binding them to the Book properties.
     */
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
    }
    
    /**
     * Initializes the combo boxes with options.
     */
    private void initializeComboBoxes() {
        // Set up categoryComboBox
        categoryComboBox.getItems().add("All Categories");
        
        try {
            // Get all available categories from the books
            List<String> categories = bookService.getAllBooks().stream()
                .map(Book::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            // Add each category to the combo box
            categoryComboBox.getItems().addAll(categories);
        } catch (SQLException e) {
            statusLabel.setText("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        categoryComboBox.setValue("All Categories");
        
        // Add listeners for category selection
        categoryComboBox.setOnAction(e -> {
            currentCategory = categoryComboBox.getValue();
            applyFiltersAndSort();
        });
        
        // Set up sortByComboBox
        sortByComboBox.getItems().addAll(
            "Title", 
            "Author", 
            "Price (Low to High)", 
            "Price (High to Low)",
            "Rating (Low to High)",
            "Rating (High to Low)"
        );
        
        sortByComboBox.setValue("Title");
        
        // Add listeners for sort selection
        sortByComboBox.setOnAction(e -> {
            currentSortBy = sortByComboBox.getValue();
            applyFiltersAndSort();
        });
    }
    
    /**
     * Loads books from the database.
     */
    private void loadBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            
            // Store all books for filtering
            allBooks.clear();
            allBooks.addAll(books);
            
            // Update the table view if it exists
            if (booksTableView != null) {
                booksTableView.getItems().clear();
                booksTableView.getItems().addAll(books);
            }
            
            // For backward compatibility with the older list view if it exists
            if (bookListView != null) {
                bookListView.getItems().clear();
                bookListView.getItems().addAll(books);
            }
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the search button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleSearchButton(ActionEvent event) {
        applyFiltersAndSort();
    }
    
    /**
     * Applies the current filters (search query and category) and sorting to the books.
     */
    private void applyFiltersAndSort() {
        // Get search query
        String searchQuery = searchField.getText().trim().toLowerCase();
        
        // Apply filters
        List<Book> filteredBooks = allBooks.stream()
            .filter(book -> {
                // Apply category filter if not "All Categories"
                boolean categoryMatch = currentCategory.equals("All Categories") || 
                                       book.getCategory().equals(currentCategory);
                
                // Apply search filter if query is not empty
                boolean searchMatch = searchQuery.isEmpty() || 
                                    book.getTitle().toLowerCase().contains(searchQuery) || 
                                    book.getAuthor().toLowerCase().contains(searchQuery) ||
                                    book.getDescription().toLowerCase().contains(searchQuery);
                
                return categoryMatch && searchMatch;
            })
            .collect(Collectors.toList());
        
        // Apply sorting
        applySort(filteredBooks);
        
        // Update table view with filtered and sorted books if it exists
        if (booksTableView != null) {
            booksTableView.getItems().clear();
            booksTableView.getItems().addAll(filteredBooks);
        }
        
        // For backward compatibility with the older list view if it exists
        if (bookListView != null) {
            bookListView.getItems().clear();
            bookListView.getItems().addAll(filteredBooks);
        }
        
        // Update status label
        statusLabel.setText("Found " + filteredBooks.size() + " books");
    }
    
    /**
     * Applies sorting to a list of books based on the current sort criteria.
     * 
     * @param books The list of books to sort
     */
    private void applySort(List<Book> books) {
        switch (currentSortBy) {
            case "Title":
                books.sort(Comparator.comparing(Book::getTitle));
                break;
            case "Author":
                books.sort(Comparator.comparing(Book::getAuthor));
                break;
            case "Price (Low to High)":
                books.sort(Comparator.comparing(Book::getPrice));
                break;
            case "Price (High to Low)":
                books.sort(Comparator.comparing(Book::getPrice).reversed());
                break;
            case "Rating (Low to High)":
                books.sort(Comparator.comparing(Book::getAverageRating));
                break;
            case "Rating (High to Low)":
                books.sort(Comparator.comparing(Book::getAverageRating).reversed());
                break;
            default:
                books.sort(Comparator.comparing(Book::getTitle));
                break;
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
     * Handles the refresh orders button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleRefreshOrders(ActionEvent event) {
        loadOrders();
    }
    
    /**
     * Handles the book table click action.
     * 
     * @param event The mouse event
     */
    @FXML
    public void handleBookTableClick(javafx.scene.input.MouseEvent event) {
        if (booksTableView != null && event.getClickCount() == 2) {
            Book selectedBook = booksTableView.getSelectionModel().getSelectedItem();
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
        Book selectedBook = null;
        
        // First check if a book is selected in the table view (preferred)
        if (booksTableView != null && booksTableView.getSelectionModel().getSelectedItem() != null) {
            selectedBook = booksTableView.getSelectionModel().getSelectedItem();
        } 
        // If no book is selected in the table view, check the list view (fallback)
        else if (bookListView != null && bookListView.getSelectionModel().getSelectedItem() != null) {
            selectedBook = bookListView.getSelectionModel().getSelectedItem();
        }
        
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
        if (userId == currentUser.getId()) {
            updateCartItemCount();
        }
    }
}