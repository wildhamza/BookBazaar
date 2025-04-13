package com.bookshop.services;

import com.bookshop.models.*;
import com.bookshop.utils.DatabaseConnection;

import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseService {
    
    private BookService bookService;
    
    public PurchaseService() {
        try {
            this.bookService = new BookService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Order processPurchase(ObservableList<CartItem> cartItems, User user, PaymentStrategy paymentStrategy) throws SQLException {
        if (cartItems == null || cartItems.isEmpty() || user == null) {
            return null;
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnection.getInstance().getConnection();
            
            Order order = new Order();
            order.setUserId(user.getId());
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(Order.Status.PENDING);
            order.setShippingAddress(user.getAddress());
            order.setPaymentMethod("Standard");
            
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                total = total.add(item.getSubtotal());
            }
            order.setTotalAmount(total);
            
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
    
    public boolean updateOrderStatus(int orderId, Order.Status status) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            
            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    public boolean cancelOrder(int orderId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                Order order = getOrderById(orderId);
                if (order == null || order.getStatus() == Order.Status.CANCELLED) {
                    connection.rollback();
                    return false;
                }
                
                String updateOrderQuery = "UPDATE orders SET status = ? WHERE id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(updateOrderQuery)) {
                    pstmt.setString(1, Order.Status.CANCELLED.name());
                    pstmt.setInt(2, orderId);
                    pstmt.executeUpdate();
                }
                
                for (OrderItem item : order.getItems()) {
                    Book book = bookService.getBookById(item.getBookId());
                    if (book != null) {
                        book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
                        bookService.updateBook(book);
                    }
                }
                
                connection.commit();
                return true;
            } catch (Exception e) {
                connection.rollback();
                e.printStackTrace();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
    
    public boolean deleteOrder(int orderId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                    pstmt.setInt(1, orderId);
                    pstmt.executeUpdate();
                }
                
                try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM orders WHERE id = ?")) {
                    pstmt.setInt(1, orderId);
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected == 0) {
                        connection.rollback();
                        return false;
                    }
                }
                
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
    
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
    
    public void checkDatabaseTables() {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement()) {
            
            System.out.println("--- CHECKING ORDERS TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders")) {
                if (rs.next()) {
                    System.out.println("Total orders in database: " + rs.getInt(1));
                }
            }
            
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM orders LIMIT 5")) {
                while (rs.next()) {
                    System.out.println("Order #" + rs.getInt("id") + 
                                     ", User: " + rs.getInt("user_id") + 
                                     ", Status: " + rs.getString("status"));
                }
            }
            
            System.out.println("--- CHECKING ORDER_ITEMS TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM order_items")) {
                if (rs.next()) {
                    System.out.println("Total order items in database: " + rs.getInt(1));
                }
            }
            
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
    
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        
        String statusStr = rs.getString("status");
        try {
            order.setStatus(Order.Status.valueOf(statusStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid status in database: " + statusStr);
            order.setStatus(Order.Status.PENDING);
        }
        
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        
        try {
            BigDecimal discountAmount = rs.getBigDecimal("discount_amount");
            if (discountAmount != null) {
                order.setDiscountAmount(discountAmount);
            } else {
                order.setDiscountAmount(BigDecimal.ZERO);
            }
        } catch (SQLException e) {
            order.setDiscountAmount(BigDecimal.ZERO);
        }
        
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        return order;
    }
}