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
import javafx.beans.property.SimpleStringProperty;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    
    @FXML
    private TableView<Book> booksTableView;
    
    @FXML
    private TableColumn<Book, String> titleColumn;
    
    @FXML
    private TableColumn<Book, String> authorColumn;
    
    @FXML
    private TableColumn<Book, String> publisherColumn;
    
    @FXML
    private TableColumn<Book, String> priceColumn;
    
    @FXML
    private TableColumn<Book, String> categoryColumn;
    
    @FXML
    private TableColumn<Book, String> ratingColumn;
    
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
    
    @FXML
    public void initialize() {
        bookService = new BookService();
        cartService = CartService.getInstance();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        cartService.addCartUpdateListener(this);
        
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        
        loyaltyStatusLabel.setText("Status: " + currentUser.getLoyaltyStatus());
        
        updateCartItemCount();
        
        if (bookListView != null) {
            bookListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Book>() {
                @Override
                protected void updateItem(Book book, boolean empty) {
                    super.updateItem(book, empty);
                    if (empty || book == null) {
                        setText(null);
                    } else {
                        String ratingText = book.getAverageRating() > 0 ? 
                            String.format(" (%.1f★)", book.getAverageRating()) : "";
                        setText(book.getTitle() + " by " + book.getAuthor() + " - €" + book.getPrice() + ratingText);
                    }
                }
            });
        }
        
        if (orderListView != null) {
            orderListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Order>() {
                @Override
                protected void updateItem(Order order, boolean empty) {
                    super.updateItem(order, empty);
                    if (empty || order == null) {
                        setText(null);
                    } else {
                        setText("Order #" + order.getId() + " - €" + order.getTotalAmount() + " - " + order.getStatus());
                    }
                }
            });
        }
        
        if (booksTableView != null) {
            setupTableColumns();
        }
        
        if (categoryComboBox != null && sortByComboBox != null) {
            initializeComboBoxes();
        }
        
        loadBooks();
        
        loadOrders();
        
        if (bookListView != null) {
            bookListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
                    if (selectedBook != null) {
                        SessionManager.getInstance().setCurrentBook(selectedBook);
                        ViewNavigator.getInstance().navigateTo("book_details.fxml");
                    }
                }
            });
        }
        
        if (orderListView != null) {
            orderListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Order selectedOrder = orderListView.getSelectionModel().getSelectedItem();
                    if (selectedOrder != null) {
                        SessionManager.getInstance().setCurrentOrder(selectedOrder);
                        ViewNavigator.getInstance().navigateTo("customer_orders.fxml");
                    }
                }
            });
        }
        
        // Force layout and refresh when returning from other screens
        if (booksTableView != null) {
            booksTableView.refresh();
            booksTableView.requestLayout();
        }
        
        // Ensure application of filters and sort criteria
        if (categoryComboBox != null && sortByComboBox != null) {
            applyFiltersAndSort();
        }
    }
    
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        priceColumn.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getPrice().doubleValue();
            return new SimpleStringProperty(String.format("€%.2f", price));
        });
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        ratingColumn.setCellValueFactory(cellData -> {
            double rating = cellData.getValue().getAverageRating();
            return new SimpleStringProperty(rating > 0 ? String.format("%.1f★", rating) : "No ratings");
        });
    }
    
    private void initializeComboBoxes() {
        categoryComboBox.getItems().add("All Categories");
        
        try {
            List<String> categories = bookService.getAllBooks().stream()
                .map(Book::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            categoryComboBox.getItems().addAll(categories);
        } catch (SQLException e) {
            statusLabel.setText("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        categoryComboBox.setValue("All Categories");
        
        categoryComboBox.setOnAction(e -> {
            currentCategory = categoryComboBox.getValue();
            applyFiltersAndSort();
        });
    
        sortByComboBox.getItems().addAll(
            "Title (A-Z)", 
            "Title (Z-A)",
            "Author (A-Z)", 
            "Author (Z-A)",
            "Publisher (A-Z)",
            "Publisher (Z-A)",
            "Category (A-Z)",
            "Category (Z-A)",
            "Price (Low to High)", 
            "Price (High to Low)",
            "Rating (Low to High)",
            "Rating (High to Low)"
        );
        
        sortByComboBox.setValue("Title (A-Z)");
        
        sortByComboBox.setOnAction(e -> {
            currentSortBy = sortByComboBox.getValue();
            applyFiltersAndSort();
        });
    }
    
    private void loadBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            
            allBooks.clear();
            allBooks.addAll(books);
            
            if (booksTableView != null) {
                booksTableView.getItems().clear();
                booksTableView.getItems().addAll(books);
            }
            
            if (bookListView != null) {
                bookListView.getItems().clear();
                bookListView.getItems().addAll(books);
            }
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleSearchButton(ActionEvent event) {
        applyFiltersAndSort();
    }
    
    private void applyFiltersAndSort() {
        String searchQuery = searchField.getText().trim().toLowerCase();
        
        List<Book> filteredBooks = allBooks.stream()
            .filter(book -> {
                boolean categoryMatch = currentCategory.equals("All Categories") || 
                                       book.getCategory().equals(currentCategory);
                
                boolean searchMatch = searchQuery.isEmpty() || 
                                    book.getTitle().toLowerCase().contains(searchQuery) || 
                                    book.getAuthor().toLowerCase().contains(searchQuery) ||
                                    book.getPublisher().toLowerCase().contains(searchQuery) ||
                                    book.getCategory().toLowerCase().contains(searchQuery);
                
                return categoryMatch && searchMatch;
            })
            .collect(Collectors.toList());
        
        applySort(filteredBooks);
        
        if (booksTableView != null) {
            booksTableView.getItems().clear();
            booksTableView.getItems().addAll(filteredBooks);
        }
        
        if (bookListView != null) {
            bookListView.getItems().clear();
            bookListView.getItems().addAll(filteredBooks);
        }
        
        statusLabel.setText("Found " + filteredBooks.size() + " books");
    }
    
    private void applySort(List<Book> books) {
        switch (currentSortBy) {
            case "Title (A-Z)":
                books.sort(Comparator.comparing(Book::getTitle));
                break;
            case "Title (Z-A)":
                books.sort(Comparator.comparing(Book::getTitle).reversed());
                break;
            case "Author (A-Z)":
                books.sort(Comparator.comparing(Book::getAuthor));
                break;
            case "Author (Z-A)":
                books.sort(Comparator.comparing(Book::getAuthor).reversed());
                break;
            case "Publisher (A-Z)":
                books.sort(Comparator.comparing(Book::getPublisher));
                break;
            case "Publisher (Z-A)":
                books.sort(Comparator.comparing(Book::getPublisher).reversed());
                break;
            case "Category (A-Z)":
                books.sort(Comparator.comparing(Book::getCategory));
                break;
            case "Category (Z-A)":
                books.sort(Comparator.comparing(Book::getCategory).reversed());
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
    
    private void loadOrders() {
        try {
            System.out.println("CustomerDashboardController: Loading orders for user ID " + currentUser.getId());
            
            PurchaseService purchaseService = new PurchaseService();
            List<Order> orders = purchaseService.getOrdersByUserId(currentUser.getId());
            
            System.out.println("CustomerDashboardController: Found " + orders.size() + " orders");
            
            orderListView.getItems().clear();
            orderListView.getItems().addAll(orders);
            
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
    
    private void updateCartItemCount() {
        try {
            int count = cartService.getCartItemCount(currentUser.getId());
            cartItemCountLabel.setText(count + " item" + (count != 1 ? "s" : ""));
            
            viewCartButton.setDisable(count == 0);
        } catch (SQLException e) {
            statusLabel.setText("Error getting cart count: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleViewCartButton(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("shopping_cart.fxml");
    }
    
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
    
    @FXML
    public void handleRefreshOrders(ActionEvent event) {
        loadOrders();
    }
    
    @FXML
    public void handleBookTableClick(javafx.scene.input.MouseEvent event) {
        if (booksTableView != null && event.getClickCount() == 2) {
            Book selectedBook = booksTableView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                SessionManager.getInstance().setCurrentBook(selectedBook);
                ViewNavigator.getInstance().navigateTo("book_details.fxml");
                loadBooks();
                applyFiltersAndSort();
            }
        }
    }
    
    @FXML
    public void handleBookListViewClick(javafx.scene.input.MouseEvent event) {
        if (bookListView != null && event.getClickCount() == 2) {
            Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                SessionManager.getInstance().setCurrentBook(selectedBook);
                ViewNavigator.getInstance().navigateTo("book_details.fxml");
                loadBooks();
                applyFiltersAndSort();
            }
        }
    }
    
    @FXML
    public void handleAddToCartButton(ActionEvent event) {
        Book selectedBook = null;
        
        if (booksTableView != null && booksTableView.getSelectionModel().getSelectedItem() != null) {
            selectedBook = booksTableView.getSelectionModel().getSelectedItem();
        } 
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
    
    @FXML
    public void handleLogoutButton(ActionEvent event) {
        cartService.removeCartUpdateListener(this);
        
        SessionManager.getInstance().logout();
        ViewNavigator.getInstance().navigateTo("login.fxml");
    }
    
    @FXML
    public void handleLogout(ActionEvent event) {
        handleLogoutButton(event);
    }
    
    @Override
    public void onCartUpdated(int userId) {
        if (userId == currentUser.getId()) {
            updateCartItemCount();
        }
    }
}