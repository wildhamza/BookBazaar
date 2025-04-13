package com.bookshop.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.bookshop.models.Book;
 
import java.sql.SQLException;
import java.util.List;

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
        String category = "Fiction";
        List<Book> books = bookService.getBooksByCategory(category);
        
        assertNotNull(books, "Books list should not be null");
        
        for (Book book : books) {
            assertEquals(category, book.getCategory(), "Book category should match the requested category");
        }
    }
    
    @Test
    @DisplayName("Test searching books")
    void testSearchBooks() throws SQLException {
        String searchTerm = "Harry Potter";
        List<Book> books = bookService.searchBooks(searchTerm);
        
        assertNotNull(books, "Books list should not be null");
        
    }
    
    @Test
    @DisplayName("Test stock quantity update")
    void testUpdateStockQuantity() throws SQLException {
        
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            fail("No books found to test with");
        }
        
        Book testBook = books.get(0);
        int originalQuantity = testBook.getStockQuantity();
        int quantityChange = 5;
        
        bookService.updateStockQuantity(testBook.getId(), quantityChange);
        
        Book updatedBook = bookService.getBookById(testBook.getId());
        
        assertEquals(originalQuantity + quantityChange, updatedBook.getStockQuantity(),
                    "Stock quantity should be updated correctly");
        
        bookService.updateStockQuantity(testBook.getId(), -quantityChange);
    }
}