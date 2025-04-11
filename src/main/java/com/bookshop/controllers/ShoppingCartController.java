package com.bookshop.controllers;

import com.bookshop.models.CartItem;
import com.bookshop.models.User;
import com.bookshop.services.CartService;
import com.bookshop.services.PaymentStrategy;
import com.bookshop.services.PurchaseService;
import com.bookshop.services.CreditCardPayment;
import com.bookshop.services.PayPalPayment;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
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
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the shopping cart view.
 */
public class ShoppingCartController implements Initializable {
    
    @FXML private TableView<CartItem> cartTableView;
    @FXML private TableColumn<CartItem, String> titleColumn;
    @FXML private TableColumn<CartItem, String> authorColumn;
    @FXML private TableColumn<CartItem, BigDecimal> priceColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, BigDecimal> subtotalColumn;
    @FXML private Label totalAmountLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Button checkoutButton;
    @FXML private Button continueShoppingButton;
    @FXML private Button clearCartButton;
    
    private CartService cartService;
    private PurchaseService purchaseService;
    private ObservableList<CartItem> cartItems;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Check if user is logged in
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        // Initialize services
        cartService = new CartService();
        purchaseService = new PurchaseService();
        
        // Setup table columns
        initializeTableColumns();
        
        // Setup payment methods
        initializePaymentMethods();
        
        // Load cart items
        loadCartItems();
    }
    
    private void initializeTableColumns() {
        titleColumn.setCellValueFactory(cellData -> {
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getBook().getTitle()
            );
        });
        
        authorColumn.setCellValueFactory(cellData -> {
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getBook().getAuthor()
            );
        });
        
        priceColumn.setCellValueFactory(cellData -> {
            return javafx.beans.binding.Bindings.createObjectBinding(
                () -> cellData.getValue().getBook().getPrice()
            );
        });
        
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        
        // Add column for increase/decrease/remove buttons
        TableColumn<CartItem, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button increaseBtn = new Button("+");
            private final Button decreaseBtn = new Button("-");
            private final Button removeBtn = new Button("Remove");
            
            {
                increaseBtn.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    int stock = item.getBook().getStockQuantity();
                    if (item.getQuantity() < stock) {
                        item.incrementQuantity();
                        updateCart();
                    } else {
                        showAlert("Cannot add more. Only " + stock + " available in stock.");
                    }
                });
                
                decreaseBtn.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    item.decrementQuantity();
                    updateCart();
                });
                
                removeBtn.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartService.removeFromCart(item.getBook());
                    loadCartItems();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
                    hbox.getChildren().addAll(decreaseBtn, increaseBtn, removeBtn);
                    setGraphic(hbox);
                }
            }
        });
        
        cartTableView.getColumns().add(actionColumn);
    }
    
    private void initializePaymentMethods() {
        ObservableList<String> paymentMethods = FXCollections.observableArrayList(
            "Credit Card", "PayPal"
        );
        paymentMethodComboBox.setItems(paymentMethods);
        paymentMethodComboBox.setValue("Credit Card");
    }
    
    private void loadCartItems() {
        cartItems = FXCollections.observableArrayList(cartService.getCartItems());
        cartTableView.setItems(cartItems);
        
        updateTotalAmount();
        
        // Enable/disable checkout button based on cart state
        checkoutButton.setDisable(cartItems.isEmpty());
        clearCartButton.setDisable(cartItems.isEmpty());
    }
    
    private void updateCart() {
        for (CartItem item : cartItems) {
            cartService.updateCartItemQuantity(item.getBook(), item.getQuantity());
        }
        
        updateTotalAmount();
    }
    
    private void updateTotalAmount() {
        BigDecimal total = cartService.calculateTotal();
        totalAmountLabel.setText("Total: $" + total);
    }
    
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert("Your cart is empty");
            return;
        }
        
        // Confirm checkout
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Checkout");
        confirmAlert.setHeaderText("Proceed with checkout?");
        confirmAlert.setContentText("Total amount: $" + cartService.calculateTotal());
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        
        // Select payment strategy based on selected payment method
        PaymentStrategy paymentStrategy;
        String paymentMethod = paymentMethodComboBox.getValue();
        
        if ("PayPal".equals(paymentMethod)) {
            paymentStrategy = new PayPalPayment();
        } else {
            paymentStrategy = new CreditCardPayment();
        }
        
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            
            // Process the payment and create the order
            boolean success = purchaseService.processPurchase(cartItems, currentUser, paymentStrategy);
            
            if (success) {
                // Clear the cart after successful purchase
                cartService.clearCart();
                
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Purchase Complete");
                successAlert.setHeaderText("Thank you for your purchase!");
                successAlert.setContentText("Your order has been placed successfully.");
                successAlert.showAndWait();
                
                // Navigate to customer dashboard
                ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
            }
        } catch (Exception e) {
            showAlert("Error processing purchase: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClearCart(ActionEvent event) {
        if (cartItems.isEmpty()) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Clear Shopping Cart");
        alert.setContentText("Are you sure you want to remove all items from your cart?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            cartService.clearCart();
            loadCartItems();
        }
    }
    
    @FXML
    private void handleContinueShopping(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
