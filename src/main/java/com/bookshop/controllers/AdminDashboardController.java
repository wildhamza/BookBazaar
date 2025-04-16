package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
import com.bookshop.services.OrderService;
import com.bookshop.utils.DatabaseInitializer;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.math.BigDecimal;

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
    
    @FXML
    private TableView<Book> booksTableView;
    
    @FXML
    private TableColumn<Book, String> titleColumn;
    
    @FXML
    private TableColumn<Book, String> authorColumn;
    
    @FXML
    private TableColumn<Book, String> publisherColumn;
    
    @FXML
    private TableColumn<Book, BigDecimal> priceColumn;
    
    @FXML
    private TableColumn<Book, String> categoryColumn;
    
    @FXML
    private TableColumn<Book, Integer> stockColumn;
    
    @FXML
    private TableColumn<Book, Double> ratingColumn;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private ComboBox<String> sortByComboBox;
    
    @FXML
    private TextField orderSearchField;
    
    @FXML
    private TableView<Order> ordersTableView;
    
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    
    @FXML
    private TableColumn<Order, String> customerNameColumn;
    
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    
    @FXML
    private TableColumn<Order, BigDecimal> totalAmountColumn;
    
    @FXML
    private TableColumn<Order, Order.Status> statusColumn;
    
    @FXML
    private TableColumn<Order, Void> orderActionColumn;
    
    private BookService bookService;
    private User currentUser;
    private ObservableList<Book> allBooks = FXCollections.observableArrayList();
    private String currentCategory = "All Categories";
    private String currentSortBy = "Title (A-Z)";
    
    @SuppressWarnings("unused")
    @FXML
    public void initialize() {
        bookService = new BookService();
        UserService userService = new UserService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null || !currentUser.isAdmin()) {
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        
        try {
            DatabaseInitializer.createTestOrders();
        } catch (SQLException e) {
            System.err.println("Error creating test orders: " + e.getMessage());
            e.printStackTrace();
        }

        configureListViews();
        
        if (booksTableView != null) {
            setupTableColumns();
        }
        
        if (categoryComboBox != null && sortByComboBox != null) {
            initializeComboBoxes();
        }
        
        if (ordersTableView != null) {
            setupOrdersTableColumns();
        }
        
        loadBooks();
        
        loadOrders();
        
        loadCompletedOrders();
        
        // Force layout and refresh when returning from other screens
        if (mainTabPane != null) {
            mainTabPane.requestLayout();
        }
        
        if (booksTableView != null) {
            booksTableView.refresh();
        }
        
        if (ordersTableView != null) {
            ordersTableView.refresh();
        }
    }
    
    private void configureListViews() {
        if (bookListView != null) {
            bookListView.setCellFactory(lv -> new ListCell<Book>() {
                @Override
                protected void updateItem(Book book, boolean empty) {
                    super.updateItem(book, empty);
                    if (empty || book == null) {
                        setText(null);
                    } else {
                        setText(book.getTitle() + " by " + book.getAuthor() + " - €" + book.getPrice());
                    }
                }
            });
            
            bookListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
                    handleBookSelection(selectedBook);
                }
            });
        }
        
        if (orderListView != null) {
            orderListView.setCellFactory(lv -> new ListCell<Order>() {
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
            
            orderListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Order selectedOrder = orderListView.getSelectionModel().getSelectedItem();
                    if (selectedOrder != null) {
                        showOrderDetailsDialog(selectedOrder);
                    }
                }
            });
        }
        
        if (completedOrdersListView != null) {
            completedOrdersListView.setCellFactory(lv -> new ListCell<Order>() {
                @Override
                protected void updateItem(Order order, boolean empty) {
                    super.updateItem(order, empty);
                    if (empty || order == null) {
                        setText(null);
                    } else {
                        setText("Order #" + order.getId() + " - €" + order.getTotalAmount() + " - Completed on: " + order.getOrderDate());
                    }
                }
            });
            
            completedOrdersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    displayCompletedOrderDetails(newSelection);
                } else {
                    completedOrderDetailsTextArea.setText("");
                }
            });
        }
        
        if (booksTableView != null) {
            booksTableView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Book selectedBook = booksTableView.getSelectionModel().getSelectedItem();
                    handleBookSelection(selectedBook);
                }
            });
        }
    }
    
    private void handleBookSelection(Book selectedBook) {
        if (selectedBook != null) {
            try {
                SessionManager.getInstance().setCurrentBook(selectedBook);
                ViewNavigator navigator = ViewNavigator.getInstance();
                if (navigator.getStage() != null) {
                    navigator.navigateTo("book_details.fxml");
                } else {
                    System.err.println("Warning: ViewNavigator stage is not set. Cannot navigate to book details.");
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
    
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        
        // Setup action column if it exists in the FXML
        TableColumn<Book, Void> actionColumn = null;
        for (TableColumn<Book, ?> column : booksTableView.getColumns()) {
            if ("actionColumn".equals(column.getId())) {
                @SuppressWarnings("unchecked")
                TableColumn<Book, Void> typedColumn = (TableColumn<Book, Void>) column;
                actionColumn = typedColumn;
                break;
            }
        }
            
        if (actionColumn != null) {
            actionColumn.setCellFactory(col -> {
                return new TableCell<Book, Void>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox actionBox = new HBox(5, editButton, deleteButton);
                    
                    {
                        editButton.getStyleClass().add("small-button");
                        deleteButton.getStyleClass().add("small-button");
                        deleteButton.getStyleClass().add("warning-button");
                        
                        editButton.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            SessionManager.getInstance().setCurrentBook(book);
                            ViewNavigator.getInstance().navigateTo("edit_book.fxml");
                        });
                        
                        deleteButton.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            handleBookDeletion(book);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionBox);
                        }
                    }
                };
            });
        }
    }
    
    private void handleBookDeletion(Book book) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Book");
        alert.setContentText("Are you sure you want to delete the book: " + book.getTitle() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bookService.deleteBook(book.getId());
                    loadBooks();
                    statusLabel.setText("Book deleted successfully.");
                } catch (SQLException e) {
                    statusLabel.setText("Error deleting book: " + e.getMessage());
                    e.printStackTrace();
                }
            }
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
            "Stock (Low to High)",
            "Stock (High to Low)",
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
            System.out.println("AdminDashboardController: loadBooks() called");
            List<Book> books = bookService.getAllBooks();

            allBooks.clear();
            allBooks.addAll(books);

            updateBooksDisplay(books);

            statusLabel.setText("Books loaded successfully");
        } catch (SQLException e) {
            statusLabel.setText("Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateBooksDisplay(List<Book> books) {
        if (booksTableView != null) {
            booksTableView.getItems().clear();
            booksTableView.getItems().addAll(books);
        }

        if (bookListView != null) {
            bookListView.getItems().clear();
            bookListView.getItems().addAll(books);
        }
    }
    
    @FXML
    public void handleBookSearch(ActionEvent event) {
        applyFiltersAndSort();
    }
    
    private void applyFiltersAndSort() {
        String searchText = bookSearchField.getText().trim().toLowerCase();
        
        List<Book> filteredBooks = allBooks.stream()
            .filter(book -> {
                boolean categoryMatch = currentCategory.equals("All Categories") || 
                                       book.getCategory().equals(currentCategory);
                
                boolean searchMatch = searchText.isEmpty() || 
                                    book.getTitle().toLowerCase().contains(searchText) || 
                                    book.getAuthor().toLowerCase().contains(searchText) ||
                                    book.getPublisher().toLowerCase().contains(searchText) ||
                                    book.getCategory().toLowerCase().contains(searchText) ||
                                    (book.getDescription() != null && book.getDescription().toLowerCase().contains(searchText));
                
                return categoryMatch && searchMatch;
            })
            .collect(Collectors.toList());
        
        applySort(filteredBooks);
        
        updateBooksDisplay(filteredBooks);
        
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
            case "Stock (Low to High)":
                books.sort(Comparator.comparing(Book::getStockQuantity));
                break;
            case "Stock (High to Low)":
                books.sort(Comparator.comparing(Book::getStockQuantity).reversed());
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
            OrderService orderService = new OrderService();
            List<Order> orders = orderService.getAllOrders();
            
            // Filter out delivered orders
            List<Order> pendingOrders = orders.stream()
                .filter(order -> order.getStatus() != Order.Status.DELIVERED)
                .collect(Collectors.toList());
            
            if (orderListView != null) {
                orderListView.getItems().clear();
                orderListView.getItems().addAll(pendingOrders);
            }
            
            if (ordersTableView != null) {
                ordersTableView.getItems().clear();
                ordersTableView.getItems().addAll(orders);
            }
            
            System.out.println("Loaded " + orders.size() + " orders");
        } catch (SQLException e) {
            statusLabel.setText("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadCompletedOrders() {
        try {
            OrderService orderService = new OrderService();
            List<Order> allOrders = orderService.getAllOrders();
            
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
    
    private void displayCompletedOrderDetails(Order order) {
        try {
            StringBuilder details = new StringBuilder();
            details.append("Order #").append(order.getId()).append("\n");
            details.append("Date: ").append(order.getOrderDate()).append("\n");
            details.append("Customer ID: ").append(order.getUserId()).append("\n");
            details.append("Total Amount: €").append(order.getTotalAmount()).append("\n");
            details.append("Payment Method: ").append(order.getPaymentMethod()).append("\n\n");
            
            OrderService orderService = new OrderService();
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            
            details.append("Items:").append("\n");
            for (OrderItem item : items) {
                Book book = bookService.getBookById(item.getBookId());
                if (book != null) {
                    details.append(" - ").append(book.getTitle())
                           .append(" (").append(item.getQuantity()).append(" x €")
                           .append(item.getPrice()).append(") = €")
                           .append(item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                           .append("\n");
                } else {
                    details.append(" - Unknown Book (ID: ").append(item.getBookId())
                           .append(") - ").append(item.getQuantity()).append(" x €")
                           .append(item.getPrice()).append("\n");
                }
            }
            
            completedOrderDetailsTextArea.setText(details.toString());
        } catch (Exception e) {
            completedOrderDetailsTextArea.setText("Error loading order details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showOrderDetailsDialog(Order order) {
        try {
            // Create dialog
            Alert dialog = new Alert(AlertType.INFORMATION);
            dialog.setTitle("Order Details");
            dialog.setHeaderText("Order #" + order.getId() + " Details");
            
            StringBuilder details = new StringBuilder();
            details.append("Date: ").append(order.getOrderDate()).append("\n");
            details.append("Customer ID: ").append(order.getUserId()).append("\n");
            details.append("Status: ").append(order.getStatus()).append("\n");
            details.append("Total Amount: €").append(order.getTotalAmount()).append("\n");
            details.append("Payment Method: ").append(order.getPaymentMethod()).append("\n\n");
            
            OrderService orderService = new OrderService();
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            
            details.append("Items:").append("\n");
            for (OrderItem item : items) {
                Book book = bookService.getBookById(item.getBookId());
                if (book != null) {
                    details.append(" - ").append(book.getTitle())
                           .append(" (").append(item.getQuantity()).append(" x €")
                           .append(item.getPrice()).append(") = €")
                           .append(item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                           .append("\n");
                } else {
                    details.append(" - Unknown Book (ID: ").append(item.getBookId())
                           .append(") - ").append(item.getQuantity()).append(" x €")
                           .append(item.getPrice()).append("\n");
                }
            }
            
            dialog.setContentText(details.toString());
            
            if (order.getStatus() != Order.Status.DELIVERED) {
                dialog.getButtonTypes().clear();
                dialog.getButtonTypes().addAll(
                    ButtonType.OK, 
                    new ButtonType("Mark as Completed", ButtonBar.ButtonData.APPLY)
                );

                dialog.showAndWait().ifPresent(response -> {
                    if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                        try {
                            orderService.updateOrderStatus(order.getId(), "DELIVERED");
                            statusLabel.setText("Order #" + order.getId() + " marked as completed");
                            
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
    
    @FXML
    public void handleCustomerSearch(ActionEvent event) {
        String searchText = customerSearchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            return;
        }
        
        try {
            UserService userService = new UserService();
            List<User> allUsers = userService.getAllUsers();
            @SuppressWarnings("unused")
            List<User> filteredUsers = allUsers.stream()
                .filter(user -> 
                    user.getUsername().toLowerCase().contains(searchText) || 
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchText)))
                .collect(java.util.stream.Collectors.toList());
       
        } catch (SQLException e) {
            statusLabel.setText("Error searching customers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    public void handleAddNewBook(ActionEvent event) {
        SessionManager.getInstance().setCurrentBook(null);
        try {
            ViewNavigator.getInstance().navigateTo("edit_book.fxml");
        } catch (Exception e) {
            System.err.println("Error navigating to edit book view: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Cannot navigate to edit book view");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    public void handleEditBook(ActionEvent event) {
        Book selectedBook = null;
        
        if (booksTableView != null && booksTableView.getSelectionModel().getSelectedItem() != null) {
            selectedBook = booksTableView.getSelectionModel().getSelectedItem();
        } 
        else if (bookListView != null && bookListView.getSelectionModel().getSelectedItem() != null) {
            selectedBook = bookListView.getSelectionModel().getSelectedItem();
        }
        
        if (selectedBook == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Book Selected");
            alert.setContentText("Please select a book to edit.");
            alert.showAndWait();
            return;
        }
        
        SessionManager.getInstance().setCurrentBook(selectedBook);
        try {
            ViewNavigator.getInstance().navigateTo("edit_book.fxml");
        } catch (Exception e) {
            System.err.println("Error navigating to edit book view: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Cannot navigate to edit book view");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        ViewNavigator.getInstance().navigateTo("login.fxml");
    }
    
    @FXML
    public void handleDeleteBook(ActionEvent event) {
        Book selectedBook = null;
        
        if (booksTableView != null && booksTableView.getSelectionModel().getSelectedItem() != null) {
            selectedBook = booksTableView.getSelectionModel().getSelectedItem();
        } 
        else if (bookListView != null && bookListView.getSelectionModel().getSelectedItem() != null) {
            selectedBook = bookListView.getSelectionModel().getSelectedItem();
        }
        
        if (selectedBook == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Book Selected");
            alert.setContentText("Please select a book to delete.");
            alert.showAndWait();
            return;
        }
        
        handleBookDeletion(selectedBook);
    }
    
    @FXML
    public void handleOrderSearch(ActionEvent event) {
        String searchText = orderSearchField.getText().trim().toLowerCase();
        try {
            OrderService orderService = new OrderService();
            List<Order> allOrders = orderService.getAllOrders();
            
            List<Order> filteredOrders = allOrders.stream()
                .filter(order -> {
                    // Check for matching order ID
                    boolean idMatch = String.valueOf(order.getId()).contains(searchText);
                    
                    // Try to get customer name if we have searchText
                    boolean customerMatch = false;
                    if (!searchText.isEmpty()) {
                        try {
                            UserService userService = new UserService();
                            User user = userService.getUserById(order.getUserId());
                            if (user != null && user.getFullName() != null) {
                                customerMatch = user.getFullName().toLowerCase().contains(searchText);
                            }
                        } catch (SQLException e) {
                            // Ignore errors in getting user info for matching
                            e.printStackTrace();
                        }
                    }
                    
                    return idMatch || customerMatch;
                })
                .collect(Collectors.toList());
            
            if (ordersTableView != null) {
                ordersTableView.getItems().clear();
                ordersTableView.getItems().addAll(filteredOrders);
                statusLabel.setText("Found " + filteredOrders.size() + " matching orders");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error searching orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupOrdersTableColumns() {
        if (orderIdColumn != null) {
            orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        
        if (customerNameColumn != null) {
            customerNameColumn.setCellValueFactory(cellData -> {
                try {
                    UserService userService = new UserService();
                    User user = userService.getUserById(cellData.getValue().getUserId());
                    return new SimpleStringProperty(user != null ? user.getFullName() : "Unknown");
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new SimpleStringProperty("Error");
                }
            });
        }
        
        if (orderDateColumn != null) {
            orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        }
        
        if (totalAmountColumn != null) {
            totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        }
        
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
        
        if (orderActionColumn != null) {
            orderActionColumn.setCellFactory(col -> {
                return new TableCell<Order, Void>() {
                    private final Button viewButton = new Button("View");
                    private final Button completeButton = new Button("Complete");
                    private final HBox actionBox = new HBox(5, viewButton, completeButton);
                    
                    {
                        viewButton.getStyleClass().add("small-button");
                        completeButton.getStyleClass().add("small-button");
                        completeButton.getStyleClass().add("primary-button");
                        
                        viewButton.setOnAction(event -> {
                            Order order = getTableView().getItems().get(getIndex());
                            showOrderDetailsDialog(order);
                        });
                        
                        completeButton.setOnAction(event -> {
                            Order order = getTableView().getItems().get(getIndex());
                            if (order.getStatus() != Order.Status.DELIVERED) {
                                try {
                                    OrderService orderService = new OrderService();
                                    orderService.updateOrderStatus(order.getId(), "DELIVERED");
                                    loadOrders();
                                    loadCompletedOrders();
                                    statusLabel.setText("Order #" + order.getId() + " marked as completed");
                                } catch (SQLException e) {
                                    statusLabel.setText("Error updating order status: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Order order = getTableView().getItems().get(getIndex());
                            completeButton.setDisable(order.getStatus() == Order.Status.DELIVERED);
                            setGraphic(actionBox);
                        }
                    }
                };
            });
        }
    }
}