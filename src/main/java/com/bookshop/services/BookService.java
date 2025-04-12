package com.bookshop.services;

import com.bookshop.db.DatabaseConnection;
import com.bookshop.models.Book;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for book-related operations.
 */
public class BookService {
    
    /**
     * Gets all books from the database.
     * 
     * @return A list of all books
     * @throws SQLException If a database access error occurs
     */
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books ORDER BY title")) {
            
            while (rs.next()) {
                Book book = mapResultSetToBook(rs);
                books.add(book);
            }
        }
        
        return books;
    }
    
    /**
     * Gets books by category from the database.
     * 
     * @param category The category to filter by
     * @return A list of books in the specified category
     * @throws SQLException If a database access error occurs
     */
    public List<Book> getBooksByCategory(String category) throws SQLException {
        List<Book> books = new ArrayList<>();
        
        String sql = "SELECT * FROM books WHERE category = ? ORDER BY title";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Book book = mapResultSetToBook(rs);
                    books.add(book);
                }
            }
        }
        
        return books;
    }
    
    /**
     * Gets a book by its ID from the database.
     * 
     * @param id The book ID
     * @return The book, or null if not found
     * @throws SQLException If a database access error occurs
     */
    public Book getBookById(int id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Adds a new book to the database.
     * 
     * @param book The book to add
     * @return The ID of the newly added book
     * @throws SQLException If a database access error occurs
     */
    public int addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, price, quantity, category, description, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setBigDecimal(4, book.getPrice());
            pstmt.setInt(5, book.getQuantity());
            pstmt.setString(6, book.getCategory());
            pstmt.setString(7, book.getDescription());
            pstmt.setString(8, book.getImageUrl());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating book failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    book.setId(id);
                    return id;
                } else {
                    throw new SQLException("Creating book failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Updates an existing book in the database.
     * 
     * @param book The book to update
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, price = ?, " +
                     "quantity = ?, category = ?, description = ?, image_url = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setBigDecimal(4, book.getPrice());
            pstmt.setInt(5, book.getQuantity());
            pstmt.setString(6, book.getCategory());
            pstmt.setString(7, book.getDescription());
            pstmt.setString(8, book.getImageUrl());
            pstmt.setInt(9, book.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Deletes a book from the database by its ID.
     * 
     * @param id The ID of the book to delete
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Updates the quantity of a book in the database.
     * 
     * @param bookId The ID of the book
     * @param newQuantity The new quantity
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateBookQuantity(int bookId, int newQuantity) throws SQLException {
        String sql = "UPDATE books SET quantity = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, bookId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Searches for books by title, author, or description.
     * 
     * @param query The search query
     * @return A list of books matching the search criteria
     * @throws SQLException If a database access error occurs
     */
    public List<Book> searchBooks(String query) throws SQLException {
        List<Book> books = new ArrayList<>();
        
        String sql = "SELECT * FROM books WHERE " +
                     "LOWER(title) LIKE ? OR " +
                     "LOWER(author) LIKE ? OR " +
                     "LOWER(description) LIKE ? " +
                     "ORDER BY title";
        
        String searchPattern = "%" + query.toLowerCase() + "%";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Book book = mapResultSetToBook(rs);
                    books.add(book);
                }
            }
        }
        
        return books;
    }
    
    /**
     * Maps a ResultSet row to a Book object.
     * 
     * @param rs The ResultSet
     * @return The Book object
     * @throws SQLException If a database access error occurs
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setPrice(rs.getBigDecimal("price"));
        book.setQuantity(rs.getInt("quantity"));
        book.setCategory(rs.getString("category"));
        book.setDescription(rs.getString("description"));
        book.setImageUrl(rs.getString("image_url"));
        
        return book;
    }
}