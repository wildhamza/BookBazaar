package com.bookshop.models;

import java.time.LocalDateTime;

/**
 * Model class representing a book review.
 */
public class Review {
    private int id;
    private int bookId;
    private int userId;
    private String username; // For display purposes
    private int rating; // 1-5 stars
    private String comment;
    private LocalDateTime createdAt;
    
    // Default constructor
    public Review() {
    }
    
    // Constructor with all fields
    public Review(int id, int bookId, int userId, String username, int rating, 
                  String comment, LocalDateTime createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods for display
    public String getFormattedDate() {
        // Format: YYYY-MM-DD HH:MM
        return createdAt.toString().substring(0, 16).replace('T', ' ');
    }
    
    public String getStarsDisplay() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("★"); // Filled star
            } else {
                stars.append("☆"); // Empty star
            }
        }
        return stars.toString();
    }
}
