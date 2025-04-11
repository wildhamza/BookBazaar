package com.bookshop.services;

import com.bookshop.db.DatabaseConnection;
import com.bookshop.models.Book;
import com.bookshop.utils.BookFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for book-related operations.
 */
public class BookService {
    
    private Connection conn;
    
    public BookService() {
        conn = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Gets all books from the database.
     * 
     * @return A list of all books
     * @throws SQLException If a database error occurs
     */
    public List<Book> getAllBooks() throws SQLException {
        String query = "SELECT b.*, " +
                       "COALESCE(AVG(r.rating), 0) as average_rating, " +
                       "COUNT(r.id) as review_count " +
                       "FROM books b " +
                       "LEFT JOIN reviews r ON b.id = r.book_id " +
                       "GROUP BY b.id " +
                       "ORDER BY b.title";
        
        List<Book> books = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Book book = BookFactory.createFromResultSet(rs);
                books.add(book);
            }
        }
        
        return books;
    }
    
    /**
     * Gets a book by its ID.
     * 
     * @param id The book ID
     * @return The book with the specified ID
     * @throws SQLException If a database error occurs
     */
    public Book getBookById(int id) throws SQLException {
        String query = "SELECT b.*, " +
                       "COALESCE(AVG(r.rating), 0) as average_rating, " +
                       "COUNT(r.id) as review_count " +
                       "FROM books b " +
                       "LEFT JOIN reviews r ON b.id = r.book_id " +
                       "WHERE b.id = ? " +
                       "GROUP BY b.id";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return BookFactory.createFromResultSet(rs);
                }
            }
        }
        
        return null; // Book not found
    }
    
    /**
     * Searches for books based on criteria.
     * 
     * @param searchTerm The search term for title or author
     * @param category The category to filter by
     * @param sortField The field to sort by
     * @param ascending Whether to sort in ascending order
     * @return A list of books matching the criteria
     * @throws SQLException If a database error occurs
     */
    public List<Book> searchBooks(String searchTerm, String category, 
                                  String sortField, boolean ascending) throws SQLException {
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT b.*, ");
        queryBuilder.append("COALESCE(AVG(r.rating), 0) as average_rating, ");
        queryBuilder.append("COUNT(r.id) as review_count ");
        queryBuilder.append("FROM books b ");
        queryBuilder.append("LEFT JOIN reviews r ON b.id = r.book_id ");
        
        // Add WHERE clause if needed
        List<String> whereConditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            whereConditions.add("(LOWER(b.title) LIKE LOWER(?) OR LOWER(b.author) LIKE LOWER(?))");
            parameters.add("%" + searchTerm + "%");
            parameters.add("%" + searchTerm + "%");
        }
        
        if (category != null && !category.isEmpty()) {
            whereConditions.add("LOWER(b.category) = LOWER(?)");
            parameters.add(category);
        }
        
        if (!whereConditions.isEmpty()) {
            queryBuilder.append("WHERE ");
            for (int i = 0; i < whereConditions.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(" AND ");
                }
                queryBuilder.append(whereConditions.get(i));
            }
        }
        
        // Add GROUP BY clause
        queryBuilder.append(" GROUP BY b.id ");
        
        // Add ORDER BY clause
        queryBuilder.append("ORDER BY ");
        
        // Map sortField to actual column name
        String orderByColumn;
        switch (sortField) {
            case "title":
                orderByColumn = "b.title";
                break;
            case "author":
                orderByColumn = "b.author";
                break;
            case "publisher":
                orderByColumn = "b.publisher";
                break;
            case "price":
                orderByColumn = "b.price";
                break;
            case "averageRating":
                orderByColumn = "average_rating";
                break;
            default:
                orderByColumn = "b.title";
        }
        
        queryBuilder.append(orderByColumn);
        queryBuilder.append(ascending ? " ASC" : " DESC");
        
        List<Book> books = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = BookFactory.createFromResultSet(rs);
                    books.add(book);
                }
            }
        }
        
        return books;
    }
    
    /**
     * Adds a new book to the database.
     * 
     * @param book The book to add
     * @return The book with its new ID
     * @throws SQLException If a database error occurs
     */
    public Book addBook(Book book) throws SQLException {
        String query = "INSERT INTO books (title, author, publisher, price, category, " +
                       "isbn, image_url, description, stock_quantity) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setBigDecimal(4, book.getPrice());
            stmt.setString(5, book.getCategory());
            stmt.setString(6, book.getIsbn());
            stmt.setString(7, book.getImageUrl());
            stmt.setString(8, book.getDescription());
            stmt.setInt(9, book.getStockQuantity());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating book failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating book failed, no ID obtained.");
                }
            }
        }
        
        return book;
    }
    
    /**
     * Updates an existing book in the database.
     * 
     * @param book The book to update
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateBook(Book book) throws SQLException {
        String query = "UPDATE books SET title = ?, author = ?, publisher = ?, " +
                       "price = ?, category = ?, isbn = ?, image_url = ?, " +
                       "description = ?, stock_quantity = ? " +
                       "WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setBigDecimal(4, book.getPrice());
            stmt.setString(5, book.getCategory());
            stmt.setString(6, book.getIsbn());
            stmt.setString(7, book.getImageUrl());
            stmt.setString(8, book.getDescription());
            stmt.setInt(9, book.getStockQuantity());
            stmt.setInt(10, book.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Deletes a book from the database.
     * 
     * @param bookId The ID of the book to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deleteBook(int bookId) throws SQLException {
        String query = "DELETE FROM books WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Updates the stock quantity of a book.
     * 
     * @param bookId The ID of the book
     * @param quantity The quantity to add (positive) or subtract (negative)
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateStockQuantity(int bookId, int quantity) throws SQLException {
        String query = "UPDATE books SET stock_quantity = stock_quantity + ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, bookId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}
