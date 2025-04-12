package com.bookshop.controllers;

import com.bookshop.models.CartItem;
import com.bookshop.models.Order;
import com.bookshop.models.User;
import com.bookshop.services.CartService;
import com.bookshop.services.PaymentStrategy;
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;

/**
 * Controller for the shopping cart view.
 */
public class ShoppingCartController {
    
    @FXML private TableView<CartItem> cartTableView;
    @FXML private TableColumn<CartItem, String> titleColumn;
    @FXML private TableColumn<CartItem, String> authorColumn;
    @FXML private TableColumn<CartItem, String> priceColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, String> subtotalColumn;
    @FXML private Label totalLabel;
    @FXML private Button checkoutButton;
    @FXML private Button increaseQuantityButton;
    @FXML private Button decreaseQuantityButton;
    @FXML private Button removeItemButton;
    @FXML private Button clearCartButton;
    @FXML private Button backToShopButton;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    
    private CartService cartService;
    private PurchaseService purchaseService;
    private User currentUser;
    private ObservableList<CartItem> cartItems;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        cartService = new CartService();
        purchaseService = new PurchaseService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            // If not logged in, redirect to login page
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        // Setup table columns
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        authorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookAuthor()));
        priceColumn.setCellValueFactory(cellData -> {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            return new SimpleStringProperty(currencyFormat.format(cellData.getValue().getBookPrice()));
        });
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        subtotalColumn.setCellValueFactory(cellData -> {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            return new SimpleStringProperty(currencyFormat.format(cellData.getValue().getSubtotal()));
        });
        
        // Setup payment method combo box
        paymentMethodComboBox.getItems().addAll("Credit Card", "PayPal", "Bank Transfer");
        paymentMethodComboBox.getSelectionModel().selectFirst();
        
        // Load cart items
        loadCartItems();
        
        // Setup button states based on selection
        cartTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            increaseQuantityButton.setDisable(!hasSelection);
            decreaseQuantityButton.setDisable(!hasSelection);
            removeItemButton.setDisable(!hasSelection);
        });
    }
    
    /**
     * Loads cart items from the database.
     */
    private void loadCartItems() {
        try {
            cartItems = FXCollections.observableArrayList(cartService.getCartItems(currentUser.getId()));
            cartTableView.setItems(cartItems);
            
            // Update the total amount
            updateTotal();
            
            // Update UI state based on cart content
            boolean hasItems = !cartItems.isEmpty();
            checkoutButton.setDisable(!hasItems);
            clearCartButton.setDisable(!hasItems);
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading cart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the total display.
     */
    private void updateTotal() {
        try {
            BigDecimal total = cartService.calculateTotal(currentUser.getId());
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            totalLabel.setText("Total: " + currencyFormat.format(total));
        } catch (SQLException e) {
            statusLabel.setText("Error calculating total: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the increase quantity button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleIncreaseQuantity(ActionEvent event) {
        CartItem selectedItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                int newQuantity = selectedItem.getQuantity() + 1;
                boolean success = cartService.updateCartItemQuantity(selectedItem.getId(), newQuantity);
                
                if (success) {
                    selectedItem.setQuantity(newQuantity);
                    cartTableView.refresh();
                    updateTotal();
                } else {
                    statusLabel.setText("Could not increase quantity. Check book availability.");
                }
            } catch (SQLException e) {
                statusLabel.setText("Error updating quantity: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handles the decrease quantity button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleDecreaseQuantity(ActionEvent event) {
        CartItem selectedItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getQuantity() > 1) {
            try {
                int newQuantity = selectedItem.getQuantity() - 1;
                boolean success = cartService.updateCartItemQuantity(selectedItem.getId(), newQuantity);
                
                if (success) {
                    selectedItem.setQuantity(newQuantity);
                    cartTableView.refresh();
                    updateTotal();
                } else {
                    statusLabel.setText("Could not decrease quantity.");
                }
            } catch (SQLException e) {
                statusLabel.setText("Error updating quantity: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handles the remove item button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleRemoveItem(ActionEvent event) {
        CartItem selectedItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                boolean success = cartService.removeFromCart(selectedItem.getId());
                
                if (success) {
                    cartItems.remove(selectedItem);
                    updateTotal();
                    
                    // Update UI state based on cart content
                    boolean hasItems = !cartItems.isEmpty();
                    checkoutButton.setDisable(!hasItems);
                    clearCartButton.setDisable(!hasItems);
                } else {
                    statusLabel.setText("Could not remove item from cart.");
                }
            } catch (SQLException e) {
                statusLabel.setText("Error removing item: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handles the clear cart button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleClearCart(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Clear Shopping Cart");
        alert.setContentText("Are you sure you want to remove all items from your cart?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = cartService.clearCart(currentUser.getId());
                    
                    if (success) {
                        cartItems.clear();
                        updateTotal();
                        
                        // Update UI state
                        checkoutButton.setDisable(true);
                        clearCartButton.setDisable(true);
                        
                        statusLabel.setText("Cart cleared successfully.");
                    } else {
                        statusLabel.setText("Failed to clear cart.");
                    }
                } catch (SQLException e) {
                    statusLabel.setText("Error clearing cart: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Handles the checkout button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            statusLabel.setText("Your cart is empty. Add some books first.");
            return;
        }
        
        if (paymentMethodComboBox.getValue() == null) {
            statusLabel.setText("Please select a payment method.");
            return;
        }
        
        try {
            // Create appropriate payment strategy based on selection
            PaymentStrategy paymentStrategy = createPaymentStrategy(paymentMethodComboBox.getValue());
            
            // Process the purchase
            Order order = purchaseService.processPurchase(cartItems, currentUser, paymentStrategy);
            
            if (order != null) {
                // Clear the cart after successful purchase
                cartService.clearCart(currentUser.getId());
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Order Placed");
                alert.setHeaderText("Order Successfully Placed");
                alert.setContentText("Your order has been successfully placed. Order ID: " + order.getId());
                
                // When user closes the alert, navigate to order history
                alert.showAndWait().ifPresent(response -> {
                    ViewNavigator.getInstance().navigateTo("customer_orders.fxml");
                });
            } else {
                statusLabel.setText("Failed to process your order. Please try again.");
            }
            
        } catch (SQLException e) {
            statusLabel.setText("Error processing checkout: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a payment strategy based on the selected payment method.
     * 
     * @param paymentMethod The selected payment method
     * @return The payment strategy
     */
    private PaymentStrategy createPaymentStrategy(String paymentMethod) {
        // This would be implemented with actual payment strategies
        // For now, we'll use a dummy implementation that always succeeds
        return new PaymentStrategy() {
            @Override
            public boolean processPayment(User user, BigDecimal amount) {
                // In a real application, this would process the actual payment
                return true;
            }
        };
    }
    
    /**
     * Handles the back to shop button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleBackToShop(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
    }
}