package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.utils.BookFactory;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the admin dashboard view.
 */
public class AdminDashboardController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;
    
    // Books Tab
    @FXML private Tab booksTab;
    @FXML private TextField bookSearchField;
    @FXML private TableView<Book> booksTableView;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> publisherColumn;
    @FXML private TableColumn<Book, BigDecimal> priceColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, Integer> stockColumn;
    @FXML private TableColumn<Book, Void> actionColumn;
    
    // Customers Tab
    @FXML private Tab customersTab;
    @FXML private TextField customerSearchField;
    @FXML private TableView<User> customersTableView;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> addressColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, Void> customerActionColumn;
    
    private BookService bookService;
    private UserService userService;
    private ObservableList<Book> bookList;
    private ObservableList<User> customerList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Check if user is logged in as admin
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        welcomeLabel.setText("Welcome, Admin " + currentUser.getFullName());
        
        // Initialize services
        bookService = new BookService();
        userService = new UserService();
        
        // Setup Books Tab
        initializeBooksTab();
        
        // Setup Customers Tab
        initializeCustomersTab();
        
        // Load data for first tab
        loadAllBooks();
    }
    
    private void initializeBooksTab() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Action column with Edit and Delete buttons
        actionColumn.setCellFactory(createBookActionColumnCellFactory());
    }
    
    private void initializeCustomersTab() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        // Action column with View Orders button
        customerActionColumn.setCellFactory(createCustomerActionColumnCellFactory());
        
        // Tab selection listener to load data when tab is selected
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == customersTab) {
                loadAllCustomers();
            }
        });
    }
    
    private Callback<TableColumn<Book, Void>, TableCell<Book, Void>> createBookActionColumnCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Book, Void> call(final TableColumn<Book, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final Button stockBtn = new Button("Stock");
                    private final VBox box = new VBox(5, editBtn, deleteBtn, stockBtn);
                    
                    {
                        editBtn.setMaxWidth(Double.MAX_VALUE);
                        deleteBtn.setMaxWidth(Double.MAX_VALUE);
                        stockBtn.setMaxWidth(Double.MAX_VALUE);
                        
                        editBtn.setOnAction((ActionEvent event) -> {
                            Book book = getTableView().getItems().get(getIndex());
                            handleEditBook(book);
                        });
                        
                        deleteBtn.setOnAction((ActionEvent event) -> {
                            Book book = getTableView().getItems().get(getIndex());
                            handleDeleteBook(book);
                        });
                        
                        stockBtn.setOnAction((ActionEvent event) -> {
                            Book book = getTableView().getItems().get(getIndex());
                            handleUpdateStock(book);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(box);
                        }
                    }
                };
            }
        };
    }
    
    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createCustomerActionColumnCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button viewOrdersBtn = new Button("View Orders");
                    
                    {
                        viewOrdersBtn.setOnAction((ActionEvent event) -> {
                            User customer = getTableView().getItems().get(getIndex());
                            handleViewCustomerOrders(customer);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(viewOrdersBtn);
                        }
                    }
                };
            }
        };
    }
    
    private void loadAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            bookList = FXCollections.observableArrayList(books);
            booksTableView.setItems(bookList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAllCustomers() {
        try {
            List<User> customers = userService.getAllCustomers();
            customerList = FXCollections.observableArrayList(customers);
            customersTableView.setItems(customerList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load customers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddNewBook(ActionEvent event) {
        // Clear any selected book
        SessionManager.getInstance().setSelectedBook(null);
        ViewNavigator.getInstance().navigateTo("edit_book.fxml");
    }
    
    @FXML
    private void handleBookSearch(ActionEvent event) {
        try {
            String searchTerm = bookSearchField.getText().trim();
            List<Book> searchResults = bookService.searchBooks(searchTerm, "", "title", true);
            bookList = FXCollections.observableArrayList(searchResults);
            booksTableView.setItems(bookList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCustomerSearch(ActionEvent event) {
        try {
            String searchTerm = customerSearchField.getText().trim();
            List<User> searchResults = userService.searchCustomers(searchTerm);
            customerList = FXCollections.observableArrayList(searchResults);
            customersTableView.setItems(customerList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
        ViewNavigator.getInstance().navigateTo("login.fxml");
    }
    
    private void handleEditBook(Book book) {
        SessionManager.getInstance().setSelectedBook(book);
        ViewNavigator.getInstance().navigateTo("edit_book.fxml");
    }
    
    private void handleDeleteBook(Book book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Book");
        alert.setContentText("Are you sure you want to delete '" + book.getTitle() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                bookService.deleteBook(book.getId());
                bookList.remove(book);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book deleted successfully");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete book: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void handleUpdateStock(Book book) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(book.getStockQuantity()));
        dialog.setTitle("Update Stock");
        dialog.setHeaderText("Update Stock Quantity for '" + book.getTitle() + "'");
        dialog.setContentText("Enter new stock quantity:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newStock = Integer.parseInt(result.get());
                if (newStock < 0) {
                    throw new IllegalArgumentException("Stock cannot be negative");
                }
                
                book.setStockQuantity(newStock);
                bookService.updateBook(book);
                
                // Refresh table
                loadAllBooks();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Stock updated successfully");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid number");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update stock: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void handleViewCustomerOrders(User customer) {
        SessionManager.getInstance().setSelectedCustomer(customer);
        ViewNavigator.getInstance().navigateTo("customer_orders.fxml");
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
