package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.OrderService;
import com.bookshop.services.UserService;
import com.bookshop.utils.SceneManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public class AdminOrdersController {

    @FXML private Button backButton;
    @FXML private Label titleLabel;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Button refreshButton;
    
    @FXML private TableView<Order> ordersTableView;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, Integer> userIdColumn;
    @FXML private TableColumn<Order, String> userNameColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, String> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    
    @FXML private TextArea orderDetailsLabel;
    
    @FXML private TableView<OrderItem> orderItemsTableView;
    @FXML private TableColumn<OrderItem, Integer> bookIdColumn;
    @FXML private TableColumn<OrderItem, String> bookTitleColumn;
    @FXML private TableColumn<OrderItem, String> bookAuthorColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, String> priceColumn;
    @FXML private TableColumn<OrderItem, String> subtotalColumn;
    
    @FXML private ComboBox<String> statusUpdateComboBox;
    @FXML private Button updateStatusButton;
    @FXML private Label statusLabel;
    
    private OrderService orderService;
    private UserService userService;
    private BookService bookService;
    private Map<Integer, User> userCache = new HashMap<>();
    private Map<Integer, Book> bookCache = new HashMap<>();
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private Order selectedOrder = null;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @FXML
    public void initialize() {
        orderService = new OrderService();
        userService = new UserService();
        bookService = new BookService();
        
        setupTables();
        setupStatusFilters();
        loadOrders(null);
        
        ordersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldOrder, newOrder) -> {
            if (newOrder != null) {
                displayOrderDetails(newOrder);
            } else {
                clearOrderDetails();
            }
        });
    }
    
    private void setupTables() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        
        userNameColumn.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            User user = getUserById(userId);
            return new SimpleStringProperty(user != null ? user.getUsername() : "Unknown");
        });
        
        orderDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getOrderDate();
            return new SimpleStringProperty(dateTime != null ? dateTime.format(dateFormatter) : "");
        });
        
        totalColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getTotalAmount())));
            
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        
        bookIdColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getBookId()).asObject());
            
        bookTitleColumn.setCellValueFactory(cellData -> {
            Book book = getBookById(cellData.getValue().getBookId());
            return new SimpleStringProperty(book != null ? book.getTitle() : "Unknown");
        });
        
        bookAuthorColumn.setCellValueFactory(cellData -> {
            Book book = getBookById(cellData.getValue().getBookId());
            return new SimpleStringProperty(book != null ? book.getAuthor() : "Unknown");
        });
        
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        priceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getPrice())));
            
        subtotalColumn.setCellValueFactory(cellData -> {
            OrderItem item = cellData.getValue();
            BigDecimal price = item.getPrice();
            BigDecimal quantity = new BigDecimal(item.getQuantity());
            BigDecimal subtotal = price.multiply(quantity);
            return new SimpleStringProperty(currencyFormat.format(subtotal));
        });
    }
    
    private void setupStatusFilters() {
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("All");
        statusOptions.add("PENDING");
        statusOptions.add("PROCESSING");
        statusOptions.add("SHIPPED");
        statusOptions.add("DELIVERED");
        statusOptions.add("CANCELLED");
        
        statusFilterComboBox.setItems(FXCollections.observableArrayList(statusOptions));
        statusFilterComboBox.getSelectionModel().select("All");
        
        statusFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String status = "All".equals(newVal) ? null : newVal;
                loadOrders(status);
            }
        });
        
        statusUpdateComboBox.setItems(FXCollections.observableArrayList(
            "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"));
    }
    
    private void loadOrders(String statusFilter) {
        try {
            System.out.println("AdminOrdersController: Loading orders with filter: " + statusFilter);
            List<Order> allOrders = orderService.getAllOrders();
            System.out.println("AdminOrdersController: Found " + allOrders.size() + " orders");
            
            orders.clear();
            
            if (statusFilter == null || statusFilter.isEmpty()) {
                orders.addAll(allOrders);
            } else {
                for (Order order : allOrders) {
                    if (statusFilter.equalsIgnoreCase(order.getStatus().toString())) {
                        orders.add(order);
                    }
                }
            }
            
            ordersTableView.setItems(orders);
            
            if (!orders.isEmpty()) {
                ordersTableView.getSelectionModel().select(0);
            } else {
                clearOrderDetails();
            }
            
            statusLabel.setText("Loaded " + orders.size() + " orders");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading orders: " + e.getMessage());
            System.err.println("AdminOrdersController: Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void displayOrderDetails(Order order) {
        selectedOrder = order;
        
        User user = getUserById(order.getUserId());
        StringBuilder details = new StringBuilder();
        details.append("Order #").append(order.getId())
               .append(" placed by ").append(user != null ? user.getUsername() : "Unknown")
               .append(" (User ID: ").append(order.getUserId()).append(")\n")
               .append("Date: ").append(order.getOrderDate().format(dateFormatter)).append("\n")
               .append("Status: ").append(order.getStatus()).append("\n")
               .append("Total: ").append(currencyFormat.format(order.getTotalAmount()));
               
        orderDetailsLabel.setText(details.toString());
        
        try {
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            orderItemsTableView.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading order items: " + e.getMessage());
            orderItemsTableView.setItems(FXCollections.observableArrayList());
        }
        
        statusUpdateComboBox.getSelectionModel().select(order.getStatus().toString());
    }
    
    private void clearOrderDetails() {
        selectedOrder = null;
        orderDetailsLabel.setText("");
        orderItemsTableView.setItems(FXCollections.observableArrayList());
    }
    
    private User getUserById(int userId) {
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }
        
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                userCache.put(userId, user);
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Book getBookById(int bookId) {
        if (bookCache.containsKey(bookId)) {
            return bookCache.get(bookId);
        }
        
        try {
            Book book = bookService.getBookById(bookId);
            if (book != null) {
                bookCache.put(bookId, book);
            }
            return book;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @FXML
    private void handleRefresh() {
        String status = statusFilterComboBox.getValue();
        loadOrders("All".equals(status) ? null : status);
    }
    
    @FXML
    private void handleUpdateStatus() {
        if (selectedOrder == null) {
            statusLabel.setText("No order selected");
            return;
        }
        
        String newStatus = statusUpdateComboBox.getValue();
        if (newStatus == null || newStatus.isEmpty()) {
            statusLabel.setText("Please select a status");
            return;
        }
        
        try {
            selectedOrder.setStatus(newStatus);
            boolean success = orderService.updateOrderStatus(selectedOrder.getId(), newStatus);
            
            if (success) {
                statusLabel.setText("Order status updated to " + newStatus);
                handleRefresh();
            } else {
                statusLabel.setText("Failed to update order status");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error updating order status: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBack() {
        SceneManager.getInstance().loadScene("admin_dashboard.fxml");
    }
} 