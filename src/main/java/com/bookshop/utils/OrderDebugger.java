package com.bookshop.utils;

import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.services.OrderService;
import com.bookshop.services.PurchaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;

/**
 * Utility class for debugging order-related issues
 */
public class OrderDebugger {
    
    public static void main(String[] args) {
        System.out.println("\n==== DIRECT DATABASE DEBUG ====");
        debugDatabaseOrders();
        
        System.out.println("\n==== SERVICE LAYER DEBUG ====");
        debugOrderService();
        
        System.out.println("\n==== CHECKING ORDER TABLE STRUCTURE ====");
        checkOrderTableStructure();
        
        System.out.println("\n==== CREATE TEST ORDER ====");
        createTestOrder();
    }
    
    private static void debugDatabaseOrders() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Test connection
            System.out.println("Database connection established: " + (conn != null ? "YES" : "NO"));
            
            if (conn != null) {
                System.out.println("Connection autocommit: " + conn.getAutoCommit());
                System.out.println("Connection valid: " + conn.isValid(5));
                
                String sql = "SELECT COUNT(*) FROM orders";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Total orders in database: " + count);
                        
                        if (count == 0) {
                            System.out.println("No orders found in the database!");
                        } else {
                            // Print some sample orders
                            String orderSql = "SELECT * FROM orders ORDER BY id LIMIT 5";
                            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql);
                                 ResultSet orderRs = orderStmt.executeQuery()) {
                                
                                System.out.println("\nSample orders:");
                                System.out.println("ID | USER_ID | DATE | STATUS | TOTAL");
                                System.out.println("--------------------------------------------------");
                                
                                while (orderRs.next()) {
                                    System.out.printf("%d | %d | %s | %s | %.2f%n", 
                                        orderRs.getInt("id"),
                                        orderRs.getInt("user_id"),
                                        orderRs.getTimestamp("order_date"),
                                        orderRs.getString("status"),
                                        orderRs.getDouble("total_amount"));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void debugOrderService() {
        try {
            PurchaseService purchaseService = new PurchaseService();
            List<Order> allOrders = purchaseService.getAllOrders();
            
            System.out.println("Orders found via service: " + allOrders.size());
            
            if (allOrders.isEmpty()) {
                System.out.println("No orders found via service!");
            } else {
                // Print some sample orders
                System.out.println("\nSample orders from service:");
                System.out.println("ID | USER_ID | DATE | STATUS | TOTAL | ITEMS");
                System.out.println("--------------------------------------------------");
                
                int count = 0;
                for (Order order : allOrders) {
                    if (count >= 5) break;
                    
                    System.out.printf("%d | %d | %s | %s | %.2f | %d items%n", 
                        order.getId(),
                        order.getUserId(),
                        order.getOrderDate(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getItems().size());
                    
                    count++;
                }
            }
        } catch (Exception e) {
            System.out.println("Service error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkOrderTableStructure() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'orders' ORDER BY ordinal_position";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                System.out.println("\nOrder table structure:");
                System.out.println("COLUMN | DATA TYPE");
                System.out.println("------------------");
                
                boolean tableExists = false;
                
                while (rs.next()) {
                    tableExists = true;
                    System.out.printf("%s | %s%n", 
                        rs.getString("column_name"),
                        rs.getString("data_type"));
                }
                
                if (!tableExists) {
                    System.out.println("Table 'orders' does not exist in the database!");
                }
            }
        } catch (Exception e) {
            System.out.println("Database error checking structure: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createTestOrder() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Check if there are any users
            boolean hasUsers = false;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    hasUsers = rs.getInt(1) > 0;
                }
            }
            
            if (!hasUsers) {
                System.out.println("No users found in the database. Cannot create test order.");
                return;
            }
            
            // Get the first user ID
            int userId = 0;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users LIMIT 1");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("id");
                }
            }
            
            if (userId == 0) {
                System.out.println("Could not find a valid user ID.");
                return;
            }
            
            System.out.println("Using user ID: " + userId + " for test order");
            
            // Create a test order
            conn.setAutoCommit(false);
            
            String orderSql = "INSERT INTO orders (user_id, order_date, status, total_amount, payment_method) VALUES (?, ?, ?, ?, ?) RETURNING id";
            
            try (PreparedStatement stmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(3, "PENDING");
                stmt.setDouble(4, 29.99);
                stmt.setString(5, "Credit Card");
                
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        System.out.println("Created test order with ID: " + orderId);
                        
                        // Check if there are any books
                        boolean hasBooks = false;
                        try (PreparedStatement bookStmt = conn.prepareStatement("SELECT COUNT(*) FROM books");
                             ResultSet rs = bookStmt.executeQuery()) {
                            if (rs.next()) {
                                hasBooks = rs.getInt(1) > 0;
                            }
                        }
                        
                        if (hasBooks) {
                            // Get the first book ID
                            int bookId = 0;
                            try (PreparedStatement bookStmt = conn.prepareStatement("SELECT id FROM books LIMIT 1");
                                 ResultSet rs = bookStmt.executeQuery()) {
                                if (rs.next()) {
                                    bookId = rs.getInt("id");
                                }
                            }
                            
                            if (bookId > 0) {
                                // Create order item
                                String itemSql = "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
                                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                                    itemStmt.setInt(1, orderId);
                                    itemStmt.setInt(2, bookId);
                                    itemStmt.setInt(3, 1);
                                    itemStmt.setDouble(4, 29.99);
                                    
                                    int itemRows = itemStmt.executeUpdate();
                                    if (itemRows > 0) {
                                        System.out.println("Created test order item for book ID: " + bookId);
                                    }
                                }
                            }
                        }
                        
                        // Commit the transaction
                        conn.commit();
                        System.out.println("Transaction committed successfully");
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Transaction rolled back due to error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.out.println("Error creating test order: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 