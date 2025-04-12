package com.bookshop.repositories;

import com.bookshop.models.Book;
import com.bookshop.utils.BookFactory;
import com.bookshop.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the BookRepository interface.
 */
public class BookRepositoryImpl implements BookRepository {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        
        String sql = "SELECT * FROM books ORDER BY title";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                books.add(BookFactory.createFromResultSet(rs));
            }
        }
        
        return books;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Book findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return BookFactory.createFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer save(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, publisher, price, category, description, image_url, stock_quantity) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getPublisher());
            pstmt.setBigDecimal(5, book.getPrice());
            pstmt.setString(6, book.getCategory());
            pstmt.setString(7, book.getDescription());
            pstmt.setString(8, book.getImageUrl());
            pstmt.setInt(9, book.getStockQuantity());
            
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
     * {@inheritDoc}
     */
    @Override
    public boolean update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, publisher = ?, " +
                     "price = ?, category = ?, description = ?, image_url = ?, stock_quantity = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getPublisher());
            pstmt.setBigDecimal(5, book.getPrice());
            pstmt.setString(6, book.getCategory());
            pstmt.setString(7, book.getDescription());
            pstmt.setString(8, book.getImageUrl());
            pstmt.setInt(9, book.getStockQuantity());
            pstmt.setInt(10, book.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Book> findByCategory(String category) throws SQLException {
        List<Book> books = new ArrayList<>();
        
        String sql = "SELECT * FROM books WHERE category = ? ORDER BY title";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(BookFactory.createFromResultSet(rs));
                }
            }
        }
        
        return books;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Book> search(String query) throws SQLException {
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
                    books.add(BookFactory.createFromResultSet(rs));
                }
            }
        }
        
        return books;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateStockQuantity(int bookId, int newQuantity) throws SQLException {
        String sql = "UPDATE books SET stock_quantity = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, bookId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateStockQuantityByDelta(int bookId, int quantityChange) throws SQLException {
        // First get the current stock quantity
        Book book = findById(bookId);
        if (book == null) {
            return false;
        }
        
        int currentQuantity = book.getStockQuantity();
        int newQuantity = currentQuantity + quantityChange;
        
        // Ensure quantity doesn't go below zero
        if (newQuantity < 0) {
            newQuantity = 0;
        }
        
        // Update with the new quantity
        return updateStockQuantity(bookId, newQuantity);
    }
} 