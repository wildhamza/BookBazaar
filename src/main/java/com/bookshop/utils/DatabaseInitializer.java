package com.bookshop.utils;

import com.bookshop.models.Order;
import com.bookshop.models.CartItem;
import com.bookshop.models.User;
import com.bookshop.models.Book;
import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.services.OrderService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
    
    public static void main(String[] args) {
        System.out.println("Database Initializer");
        System.out.println("-------------------");
        
        try {
            createTestOrders();
            System.out.println("Test orders created successfully.");
        } catch (Exception e) {
            System.err.println("Error creating test orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void createTestOrders() throws SQLException {
        OrderService orderService = new OrderService();
        List<Order> existingOrders = orderService.getAllOrders();
        
        if (!existingOrders.isEmpty()) {
            System.out.println("Database already has " + existingOrders.size() + " orders. No need to create test orders.");
            
            boolean hasCompletedOrders = existingOrders.stream()
                .anyMatch(order -> order.getStatus() == Order.Status.DELIVERED);
                
            if (!hasCompletedOrders) {
                System.out.println("No completed orders found. Marking some orders as delivered for testing.");
                
                int count = 0;
                for (Order order : existingOrders) {
                    if (order.getStatus() != Order.Status.DELIVERED && count < 2) {
                        orderService.updateOrderStatus(order.getId(), "DELIVERED");
                        System.out.println("Marked order #" + order.getId() + " as DELIVERED");
                        count++;
                    }
                }
            }
            
            return;
        }
        
        UserService userService = new UserService();
        BookService bookService = new BookService();
        
        List<User> users = userService.getAllUsers();
        List<Book> books = bookService.getAllBooks();
        
        if (users.isEmpty()) {
            System.out.println("No users found in the database. Cannot create test orders.");
            return;
        }
        
        if (books.isEmpty()) {
            System.out.println("No books found in the database. Cannot create test orders.");
            return;
        }
        
        System.out.println("Creating test orders...");
        
        User customer = null;
        for (User user : users) {
            if (!"admin".equalsIgnoreCase(user.getRole())) {
                customer = user;
                break;
            }
        }
        
        if (customer == null) {
            customer = users.get(0);
        }
        
        System.out.println("Using user: " + customer.getUsername() + " (ID: " + customer.getId() + ")");
        
        String[] statuses = {"PENDING", "PROCESSING", "DELIVERED"};
        
        for (int i = 0; i < statuses.length; i++) {
            List<CartItem> cartItems = new ArrayList<>();
            
            int numItems = 1 + (int)(Math.random() * 3);
            
            for (int j = 0; j < numItems && j < books.size(); j++) {
                Book book = books.get(j % books.size());
                
                CartItem item = new CartItem();
                item.setBookId(book.getId());
                item.setQuantity(1 + (int)(Math.random() * 3));
                
                cartItems.add(item);
                System.out.println("Added book: " + book.getTitle() + " (ID: " + book.getId() + "), Quantity: " + item.getQuantity());
            }
            
            try {
                int orderId = orderService.createOrder(customer.getId(), cartItems, "Credit Card");
                
                if (orderId > 0) {
                    if (!"PENDING".equals(statuses[i])) {
                        orderService.updateOrderStatus(orderId, statuses[i]);
                    }
                    
                    System.out.println("Created order #" + orderId + " with status " + statuses[i]);
                } else {
                    System.out.println("Failed to create order with status " + statuses[i]);
                }
            } catch (Exception e) {
                System.err.println("Error creating order with status " + statuses[i] + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public static boolean ordersTableExists() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'orders'");
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error checking if orders table exists: " + e.getMessage());
        }
        return false;
    }
} 