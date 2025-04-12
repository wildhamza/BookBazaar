package com.bookshop.repositories;

import com.bookshop.models.Book;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Book entities.
 */
public interface BookRepository extends Repository<Book, Integer> {
    
    /**
     * Find books by category.
     * 
     * @param category The category to filter by
     * @return List of books in the specified category
     * @throws SQLException If a database error occurs
     */
    List<Book> findByCategory(String category) throws SQLException;
    
    /**
     * Search books by title, author, or description.
     * 
     * @param query The search query
     * @return List of books matching the search criteria
     * @throws SQLException If a database error occurs
     */
    List<Book> search(String query) throws SQLException;
    
    /**
     * Update the stock quantity of a book.
     * 
     * @param bookId The book ID
     * @param newQuantity The new stock quantity
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean updateStockQuantity(int bookId, int newQuantity) throws SQLException;
    
    /**
     * Update the stock quantity by adding (or subtracting) the specified amount.
     * 
     * @param bookId The book ID
     * @param quantityChange The amount to add (positive) or subtract (negative)
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean updateStockQuantityByDelta(int bookId, int quantityChange) throws SQLException;
} 