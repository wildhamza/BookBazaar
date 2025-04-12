package com.bookshop;

import com.bookshop.models.Book;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Console-based application for testing the backend functionality.
 */
public class ConsoleMain {
    
    /**
     * Main method for the console application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("BookShop Console Application");
        System.out.println("----------------------------");
        
        try {
            // Test database connection
            testDatabaseConnection();
            
            // Test book service
            testBookService();
            
            // Test user service
            testUserService();
            
            System.out.println("\nAll tests completed successfully.");
            
        } catch (Exception e) {
            System.err.println("\nError occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test the database connection.
     * 
     * @throws SQLException If a database error occurs
     */
    private static void testDatabaseConnection() throws SQLException {
        System.out.println("\nTesting database connection...");
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            System.out.println("Database connection successful!");
        }
    }
    
    /**
     * Test the book service.
     * 
     * @throws SQLException If a database error occurs
     */
    private static void testBookService() throws SQLException {
        System.out.println("\nTesting BookService...");
        
        BookService bookService = new BookService();
        
        // Get all books
        List<Book> books = bookService.getAllBooks();
        System.out.println("Found " + books.size() + " books in the database.");
        
        // Display the first few books
        int count = 0;
        for (Book book : books) {
            System.out.println("Book: " + book.getTitle() + " by " + book.getAuthor() + 
                              " ($" + book.getPrice() + ")");
            count++;
            if (count >= 3) break;
        }
        
        if (books.size() > 0) {
            // Test getting a book by ID
            Book firstBook = books.get(0);
            Book retrievedBook = bookService.getBookById(firstBook.getId());
            
            if (retrievedBook != null) {
                System.out.println("Successfully retrieved book by ID: " + retrievedBook.getTitle());
            } else {
                System.out.println("Failed to retrieve book by ID.");
            }
        }
    }
    
    /**
     * Test the user service.
     * 
     * @throws SQLException If a database error occurs
     */
    private static void testUserService() throws SQLException {
        System.out.println("\nTesting UserService...");
        
        UserService userService = new UserService();
        
        // Try to get admin user
        User adminUser = userService.getUserByUsername("admin");
        
        if (adminUser != null) {
            System.out.println("Found admin user: " + adminUser.getUsername() + 
                              " (Role: " + adminUser.getRole() + ")");
        } else {
            System.out.println("Admin user not found.");
        }
        
        // Get all users
        List<User> users = userService.getAllUsers();
        System.out.println("Found " + users.size() + " users in the database.");
        
        // Display the first few users
        int count = 0;
        for (User user : users) {
            System.out.println("User: " + user.getUsername() + " (Role: " + user.getRole() + ")");
            count++;
            if (count >= 3) break;
        }
    }
}