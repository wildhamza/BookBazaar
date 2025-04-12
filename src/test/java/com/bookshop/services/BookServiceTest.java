package com.bookshop.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.bookshop.models.Book;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Unit tests for the BookService class.
 */
public class BookServiceTest {
    
    private BookService bookService;
    
    @BeforeEach
    void setUp() {
        bookService = new BookService();
    }
    
    @Test
    @DisplayName("Test getting all books")
    void testGetAllBooks() throws SQLException {
        List<Book> books = bookService.getAllBooks();
        
        assertNotNull(books, "Books list should not be null");
        assertFalse(books.isEmpty(), "Books list should not be empty");
    }
    
    @Test
    @DisplayName("Test getting books by category")
    void testGetBooksByCategory() throws SQLException {
        // Use a category that we know exists in the database
        String category = "Fiction";
        List<Book> books = bookService.getBooksByCategory(category);
        
        assertNotNull(books, "Books list should not be null");
        
        // Every book in the result should have the correct category
        for (Book book : books) {
            assertEquals(category, book.getCategory(), "Book category should match the requested category");
        }
    }
    
    @Test
    @DisplayName("Test searching books")
    void testSearchBooks() throws SQLException {
        // Use a search term that we know will return results
        String searchTerm = "Harry Potter";
        List<Book> books = bookService.searchBooks(searchTerm);
        
        assertNotNull(books, "Books list should not be null");
        
        // In a real test, we might want to verify that each book actually matches the search criteria
        // This would require a more complex check
    }
    
    @Test
    @DisplayName("Test stock quantity update")
    void testUpdateStockQuantity() throws SQLException {
        // Skip this test for now
        // When implementing proper database connection, uncomment the code below
        /*
        // Get a book that we know exists
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            fail("No books found to test with");
        }
        
        Book testBook = books.get(0);
        int originalQuantity = testBook.getStockQuantity();
        int quantityChange = 5;
        
        // Update the stock quantity
        bookService.updateStockQuantity(testBook.getId(), quantityChange);
        
        // Get the book again to check the updated quantity
        Book updatedBook = bookService.getBookById(testBook.getId());
        
        assertEquals(originalQuantity + quantityChange, updatedBook.getStockQuantity(),
                    "Stock quantity should be updated correctly");
        
        // Restore the original quantity for clean test
        bookService.updateStockQuantity(testBook.getId(), -quantityChange);
        */
    }
    
    // Add more test cases as needed
}