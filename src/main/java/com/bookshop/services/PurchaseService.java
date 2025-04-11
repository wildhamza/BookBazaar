package com.bookshop.services;

import com.bookshop.db.DatabaseConnection;
import com.bookshop.models.CartItem;
import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling purchase transactions.
 * Uses Strategy pattern for payment processing.
 */
public class PurchaseService {
    
    private Connection conn;
    private BookService bookService;
    
    public PurchaseService() {
        conn = DatabaseConnection.getInstance().getConnection();
        bookService = new BookService();
    }
    
    /**
     * Processes a purchase using the specified payment strategy.
     * 
     * @param cartItems The items to purchase
     * @param user The user making the purchase
     * @param paymentStrategy The payment strategy to use
     * @return true if the purchase was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean processPurchase(List<CartItem> cartItems, User user, 
                                   PaymentStrategy paymentStrategy) throws SQLException {
        
        if (cartItems == null || cartItems.isEmpty() || user == null) {
            throw new IllegalArgumentException("Invalid cart items or user");
        }
        
        // Start transaction
        conn.setAutoCommit(false);
        
        try {
            // Create order
            Order order = new Order();
            order.setUserId(user.getId());
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(Order.Status.PENDING);
            order.setShippingAddress(user.getAddress());
            order.setPaymentMethod(paymentStrategy.getType());
            
            // Calculate total
            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem(cartItem.getBook(), cartItem.getQuantity());
                order.addOrderItem(orderItem);
            }
            
            // Save order
            int orderId = saveOrder(order);
            if (orderId == -1) {
                throw new SQLException("Failed to save order");
            }
            
            // Save order items
            for (OrderItem orderItem : order.getOrderItems()) {
                orderItem.setOrderId(orderId);
                saveOrderItem(orderItem);
                
                // Update stock
                bookService.updateStockQuantity(orderItem.getBookId(), -orderItem.getQuantity());
            }
            
            // Process payment
            boolean paymentSuccess = paymentStrategy.processPayment(user, order.getTotalAmount());
            if (!paymentSuccess) {
                throw new RuntimeException("Payment processing failed");
            }
            
            // Update order status
            order.setStatus(Order.Status.PROCESSING);
            updateOrderStatus(orderId, order.getStatus());
            
            // Commit transaction
            conn.commit();
            return true;
            
        } catch (Exception e) {
            // Rollback on error
            conn.rollback();
            throw e;
        } finally {
            // Restore auto-commit
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Saves an order to the database.
     * 
     * @param order The order to save
     * @return The generated order ID
     * @throws SQLException If a database error occurs
     */
    private int saveOrder(Order order) throws SQLException {
        String query = "INSERT INTO orders (user_id, order_date, total_amount, status, " +
                       "shipping_address, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getUserId());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            stmt.setBigDecimal(3, order.getTotalAmount());
            stmt.setString(4, order.getStatus().toString());
            stmt.setString(5, order.getShippingAddress());
            stmt.setString(6, order.getPaymentMethod());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return -1;
                }
            }
        }
    }
    
    /**
     * Saves an order item to the database.
     * 
     * @param orderItem The order item to save
     * @throws SQLException If a database error occurs
     */
    private void saveOrderItem(OrderItem orderItem) throws SQLException {
        String query = "INSERT INTO order_items (order_id, book_id, book_title, book_author, " +
                       "quantity, price_at_purchase) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderItem.getOrderId());
            stmt.setInt(2, orderItem.getBookId());
            stmt.setString(3, orderItem.getBookTitle());
            stmt.setString(4, orderItem.getBookAuthor());
            stmt.setInt(5, orderItem.getQuantity());
            stmt.setBigDecimal(6, orderItem.getPriceAtPurchase());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Updates the status of an order.
     * 
     * @param orderId The order ID
     * @param status The new status
     * @throws SQLException If a database error occurs
     */
    private void updateOrderStatus(int orderId, Order.Status status) throws SQLException {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status.toString());
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Gets all orders for a user.
     * 
     * @param userId The user ID
     * @return A list of the user's orders
     * @throws SQLException If a database error occurs
     */
    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        
        List<Order> orders = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setStatus(Order.Status.valueOf(rs.getString("status")));
                    order.setShippingAddress(rs.getString("shipping_address"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    
                    // Load order items
                    order.setOrderItems(getOrderItemsByOrderId(order.getId()));
                    
                    orders.add(order);
                }
            }
        }
        
        return orders;
    }
    
    /**
     * Gets all items for an order.
     * 
     * @param orderId The order ID
     * @return A list of the order's items
     * @throws SQLException If a database error occurs
     */
    public List<OrderItem> getOrderItemsByOrderId(int orderId) throws SQLException {
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        
        List<OrderItem> items = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setBookId(rs.getInt("book_id"));
                    item.setBookTitle(rs.getString("book_title"));
                    item.setBookAuthor(rs.getString("book_author"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPriceAtPurchase(rs.getBigDecimal("price_at_purchase"));
                    
                    items.add(item);
                }
            }
        }
        
        return items;
    }
}
