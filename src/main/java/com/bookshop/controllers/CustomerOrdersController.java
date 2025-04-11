package com.bookshop.controllers;

import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.models.User;
import com.bookshop.services.PurchaseService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the customer orders view.
 */
public class CustomerOrdersController implements Initializable {
    
    @FXML private Label titleLabel;
    @FXML private TableView<Order> ordersTableView;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, BigDecimal> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    
    @FXML private TableView<OrderItem> orderItemsTableView;
    @FXML private TableColumn<OrderItem, String> bookTitleColumn;
    @FXML private TableColumn<OrderItem, String> bookAuthorColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> priceColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> subtotalColumn;
    
    @FXML private Label addressLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label orderStatusLabel;
    @FXML private Button backButton;
    
    private PurchaseService purchaseService;
    private ObservableList<Order> ordersList;
    private ObservableList<OrderItem> orderItemsList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize purchase service
        purchaseService = new PurchaseService();
        
        // Setup UI based on whether we're viewing from admin or customer perspective
        User selectedCustomer = SessionManager.getInstance().getSelectedCustomer();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (selectedCustomer != null && currentUser.isAdmin()) {
            // Admin viewing a customer's orders
            titleLabel.setText("Orders for " + selectedCustomer.getFullName());
            loadOrders(selectedCustomer.getId());
        } else if (currentUser != null) {
            // Customer viewing their own orders
            titleLabel.setText("My Orders");
            loadOrders(currentUser.getId());
        } else {
            // Not logged in or invalid state
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        // Initialize table columns
        initializeOrdersTable();
        initializeOrderItemsTable();
        
        // Add selection listener to orders table
        ordersTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showOrderDetails(newSelection);
                }
            }
        );
    }
    
    private void initializeOrdersTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }
    
    private void initializeOrderItemsTable() {
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("bookAuthor"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("priceAtPurchase"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }
    
    private void loadOrders(int userId) {
        try {
            List<Order> orders = purchaseService.getOrdersByUserId(userId);
            ordersList = FXCollections.observableArrayList(orders);
            ordersTableView.setItems(ordersList);
            
            // Select first order if available
            if (!orders.isEmpty()) {
                ordersTableView.getSelectionModel().select(0);
                showOrderDetails(orders.get(0));
            }
        } catch (Exception e) {
            showAlert("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showOrderDetails(Order order) {
        // Show order items
        orderItemsList = FXCollections.observableArrayList(order.getOrderItems());
        orderItemsTableView.setItems(orderItemsList);
        
        // Show order details
        addressLabel.setText(order.getShippingAddress());
        paymentMethodLabel.setText(order.getPaymentMethod());
        orderStatusLabel.setText(order.getStatus().toString());
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser.isAdmin()) {
            // Clear selected customer and return to admin dashboard
            SessionManager.getInstance().setSelectedCustomer(null);
            ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
        } else {
            // Return to customer dashboard
            ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
