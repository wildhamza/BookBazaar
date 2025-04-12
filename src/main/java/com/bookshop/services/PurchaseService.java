package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.models.User;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling purchase-related operations such as creating orders,
 * processing payments, and applying discounts.
 */
public class PurchaseService {
    
    private DiscountService discountService;
    private BookService bookService;
    
    public PurchaseService() {
        this.discountService = new DiscountService();
        this.bookService = new BookService();
    }
    
    /**
     * Create an order from cart items for a user with loyalty discounts applied.
     * 
     * @param user The user making the purchase
     * @param cartItems The items in the user's cart
     * @param shippingAddress The shipping address for the order
     * @param paymentMethod The payment method for the order
     * @return The created order
     * @throws SQLException If a database error occurs
     */
    public Order createOrder(User user, List<CartItem> cartItems, String shippingAddress, String paymentMethod) 
            throws SQLException {
        if (user == null || cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("User and cart items cannot be null or empty");
        }
        
        // Create a new order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        
        // Add all items to the order
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            int quantity = cartItem.getQuantity();
            
            // Check if there's enough stock
            if (book.getStockQuantity() < quantity) {
                throw new IllegalStateException("Not enough stock for book: " + book.getTitle());
            }
            
            // Create an order item from the cart item
            OrderItem orderItem = new OrderItem(book, quantity);
            order.addItem(orderItem);
            
            // Update the book stock quantity
            bookService.updateStockQuantity(book.getId(), -quantity);
        }
        
        // Calculate the total amount
        BigDecimal totalAmount = order.getTotalAmount();
        
        // Apply any applicable discounts
        BigDecimal discountAmount = discountService.calculateDiscountAmount(user, totalAmount);
        order.setDiscountAmount(discountAmount);
        
        // Save the order to the database
        saveOrder(order);
        
        // Update the user's order count
        user.incrementOrderCount();
        updateUserOrderCount(user);
        
        return order;
    }
    
    /**
     * Save an order to the database.
     * 
     * @param order The order to save
     * @throws SQLException If a database error occurs
     */
    private void saveOrder(Order order) throws SQLException {
        // Implementation would insert the order into the database and get the generated ID
        // Then insert all order items with the order ID
        // This is a placeholder implementation
        
        // TODO: Implement the actual database operation
    }
    
    /**
     * Update a user's order count in the database.
     * 
     * @param user The user to update
     * @throws SQLException If a database error occurs
     */
    private void updateUserOrderCount(User user) throws SQLException {
        // Implementation would update the user's order count in the database
        // This is a placeholder implementation
        
        // TODO: Implement the actual database operation
    }
    
    /**
     * Get the discount service.
     * 
     * @return The discount service
     */
    public DiscountService getDiscountService() {
        return discountService;
    }
    
    /**
     * Set the discount service.
     * 
     * @param discountService The discount service to set
     */
    public void setDiscountService(DiscountService discountService) {
        this.discountService = discountService;
    }
    
    /**
     * Get the book service.
     * 
     * @return The book service
     */
    public BookService getBookService() {
        return bookService;
    }
    
    /**
     * Set the book service.
     * 
     * @param bookService The book service to set
     */
    public void setBookService(BookService bookService) {
        this.bookService = bookService;
    }
}