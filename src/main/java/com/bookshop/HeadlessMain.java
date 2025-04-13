package com.bookshop;

import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.services.CartService;
import com.bookshop.services.DiscountService;
import com.bookshop.models.Book;
import com.bookshop.models.User;
import com.bookshop.models.CartItem;
import com.bookshop.models.Order;
import com.bookshop.models.OrderItem;
import com.bookshop.services.OrderService;
import com.bookshop.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

public class HeadlessMain {
        public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a command. Available commands:");
            System.out.println("- check_db: Check database connection");
            System.out.println("- check_users: Check if users exist");
            System.out.println("- check_books: Check if books exist");
            System.out.println("- check_orders: Check if orders exist and create a test order if none exist");
            System.exit(0);
        }
        
        String command = args[0];
        
        try {
            switch (command) {
                case "check_db":
                    checkDatabaseConnection();
                    break;
                case "check_users":
                    checkUsers();
                    break;
                case "check_books":
                    checkBooks();
                    break;
                case "check_orders":
                    checkOrders();
                    break;
                default:
                    System.out.println("Unknown command: " + command);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error running command: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runDatabaseTests() throws Exception {
        System.out.println("Testing database connection...");
        testBookService();
        testUserService();
        testDiscountService();
        testCartService();
        checkOrdersTable();
    }
    
    private static void testBookService() throws SQLException {
        System.out.println("Testing BookService...");
        
        BookService bookService = new BookService();
        List<Book> books = bookService.getAllBooks();
        System.out.println("Found " + books.size() + " books in the database.");
        
        if (!books.isEmpty()) {
            for (int i = 0; i < Math.min(3, books.size()); i++) {
                Book book = books.get(i);
                System.out.println("Book: " + book.getTitle() + " by " + book.getAuthor() + " ($" + book.getPrice() + ")");
            }
            
            Book firstBook = books.get(0);
            Book bookById = bookService.getBookById(firstBook.getId());
            if (bookById != null) {
                System.out.println("Successfully retrieved book by ID: " + bookById.getTitle());
            }
        }
    }
    
    private static void testUserService() throws SQLException {
        System.out.println("Testing UserService...");
        
        UserService userService = new UserService();
        User adminUser = userService.authenticateUser("admin", "admin123");
        
        if (adminUser != null) {
            System.out.println("Found admin user: " + adminUser.getUsername() + " (Role: " + adminUser.getRole() + ")");
        } else {
            System.out.println("Admin user not found. Database may not be properly initialized.");
        }
        
        List<User> users = userService.getAllUsers();
        System.out.println("Found " + users.size() + " users in the database.");
        
        if (!users.isEmpty()) {
            for (int i = 0; i < Math.min(3, users.size()); i++) {
                User user = users.get(i);
                System.out.println("User: " + user.getUsername() + " (Role: " + user.getRole() + ")");
            }
        }
    }
    
    private static void testCartService() throws SQLException {
        System.out.println("Testing CartService...");
        
        CartService cartService = CartService.getInstance();
        
        int adminUserId = 1;
        
        int count = cartService.getCartItemCount(adminUserId);
        System.out.println("Admin user has " + count + " items in cart");
    }
    
    private static void testDiscountService() {
        System.out.println("Testing DiscountService...");
        
        DiscountService discountService = new DiscountService();
        
        User standardUser = new User();
        standardUser.setOrderCount(2); 
        
        User regularUser = new User();
        regularUser.setOrderCount(7); 
        
        User premiumUser = new User();
        premiumUser.setOrderCount(12); 
        
        BigDecimal originalPrice = new BigDecimal("100.00");
        
        BigDecimal standardPrice = discountService.calculateDiscountedPrice(originalPrice, standardUser);
        BigDecimal regularPrice = discountService.calculateDiscountedPrice(originalPrice, regularUser);
        BigDecimal premiumPrice = discountService.calculateDiscountedPrice(originalPrice, premiumUser);
        
        System.out.println("Discount test for $100.00 book:");
        System.out.println("- Standard user price (0% discount): $" + standardPrice);
        System.out.println("- Regular member price (10% discount): $" + regularPrice);
        System.out.println("- Premium member price (15% discount): $" + premiumPrice);
    }
    
    private static void checkOrdersTable() throws SQLException {
        System.out.println("Checking orders table structure...");
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "orders", null);
            
            System.out.println("Columns in orders table:");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                System.out.println("  - " + columnName + " (" + dataType + "(" + columnSize + "))");
            }
            
            ResultSet discountColumn = metaData.getColumns(null, null, "orders", "discount_amount");
            if (!discountColumn.next()) {
                System.out.println("Warning: discount_amount column is missing in the orders table!");
            }
        }
    }
    
    private static void checkDatabaseConnection() throws SQLException {
        System.out.println("Checking database connection...");
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null && !conn.isClosed()) {
            System.out.println("Database connection successful!");
            System.out.println("Connection valid: " + conn.isValid(5));
            System.out.println("Auto-commit: " + conn.getAutoCommit());
            conn.close();
        } else {
            System.out.println("Database connection failed!");
        }
    }
    
    private static void checkUsers() throws SQLException {
        System.out.println("Checking users...");
        
        UserService userService = new UserService();
        List<User> users = userService.getAllUsers();
        
        System.out.println("Found " + users.size() + " users in the database");
        
        if (!users.isEmpty()) {
            System.out.println("Sample users:");
            for (int i = 0; i < Math.min(5, users.size()); i++) {
                User user = users.get(i);
                System.out.println("- ID: " + user.getId() + ", Username: " + user.getUsername() + ", Role: " + user.getRole());
            }
        }
    }
    
    private static void checkBooks() throws SQLException {
        System.out.println("Checking books...");
        
        BookService bookService = new BookService();
        List<Book> books = bookService.getAllBooks();
        
        System.out.println("Found " + books.size() + " books in the database");
        
        if (!books.isEmpty()) {
            System.out.println("Sample books:");
            for (int i = 0; i < Math.min(5, books.size()); i++) {
                Book book = books.get(i);
                System.out.println("- ID: " + book.getId() + ", Title: " + book.getTitle() + ", Author: " + book.getAuthor() + ", Price: $" + book.getPrice());
            }
        }
    }
    
    private static void checkOrders() throws SQLException {
        System.out.println("Checking orders...");
        
        OrderService orderService = new OrderService();
        List<Order> orders = orderService.getAllOrders();
        
        System.out.println("Found " + orders.size() + " orders in the database");
        
        if (!orders.isEmpty()) {
            System.out.println("Sample orders:");
            for (int i = 0; i < Math.min(5, orders.size()); i++) {
                Order order = orders.get(i);
                System.out.println("- ID: " + order.getId() + ", User ID: " + order.getUserId() + ", Status: " + order.getStatus() + ", Total: $" + order.getTotalAmount());
                List<OrderItem> items = orderService.getOrderItems(order.getId());
                System.out.println("  Items: " + items.size());
                for (OrderItem item : items) {
                    System.out.println("  - Book ID: " + item.getBookId() + ", Quantity: " + item.getQuantity() + ", Price: $" + item.getPrice());
                }
            }
        } else {
            System.out.println("No orders found. Checking order table structure...");
            
            boolean tableExists = false;
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'orders'");
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next() && rs.getInt(1) > 0) {
                    tableExists = true;
                }
            }
            
            if (!tableExists) {
                System.out.println("Orders table does not exist! The database schema might not be properly initialized.");
                return;
            }
            
            System.out.println("Creating a test order...");
            
            UserService userService = new UserService();
            BookService bookService = new BookService();
            
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users found. Cannot create test order.");
                return;
            }
            
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                System.out.println("No books found. Cannot create test order.");
                return;
            }
            
            User user = users.get(0);
            Book book = books.get(0);
            
            System.out.println("Using User: " + user.getUsername() + " (ID: " + user.getId() + ")");
            System.out.println("Using Book: " + book.getTitle() + " (ID: " + book.getId() + ")");
            
            List<CartItem> cartItems = new ArrayList<>();
            CartItem cartItem = new CartItem();
            cartItem.setBookId(book.getId());
            cartItem.setQuantity(1);
            cartItems.add(cartItem);
            
            try {
                int orderId = orderService.createOrder(user.getId(), cartItems, "Credit Card");
                if (orderId > 0) {
                    System.out.println("Test order created successfully! Order ID: " + orderId);
                    
                    Order createdOrder = orderService.getOrderById(orderId);
                    if (createdOrder != null) {
                        System.out.println("Verified order: ID: " + createdOrder.getId() + ", Status: " + createdOrder.getStatus() + ", Total: $" + createdOrder.getTotalAmount());
                    } else {
                        System.out.println("Could not verify order creation. Something went wrong.");
                    }
                } else {
                    System.out.println("Failed to create test order.");
                }
            } catch (Exception e) {
                System.out.println("Error creating test order: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}