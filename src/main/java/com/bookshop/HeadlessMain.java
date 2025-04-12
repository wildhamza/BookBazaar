package com.bookshop;

import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.services.CartService;
import com.bookshop.services.ReviewService;
import com.bookshop.services.DiscountService;
import com.bookshop.models.Book;
import com.bookshop.models.User;
import com.bookshop.models.CartItem;
import com.bookshop.models.Review;

import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;

/**
 * HeadlessMain class for environments without GUI support.
 * This class provides basic tests and functionality verification 
 * without requiring JavaFX or any GUI components.
 */
public class HeadlessMain {
    
    /**
     * Main method for headless execution.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("BookShop Headless Application");
        System.out.println("-----------------------------");
        
        try {
            runDatabaseTests();
            System.out.println("All tests completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run a series of database tests to verify functionality
     * 
     * @throws Exception if any test fails
     */
    private static void runDatabaseTests() throws Exception {
        System.out.println("Testing database connection...");
        testBookService();
        testUserService();
        testDiscountService();
    }
    
    /**
     * Test BookService functionality
     * 
     * @throws SQLException if a database error occurs
     */
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
    
    /**
     * Test UserService functionality
     * 
     * @throws SQLException if a database error occurs
     */
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
    
    /**
     * Test DiscountService functionality
     */
    private static void testDiscountService() {
        System.out.println("Testing DiscountService...");
        
        DiscountService discountService = new DiscountService();
        
        // Create test users with different order counts
        User standardUser = new User();
        standardUser.setOrderCount(2); // Not eligible for discount
        
        User regularUser = new User();
        regularUser.setOrderCount(7); // Regular member (5+ orders)
        
        User premiumUser = new User();
        premiumUser.setOrderCount(12); // Premium member (10+ orders)
        
        BigDecimal originalPrice = new BigDecimal("100.00");
        
        // Test discount calculations
        BigDecimal standardPrice = discountService.applyBestDiscount(standardUser, originalPrice);
        BigDecimal regularPrice = discountService.applyBestDiscount(regularUser, originalPrice);
        BigDecimal premiumPrice = discountService.applyBestDiscount(premiumUser, originalPrice);
        
        System.out.println("Discount test for $100.00 book:");
        System.out.println("- Standard user price (0% discount): $" + standardPrice);
        System.out.println("- Regular member price (10% discount): $" + regularPrice);
        System.out.println("- Premium member price (15% discount): $" + premiumPrice);
    }
}