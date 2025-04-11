package com.bookshop.services;

import com.bookshop.db.DatabaseConnection;
import com.bookshop.models.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for book review operations.
 */
public class ReviewService {
    
    private Connection conn;
    
    public ReviewService() {
        conn = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Gets all reviews for a book.
     * 
     * @param bookId The book ID
     * @return A list of reviews for the book
     * @throws SQLException If a database error occurs
     */
    public List<Review> getReviewsByBookId(int bookId) throws SQLException {
        String query = "SELECT r.*, u.username FROM reviews r " +
                       "JOIN users u ON r.user_id = u.id " +
                       "WHERE r.book_id = ? " +
                       "ORDER BY r.created_at DESC";
        
        List<Review> reviews = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review();
                    review.setId(rs.getInt("id"));
                    review.setBookId(rs.getInt("book_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setUsername(rs.getString("username"));
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));
                    review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    
                    reviews.add(review);
                }
            }
        }
        
        return reviews;
    }
    
    /**
     * Adds a new review.
     * 
     * @param review The review to add
     * @return The review with its new ID
     * @throws SQLException If a database error occurs
     */
    public Review addReview(Review review) throws SQLException {
        // Check if user already reviewed this book
        if (hasUserReviewedBook(review.getUserId(), review.getBookId())) {
            throw new IllegalArgumentException("You have already reviewed this book");
        }
        
        String query = "INSERT INTO reviews (book_id, user_id, rating, comment, created_at) " +
                       "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, review.getBookId());
            stmt.setInt(2, review.getUserId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComment());
            
            // Set created_at to current time
            LocalDateTime now = LocalDateTime.now();
            review.setCreatedAt(now);
            stmt.setTimestamp(5, Timestamp.valueOf(now));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating review failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    review.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating review failed, no ID obtained.");
                }
            }
        }
        
        // Update book rating in cache
        updateBookRating(review.getBookId());
        
        return review;
    }
    
    /**
     * Checks if a user has already reviewed a book.
     * 
     * @param userId The user ID
     * @param bookId The book ID
     * @return true if the user has already reviewed the book, false otherwise
     * @throws SQLException If a database error occurs
     */
    private boolean hasUserReviewedBook(int userId, int bookId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND book_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Updates the average rating for a book.
     * 
     * @param bookId The book ID
     * @throws SQLException If a database error occurs
     */
    private void updateBookRating(int bookId) throws SQLException {
        // This would typically update a cached average rating in the books table
        // For this implementation, we'll just calculate it on-the-fly in the BookService
    }
}
