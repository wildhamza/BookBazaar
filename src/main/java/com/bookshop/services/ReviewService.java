package com.bookshop.services;

import com.bookshop.models.Review;
// Using the fully qualified name in the getConnection method

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing book reviews.
 */
public class ReviewService {
    
    private Connection connection;
    
    /**
     * Default constructor.
     */
    public ReviewService() {
        try {
            this.connection = com.bookshop.utils.DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a new review.
     * 
     * @param review The review to add
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean addReview(Review review) throws SQLException {
        String query = "INSERT INTO reviews (user_id, book_id, rating, content, review_date) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, review.getUserId());
            stmt.setInt(2, review.getBookId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getContent());
            stmt.setTimestamp(5, Timestamp.valueOf(review.getReviewDate()));
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                // Update the book's average rating
                updateBookRating(review.getBookId());
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Adds a new review.
     * 
     * @param userId The user ID
     * @param bookId The book ID
     * @param content The review content
     * @param rating The rating (1-5)
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean addReview(int userId, int bookId, String content, int rating) throws SQLException {
        Review review = new Review();
        review.setUserId(userId);
        review.setBookId(bookId);
        review.setContent(content);
        review.setRating(rating);
        review.setReviewDate(LocalDateTime.now());
        
        return addReview(review);
    }
    
    /**
     * Checks if a user has already reviewed a book.
     * 
     * @param userId The user ID
     * @param bookId The book ID
     * @return true if the user has already reviewed the book, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean hasUserReviewedBook(int userId, int bookId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND book_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }
    
    /**
     * Gets all reviews for a book.
     * 
     * @param bookId The book ID
     * @return A list of reviews
     * @throws SQLException If a database error occurs
     */
    public List<Review> getBookReviews(int bookId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        
        String query = "SELECT r.*, u.username " +
                      "FROM reviews r " +
                      "JOIN users u ON r.user_id = u.id " +
                      "WHERE r.book_id = ? " +
                      "ORDER BY r.review_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Review review = new Review();
                review.setId(rs.getInt("id"));
                review.setUserId(rs.getInt("user_id"));
                review.setBookId(rs.getInt("book_id"));
                review.setRating(rs.getInt("rating"));
                review.setContent(rs.getString("content"));
                review.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());
                review.setUsername(rs.getString("username"));
                
                reviews.add(review);
            }
        }
        
        return reviews;
    }
    
    /**
     * Gets all reviews by a user.
     * 
     * @param userId The user ID
     * @return A list of reviews
     * @throws SQLException If a database error occurs
     */
    public List<Review> getUserReviews(int userId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        
        String query = "SELECT r.*, b.title AS book_title " +
                      "FROM reviews r " +
                      "JOIN books b ON r.book_id = b.id " +
                      "WHERE r.user_id = ? " +
                      "ORDER BY r.review_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Review review = new Review();
                review.setId(rs.getInt("id"));
                review.setUserId(rs.getInt("user_id"));
                review.setBookId(rs.getInt("book_id"));
                review.setRating(rs.getInt("rating"));
                review.setContent(rs.getString("content"));
                review.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());
                review.setBookTitle(rs.getString("book_title"));
                
                reviews.add(review);
            }
        }
        
        return reviews;
    }
    
    /**
     * Deletes a review.
     * 
     * @param reviewId The review ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deleteReview(int reviewId) throws SQLException {
        // First get the book ID to update its rating later
        int bookId = 0;
        String getBookIdQuery = "SELECT book_id FROM reviews WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(getBookIdQuery)) {
            stmt.setInt(1, reviewId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                bookId = rs.getInt("book_id");
            } else {
                return false; // Review not found
            }
        }
        
        // Now delete the review
        String query = "DELETE FROM reviews WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            
            int result = stmt.executeUpdate();
            
            if (result > 0 && bookId > 0) {
                // Update the book's average rating
                updateBookRating(bookId);
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Updates a book's average rating and review count.
     * 
     * @param bookId The book ID
     * @throws SQLException If a database error occurs
     */
    private void updateBookRating(int bookId) throws SQLException {
        // Get average rating and count
        String getStatsQuery = "SELECT AVG(rating) AS avg_rating, COUNT(*) AS review_count " +
                              "FROM reviews WHERE book_id = ?";
        
        double avgRating = 0.0;
        int reviewCount = 0;
        
        try (PreparedStatement stmt = connection.prepareStatement(getStatsQuery)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                avgRating = rs.getDouble("avg_rating");
                reviewCount = rs.getInt("review_count");
            }
        }
        
        // Update the book
        String updateBookQuery = "UPDATE books SET average_rating = ?, review_count = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(updateBookQuery)) {
            stmt.setDouble(1, avgRating);
            stmt.setInt(2, reviewCount);
            stmt.setInt(3, bookId);
            
            stmt.executeUpdate();
        }
    }
}