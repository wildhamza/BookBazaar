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
    
    private BookService bookService;
    
    /**
     * Default constructor.
     */
    public PurchaseService() {
        try {
            this.bookService = new BookService();
        } catch (Exception e) {
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
        if (cartItems == null || cartItems.isEmpty() || user == null) {
            return null;
        }
        
        Connection connection = null;
        
        try {
            // Get a fresh connection
            connection = DatabaseConnection.getInstance().getConnection();
            
            // Create order
            Order order = new Order();
            order.setUserId(user.getId());
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(Order.Status.PENDING);
            order.setShippingAddress(user.getAddress());
            order.setPaymentMethod("Standard");
            
            // Calculate total
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                total = total.add(item.getSubtotal());
            }
            order.setTotalAmount(total);
            
            // Insert order record
            String insertOrderSQL = "INSERT INTO orders (user_id, order_date, status, total_amount, shipping_address, payment_method) " +
                                  "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
            
            PreparedStatement pstmt = connection.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, user.getId());
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3, "PENDING");
            pstmt.setBigDecimal(4, total);
            pstmt.setString(5, user.getAddress());
            pstmt.setString(6, "Standard");
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
            
            // Insert order items
            String insertItemSQL = "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement itemStmt = connection.prepareStatement(insertItemSQL);
            
            for (CartItem item : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setBookId(item.getBookId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(item.getBookPrice());
                order.addItem(orderItem);
                
                itemStmt.setInt(1, order.getId());
                itemStmt.setInt(2, item.getBookId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setBigDecimal(4, item.getBookPrice());
                itemStmt.addBatch();
            }
            
            itemStmt.executeBatch();
            
            // Update user order count
            String updateUserSQL = "UPDATE users SET order_count = order_count + 1 WHERE id = ?";
            PreparedStatement userStmt = connection.prepareStatement(updateUserSQL);
            userStmt.setInt(1, user.getId());
            userStmt.executeUpdate();
            
            return order;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
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
        
        System.out.println("Getting orders for user ID: " + userId);
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                 "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC")) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    Order order = mapResultSetToOrder(rs);
                    loadOrderItems(order);
                    orders.add(order);
                    System.out.println("Loaded order #" + order.getId() + " with status " + order.getStatus());
                }
                System.out.println("Found " + count + " orders in database");
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
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM orders ORDER BY order_date DESC")) {
            
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
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM orders WHERE id = ?")) {
            
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
     * Updates the status of an order.
     * 
     * @param orderId The order ID
     * @param status The new status
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateOrderStatus(int orderId, Order.Status status) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            
            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
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
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            // Begin transaction
            connection.setAutoCommit(false);
            
            try {
                // 1. Get order
                Order order = getOrderById(orderId);
                if (order == null || order.getStatus() == Order.Status.CANCELLED) {
                    connection.rollback();
                    return false;
                }
                
                // 2. Update order status
                String updateOrderQuery = "UPDATE orders SET status = ? WHERE id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(updateOrderQuery)) {
                    pstmt.setString(1, Order.Status.CANCELLED.name());
                    pstmt.setInt(2, orderId);
                    pstmt.executeUpdate();
                }
                
                // 3. Restore book quantities
                for (OrderItem item : order.getItems()) {
                    Book book = bookService.getBookById(item.getBookId());
                    if (book != null) {
                        book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
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
    }
    
    /**
     * Deletes an order from the database.
     * 
     * @param orderId The order ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deleteOrder(int orderId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);
            
            try {
                // First, delete the order items
                try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                    pstmt.setInt(1, orderId);
                    pstmt.executeUpdate();
                }
                
                // Then, delete the order
                try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM orders WHERE id = ?")) {
                    pstmt.setInt(1, orderId);
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected == 0) {
                        // No rows affected, rollback the transaction
                        connection.rollback();
                        return false;
                    }
                }
                
                // Commit the transaction
                connection.commit();
                return true;
            } catch (SQLException e) {
                // Rollback the transaction in case of error
                connection.rollback();
                throw e;
            } finally {
                // Restore auto-commit mode
                connection.setAutoCommit(true);
            }
        }
    }
    
    /**
     * Loads order items for an order.
     * 
     * @param order The order
     * @throws SQLException If a database error occurs
     */
    private void loadOrderItems(Order order) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM order_items WHERE order_id = ?")) {
            
            pstmt.setInt(1, order.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setBookId(rs.getInt("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));
                    
                    // Get book details
                    Book book = bookService.getBookById(item.getBookId());
                    if (book != null) {
                        item.setBookTitle(book.getTitle());
                        item.setBookAuthor(book.getAuthor());
                    }
                    
                    order.addItem(item);
                }
            }
        }
    }
    
    /**
     * Debug method to check the database tables
     */
    public void checkDatabaseTables() {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement()) {
            
            // Check orders table
            System.out.println("--- CHECKING ORDERS TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders")) {
                if (rs.next()) {
                    System.out.println("Total orders in database: " + rs.getInt(1));
                }
            }
            
            // Check a few sample orders
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM orders LIMIT 5")) {
                while (rs.next()) {
                    System.out.println("Order #" + rs.getInt("id") + 
                                     ", User: " + rs.getInt("user_id") + 
                                     ", Status: " + rs.getString("status"));
                }
            }
            
            // Check order_items table
            System.out.println("--- CHECKING ORDER_ITEMS TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM order_items")) {
                if (rs.next()) {
                    System.out.println("Total order items in database: " + rs.getInt(1));
                }
            }
            
            // Check a few sample order items
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM order_items LIMIT 5")) {
                while (rs.next()) {
                    System.out.println("Item #" + rs.getInt("id") + 
                                     ", Order: " + rs.getInt("order_id") + 
                                     ", Book: " + rs.getInt("book_id") +
                                     ", Quantity: " + rs.getInt("quantity"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Maps a ResultSet row to an Order object.
     * 
     * @param rs The ResultSet
     * @return The Order object
     * @throws SQLException If a database error occurs
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        
        // Handle status case-insensitively
        String statusStr = rs.getString("status");
        try {
            order.setStatus(Order.Status.valueOf(statusStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid status in database: " + statusStr);
            // Default to PENDING if status is invalid
            order.setStatus(Order.Status.PENDING);
        }
        
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        
        // Check if discount_amount column exists in the result set
        try {
            BigDecimal discountAmount = rs.getBigDecimal("discount_amount");
            if (discountAmount != null) {
                order.setDiscountAmount(discountAmount);
            } else {
                order.setDiscountAmount(BigDecimal.ZERO);
            }
        } catch (SQLException e) {
            // discount_amount column doesn't exist
            order.setDiscountAmount(BigDecimal.ZERO);
        }
        
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        return order;
    }
}