package com.bookshop.services;

import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.models.CartItem;
import com.bookshop.models.Book;
import com.bookshop.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; 
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class OrderService {
    
    private BookService bookService;
    private DiscountService discountService;
    
    public OrderService() {
        this.bookService = new BookService();
        this.discountService = new DiscountService();
    }
    
    public int createOrder(int userId, List<CartItem> cartItems, String paymentMethod) throws SQLException {
        if (cartItems == null || cartItems.isEmpty()) {
            return -1;
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int orderId = -1;
        
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                Book book = bookService.getBookById(item.getBookId());
                if (book != null) {
                    BigDecimal itemPrice = book.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    totalAmount = totalAmount.add(itemPrice);
                }
            }
            
            String sql = "INSERT INTO orders (user_id, order_date, status, total_amount, payment_method) " +
                         "VALUES (?, ?, ?, ?, ?) RETURNING id";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3, "pending");
            pstmt.setBigDecimal(4, totalAmount);
            pstmt.setString(5, paymentMethod);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                orderId = rs.getInt(1);
                
                for (CartItem item : cartItems) {
                    Book book = bookService.getBookById(item.getBookId());
                    if (book != null) {
                        String itemSql = "INSERT INTO order_items (order_id, book_id, quantity, price) " +
                                         "VALUES (?, ?, ?, ?)";
                        
                        try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                            itemStmt.setInt(1, orderId);
                            itemStmt.setInt(2, item.getBookId());
                            itemStmt.setInt(3, item.getQuantity());
                            itemStmt.setBigDecimal(4, book.getPrice());
                            itemStmt.executeUpdate();
                        }
                        
                        book.reduceStock(item.getQuantity());
                        bookService.updateBook(book);
                    }
                }
                
                String updateUserSql = "UPDATE users SET order_count = order_count + 1 WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateUserSql)) {
                    updateStmt.setInt(1, userId);
                    updateStmt.executeUpdate();
                }
                
                conn.commit();
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            throw e;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return orderId;
    }
    
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        
        System.out.println("OrderService: getAllOrders called");
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("OrderService: Query executed");
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("OrderService: Processing order #" + count);
                
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                
                try {
                    Timestamp timestamp = rs.getTimestamp("order_date");
                    if (timestamp != null) {
                        order.setOrderDate(timestamp.toLocalDateTime());
                    } else {
                        System.out.println("OrderService: order_date is null for order ID " + order.getId());
                        order.setOrderDate(LocalDateTime.now());
                    }
                } catch (Exception e) {
                    System.err.println("OrderService: Error parsing order_date: " + e.getMessage());
                    order.setOrderDate(LocalDateTime.now());
                }
                
                String status = rs.getString("status");
                System.out.println("OrderService: Order status is: " + status);
                order.setStatus(status);
                
                try {
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                } catch (Exception e) {
                    System.err.println("OrderService: Error getting total_amount: " + e.getMessage());
                    order.setTotalAmount(BigDecimal.ZERO);
                }
                
                order.setPaymentMethod(rs.getString("payment_method"));
                
                List<OrderItem> items = getOrderItems(order.getId());
                System.out.println("OrderService: Order ID " + order.getId() + " has " + items.size() + " items");
                order.setItems(items);
                
                orders.add(order);
            }
            
            System.out.println("OrderService: Found " + orders.size() + " orders in total");
        } catch (SQLException e) {
            System.err.println("OrderService: SQLException in getAllOrders: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("OrderService: Unexpected exception in getAllOrders: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error retrieving orders: " + e.getMessage(), e);
        }
        
        return orders;
    }
    
    public List<Order> getOrdersByUser(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    
                    orders.add(order);
                }
            }
        }
        
        return orders;
    }
    
    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    
                    order.setItems(getOrderItems(order.getId()));
                    
                    return order;
                }
            }
        }
        
        return null;
    }
    
    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setBookId(rs.getInt("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));
                    
                    items.add(item);
                }
            }
        }
        
        return items;
    }
    
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}