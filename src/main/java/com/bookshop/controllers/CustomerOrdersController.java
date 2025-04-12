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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the customer orders view.
 */
public class CustomerOrdersController {
    
    @FXML private TableView<Order> ordersTableView;
    @FXML private TableColumn<Order, String> orderIdColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> totalColumn;
    
    @FXML private TableView<OrderItem> orderItemsTableView;
    @FXML private TableColumn<OrderItem, String> bookTitleColumn;
    @FXML private TableColumn<OrderItem, String> bookAuthorColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, String> priceColumn;
    @FXML private TableColumn<OrderItem, String> subtotalColumn;
    
    @FXML private Label orderDetailsLabel;
    @FXML private Label statusLabel;
    @FXML private Button backButton;
    @FXML private Button cancelOrderButton;
    
    private PurchaseService purchaseService;
    private User currentUser;
    private ObservableList<Order> userOrders;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        purchaseService = new PurchaseService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            // If not logged in, redirect to login page
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        // Setup orders table columns
        orderIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        
        orderDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(cellData.getValue().getOrderDate().format(formatter));
        });
        
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        
        totalColumn.setCellValueFactory(cellData -> {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            return new SimpleStringProperty(currencyFormat.format(cellData.getValue().getFinalAmount()));
        });
        
        // Setup order items table columns
        bookTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        bookAuthorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookAuthor()));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        priceColumn.setCellValueFactory(cellData -> {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            return new SimpleStringProperty(currencyFormat.format(cellData.getValue().getPrice()));
        });
        
        subtotalColumn.setCellValueFactory(cellData -> {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            return new SimpleStringProperty(currencyFormat.format(cellData.getValue().getSubtotal()));
        });
        
        // Load orders
        loadOrders();
        
        // Listen for order selection to show details
        ordersTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showOrderDetails(newSelection);
                
                // Enable/disable cancel button based on order status
                boolean canCancel = newSelection.getStatus() == Order.Status.PENDING || 
                                    newSelection.getStatus() == Order.Status.PROCESSING;
                cancelOrderButton.setDisable(!canCancel);
            } else {
                orderItemsTableView.getItems().clear();
                orderDetailsLabel.setText("Select an order to view details");
                cancelOrderButton.setDisable(true);
            }
        });
    }
    
    /**
     * Loads all orders for the current user.
     */
    private void loadOrders() {
        try {
            List<Order> orders = purchaseService.getOrdersByUserId(currentUser.getId());
            userOrders = FXCollections.observableArrayList(orders);
            ordersTableView.setItems(userOrders);
            
            // Clear details
            orderItemsTableView.getItems().clear();
            orderDetailsLabel.setText("Select an order to view details");
            cancelOrderButton.setDisable(true);
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows details for the selected order.
     * 
     * @param order The selected order
     */
    private void showOrderDetails(Order order) {
        // Show order items
        orderItemsTableView.setItems(FXCollections.observableArrayList(order.getOrderItems()));
        
        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        
        // Show order summary
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("Order #").append(order.getId()).append("\n");
        detailsBuilder.append("Status: ").append(order.getStatus()).append("\n");
        detailsBuilder.append("Date: ").append(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        detailsBuilder.append("Subtotal: ").append(currencyFormat.format(order.getTotalAmount())).append("\n");
        
        if (order.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            detailsBuilder.append("Discount: ").append(currencyFormat.format(order.getDiscountAmount())).append("\n");
        }
        
        detailsBuilder.append("Total: ").append(currencyFormat.format(order.getFinalAmount())).append("\n");
        detailsBuilder.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        detailsBuilder.append("Shipping Address: ").append(order.getShippingAddress());
        
        orderDetailsLabel.setText(detailsBuilder.toString());
    }
    
    /**
     * Handles the back button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleBack(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
    }
    
    /**
     * Handles the cancel order button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleCancelOrder(ActionEvent event) {
        Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
        
        if (selectedOrder == null) {
            return;
        }
        
        // Check if order can be cancelled
        if (selectedOrder.getStatus() != Order.Status.PENDING && 
            selectedOrder.getStatus() != Order.Status.PROCESSING) {
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Cancel Order");
            alert.setHeaderText("Order Cannot Be Cancelled");
            alert.setContentText("This order cannot be cancelled because it has already been " + 
                                selectedOrder.getStatus().toString().toLowerCase() + ".");
            alert.showAndWait();
            return;
        }
        
        // Confirm cancellation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancellation");
        alert.setHeaderText("Cancel Order #" + selectedOrder.getId());
        alert.setContentText("Are you sure you want to cancel this order? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = purchaseService.cancelOrder(selectedOrder.getId());
                    
                    if (success) {
                        // Refresh orders
                        loadOrders();
                        statusLabel.setText("Order cancelled successfully.");
                    } else {
                        statusLabel.setText("Failed to cancel order.");
                    }
                } catch (SQLException e) {
                    statusLabel.setText("Error cancelling order: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}