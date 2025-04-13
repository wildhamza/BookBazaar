package com.bookshop.services;

import com.bookshop.models.Review;
import com.bookshop.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewService {
    
    public ReviewService() {
    }
    
    public boolean addReview(Review review) throws SQLException {
        String query = "INSERT INTO reviews (user_id, book_id, rating, comment, review_date) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, review.getUserId());
            stmt.setInt(2, review.getBookId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getContent());
            stmt.setTimestamp(5, Timestamp.valueOf(review.getReviewDate()));
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                updateBookRating(review.getBookId());
                return true;
            }
            
            return false;
        }
    }
    
    public boolean addReview(int userId, int bookId, String content, int rating) throws SQLException {
        Review review = new Review();
        review.setUserId(userId);
        review.setBookId(bookId);
        review.setContent(content);
        review.setRating(rating);
        review.setReviewDate(LocalDateTime.now());
        
        return addReview(review);
    }
    
    public boolean hasUserReviewedBook(int userId, int bookId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND book_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }
    
    public List<Review> getBookReviews(int bookId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        
        String query = "SELECT r.*, u.username " +
                      "FROM reviews r " +
                      "JOIN users u ON r.user_id = u.id " +
                      "WHERE r.book_id = ? " +
                      "ORDER BY r.review_date DESC";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Review review = new Review();
                review.setId(rs.getInt("id"));
                review.setUserId(rs.getInt("user_id"));
                review.setBookId(rs.getInt("book_id"));
                review.setRating(rs.getInt("rating"));
                review.setContent(rs.getString("comment"));
                review.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());
                review.setUsername(rs.getString("username"));
                
                reviews.add(review);
            }
        }
        
        return reviews;
    }
    
    public List<Review> getUserReviews(int userId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        
        String query = "SELECT r.*, b.title AS book_title " +
                      "FROM reviews r " +
                      "JOIN books b ON r.book_id = b.id " +
                      "WHERE r.user_id = ? " +
                      "ORDER BY r.review_date DESC";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Review review = new Review();
                review.setId(rs.getInt("id"));
                review.setUserId(rs.getInt("user_id"));
                review.setBookId(rs.getInt("book_id"));
                review.setRating(rs.getInt("rating"));
                review.setContent(rs.getString("comment"));
                review.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());
                review.setBookTitle(rs.getString("book_title"));
                
                reviews.add(review);
            }
        }
        
        return reviews;
    }
    
    public boolean deleteReview(int reviewId) throws SQLException {
        int bookId = 0;
        String getBookIdQuery = "SELECT book_id FROM reviews WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(getBookIdQuery)) {
            stmt.setInt(1, reviewId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                bookId = rs.getInt("book_id");
            } else {
                return false;
            }
        }
        
        String query = "DELETE FROM reviews WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            
            int result = stmt.executeUpdate();
            
            if (result > 0 && bookId > 0) {
                updateBookRating(bookId);
                return true;
            }
            
            return false;
        }
    }
    
    private void updateBookRating(int bookId) throws SQLException {
        String getStatsQuery = "SELECT AVG(rating) AS avg_rating, COUNT(*) AS review_count " +
                              "FROM reviews WHERE book_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(getStatsQuery)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double avgRating = rs.getDouble("avg_rating");
                int reviewCount = rs.getInt("review_count");
                System.out.println("Book ID " + bookId + " has average rating " + avgRating + 
                                  " from " + reviewCount + " reviews");
            }
        }
    }
}