package com.bookshop.controllers;

import com.bookshop.models.CartItem;
import com.bookshop.models.Order;
import com.bookshop.models.User;
import com.bookshop.services.CartService;
import com.bookshop.services.CartService.CartUpdateListener;
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

public class ShoppingCartController implements CartUpdateListener {
    
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
    @FXML private Button continueShoppingButton;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    
    private CartService cartService;
    private PurchaseService purchaseService;
    private User currentUser;
    private ObservableList<CartItem> cartItems;
    
    @FXML
    public void initialize() {
        cartService = CartService.getInstance();
        purchaseService = new PurchaseService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        cartService.addCartUpdateListener(this);
        
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
        
        paymentMethodComboBox.getItems().addAll("Credit Card", "PayPal", "Bank Transfer");
        paymentMethodComboBox.getSelectionModel().selectFirst();
        
        loadCartItems();
        
        cartTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            increaseQuantityButton.setDisable(!hasSelection);
            decreaseQuantityButton.setDisable(!hasSelection);
            removeItemButton.setDisable(!hasSelection);
        });
    }
    
    private void loadCartItems() {
        try {
            cartItems = FXCollections.observableArrayList(cartService.getCartItems(currentUser.getId()));
            cartTableView.setItems(cartItems);
            
            updateTotal();
            
            boolean hasItems = !cartItems.isEmpty();
            checkoutButton.setDisable(!hasItems);
            clearCartButton.setDisable(!hasItems);
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading cart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
    
    @FXML
    public void handleRemoveItem(ActionEvent event) {
        CartItem selectedItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                boolean success = cartService.removeFromCart(selectedItem.getId());
                
                if (success) {
                    cartItems.remove(selectedItem);
                    updateTotal();
                    
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
    
    @FXML
    public void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            statusLabel.setText("Your cart is empty. Add some books first.");
            return;
        }
        
        System.out.println("Checkout for user ID: " + currentUser.getId() + ", Name: " + currentUser.getFullName());
        
        User sessionUser = SessionManager.getInstance().getCurrentUser();
        if (sessionUser != null) {
            System.out.println("Session user ID: " + sessionUser.getId() + ", Name: " + sessionUser.getFullName());
            if (sessionUser.getId() != currentUser.getId()) {
                System.out.println("WARNING: Session user doesn't match controller user. Updating to session user.");
                currentUser = sessionUser;
            }
        }
        
        checkoutButton.setDisable(true);
        statusLabel.setText("Processing your order...");
        
        PaymentStrategy paymentStrategy = createPaymentStrategy("Standard");
        
        try {
            Order order = purchaseService.processPurchase(cartItems, currentUser, paymentStrategy);
            
            if (order != null) {
                System.out.println("Order created successfully with ID: " + order.getId() + 
                                 " for user ID: " + order.getUserId());
                
                try {
                    cartService.clearCart(currentUser.getId());
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Order Placed");
                    alert.setHeaderText("Order Successfully Placed");
                    alert.setContentText("Your order #" + order.getId() + " has been recorded. You can view it in your orders section. User ID: " + currentUser.getId());
                    
                    alert.showAndWait().ifPresent(response -> {
                        cartService.removeCartUpdateListener(this);
                        try {
                            System.out.println("Navigating to orders view for user ID: " + currentUser.getId());
                            ViewNavigator.getInstance().navigateTo("customer_orders.fxml");
                        } catch (Exception ex) {
                            System.err.println("Error navigating to orders view: " + ex.getMessage());
                            try {
                                ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
                            } catch (Exception e2) {
                                statusLabel.setText("Error: Could not navigate to any view. Please restart the application.");
                            }
                        }
                    });
                } catch (SQLException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Order Placed - With Warning");
                    alert.setHeaderText("Order Successfully Placed");
                    alert.setContentText("Your order #" + order.getId() + " has been recorded, but there was an issue clearing your cart. " +
                                         "You may need to clear it manually.");
                    
                    alert.showAndWait().ifPresent(response -> {
                        cartService.removeCartUpdateListener(this);
                        try {
                            System.out.println("Navigating to orders view for user ID: " + currentUser.getId());
                            ViewNavigator.getInstance().navigateTo("customer_orders.fxml");
                        } catch (Exception ex) {
                            System.err.println("Error navigating to orders view: " + ex.getMessage());
                            try {
                                ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
                            } catch (Exception e2) {
                                statusLabel.setText("Error: Could not navigate to any view. Please restart the application.");
                            }
                        }
                    });
                }
            } else {
                System.err.println("Failed to create order for user ID: " + currentUser.getId());
                statusLabel.setText("Failed to create order. Please try again.");
                checkoutButton.setDisable(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: " + e.getMessage());
            checkoutButton.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Unexpected error: " + e.getMessage());
            checkoutButton.setDisable(false);
        }
    }
    
    private PaymentStrategy createPaymentStrategy(String paymentMethod) {
        return new PaymentStrategy() {
            @Override
            public boolean processPayment(User user, BigDecimal amount) {
                return true;
            }
        };
    }
    
    @FXML
    public void handleBackToShop(ActionEvent event) {
        cartService.removeCartUpdateListener(this);
        ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
    }
    
    @FXML
    public void handleContinueShopping(ActionEvent event) {
        cartService.removeCartUpdateListener(this);
        ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
    }
    
    @Override
    public void onCartUpdated(int userId) {
        if (currentUser != null && currentUser.getId() == userId) {
            javafx.application.Platform.runLater(this::loadCartItems);
        }
    }
}