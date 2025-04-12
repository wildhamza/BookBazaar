package com.bookshop.services;

import com.bookshop.models.*;
import com.bookshop.utils.DatabaseConnection;

import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing purchases and orders.
 */
public class PurchaseService {
    
    private Connection connection;
    private BookService bookService;
    
    /**
     * Default constructor.
     */
    public PurchaseService() {
        try {
            this.connection = com.bookshop.utils.DatabaseConnection.getInstance().getConnection();
            this.bookService = new BookService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Processes a purchase from the cart items.
     * 
     * @param cartItems The cart items to purchase
     * @param user The user making the purchase
     * @param paymentStrategy The payment strategy to use
     * @return The created order if successful, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Order processPurchase(ObservableList<CartItem> cartItems, User user, PaymentStrategy paymentStrategy) throws SQLException {
        if (cartItems == null || cartItems.isEmpty() || user == null || paymentStrategy == null) {
            return null;
        }
        
        // Begin transaction
        connection.setAutoCommit(false);
        
        try {
            // 1. Create order
            Order order = new Order();
            order.setUserId(user.getId());
            order.setShippingAddress(user.getAddress());
            order.setPaymentMethod(paymentStrategy.getClass().getSimpleName());
            
            // 2. Calculate total (reuse cart service logic for discount calculations)
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                total = total.add(item.getSubtotal());
            }
            order.setTotalAmount(total);
            
            // 3. Apply any discounts based on user's loyalty status
            DiscountStrategy discountStrategy;
            if (user.isPremiumLoyaltyMember()) {
                discountStrategy = new PremiumMemberDiscount();
            } else if (user.isRegularLoyaltyMember()) {
                discountStrategy = new RegularMemberDiscount();
            } else {
                discountStrategy = new NoDiscount();
            }
            
            BigDecimal discountAmount = discountStrategy.calculateDiscount(user, total);
            order.setDiscountAmount(discountAmount);
            
            // 4. Process payment
            BigDecimal finalAmount = order.getFinalAmount();
            boolean paymentSuccessful = paymentStrategy.processPayment(user, finalAmount);
            
            if (!paymentSuccessful) {
                connection.rollback();
                return null;
            }
            
            // 5. Create order in database
            String insertOrderQuery = "INSERT INTO orders (user_id, order_date, status, total_amount, discount_amount, " +
                                     "final_amount, shipping_address, payment_method) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            
            try (PreparedStatement pstmt = connection.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getUserId());
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(3, order.getStatus().name());
                pstmt.setBigDecimal(4, order.getTotalAmount());
                pstmt.setBigDecimal(5, order.getDiscountAmount());
                pstmt.setBigDecimal(6, order.getFinalAmount());
                pstmt.setString(7, order.getShippingAddress());
                pstmt.setString(8, order.getPaymentMethod());
                
                pstmt.executeUpdate();
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                    } else {
                        connection.rollback();
                        return null;
                    }
                }
            }
            
            // 6. Create order items
            String insertOrderItemQuery = "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(insertOrderItemQuery)) {
                for (CartItem cartItem : cartItems) {
                    // Create and add order item
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(order.getId());
                    orderItem.setBookId(cartItem.getBookId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getBookPrice());
                    order.addItem(orderItem);
                    
                    // Insert into database
                    pstmt.setInt(1, orderItem.getOrderId());
                    pstmt.setInt(2, orderItem.getBookId());
                    pstmt.setInt(3, orderItem.getQuantity());
                    pstmt.setBigDecimal(4, orderItem.getPrice());
                    pstmt.addBatch();
                    
                    // Update book quantity
                    Book book = bookService.getBookById(cartItem.getBookId());
                    if (book != null) {
                        boolean success = book.decreaseQuantity(cartItem.getQuantity());
                        if (!success) {
                            connection.rollback();
                            return null;
                        }
                        bookService.updateBook(book);
                    }
                }
                
                pstmt.executeBatch();
            }
            
            // 7. Update user order count
            user.incrementOrderCount();
            String updateUserQuery = "UPDATE users SET order_count = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(updateUserQuery)) {
                pstmt.setInt(1, user.getOrderCount());
                pstmt.setInt(2, user.getId());
                pstmt.executeUpdate();
            }
            
            // Commit transaction
            connection.commit();
            
            return order;
        } catch (Exception e) {
            // Rollback transaction on error
            connection.rollback();
            e.printStackTrace();
            return null;
        } finally {
            // Restore auto-commit
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Gets all orders for a user.
     * 
     * @param userId The user ID
     * @return A list of orders
     * @throws SQLException If a database error occurs
     */
    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        
        String query = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    loadOrderItems(order);
                    orders.add(order);
                }
            }
        }
        
        return orders;
    }
    
    /**
     * Gets all orders.
     * 
     * @return A list of all orders
     * @throws SQLException If a database error occurs
     */
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        
        String query = "SELECT * FROM orders ORDER BY order_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        }
        
        return orders;
    }
    
    /**
     * Gets an order by ID.
     * 
     * @param orderId The order ID
     * @return The order, or null if not found
     * @throws SQLException If a database error occurs
     */
    public Order getOrderById(int orderId) throws SQLException {
        String query = "SELECT * FROM orders WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    loadOrderItems(order);
                    return order;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Updates an order's status.
     * 
     * @param orderId The order ID
     * @param status The new status
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateOrderStatus(int orderId, Order.Status status) throws SQLException {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Cancels an order.
     * 
     * @param orderId The order ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean cancelOrder(int orderId) throws SQLException {
        // Begin transaction
        connection.setAutoCommit(false);
        
        try {
            // Get order
            Order order = getOrderById(orderId);
            if (order == null) {
                return false;
            }
            
            // Check if order is in a cancellable state
            if (order.getStatus() == Order.Status.SHIPPED || order.getStatus() == Order.Status.DELIVERED) {
                return false;
            }
            
            // Update status
            String updateStatusQuery = "UPDATE orders SET status = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateStatusQuery)) {
                pstmt.setString(1, Order.Status.CANCELLED.name());
                pstmt.setInt(2, orderId);
                
                if (pstmt.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }
            
            // Return books to inventory
            for (OrderItem item : order.getItems()) {
                Book book = bookService.getBookById(item.getBookId());
                if (book != null) {
                    book.increaseQuantity(item.getQuantity());
                    bookService.updateBook(book);
                }
            }
            
            // Commit transaction
            connection.commit();
            return true;
            
        } catch (Exception e) {
            // Rollback transaction on error
            connection.rollback();
            e.printStackTrace();
            return false;
        } finally {
            // Restore auto-commit
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Maps a result set row to an Order object.
     * 
     * @param rs The result set
     * @return The Order object
     * @throws SQLException If a database error occurs
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        
        Timestamp timestamp = rs.getTimestamp("order_date");
        order.setOrderDate(timestamp.toLocalDateTime());
        
        order.setStatus(Order.Status.valueOf(rs.getString("status")));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        // Note: final_amount is calculated automatically when setting total and discount
        
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        
        return order;
    }
    
    /**
     * Loads order items for an order.
     * 
     * @param order The order
     * @throws SQLException If a database error occurs
     */
    private void loadOrderItems(Order order) throws SQLException {
        String query = "SELECT oi.*, b.title, b.author " +
                      "FROM order_items oi " +
                      "JOIN books b ON oi.book_id = b.id " +
                      "WHERE oi.order_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, order.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setBookId(rs.getInt("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setBookTitle(rs.getString("title"));
                    item.setBookAuthor(rs.getString("author"));
                    
                    order.addItem(item);
                }
            }
        }
    }
}