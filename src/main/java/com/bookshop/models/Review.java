package com.bookshop.models;

import java.time.LocalDateTime;

/**
 * Model class representing a book review.
 */
public class Review {
    
    private int id;
    private int userId;
    private int bookId;
    private int rating;
    private String content;
    private LocalDateTime reviewDate;
    
    // Additional fields for display purposes
    private String username;
    private String bookTitle;
    
    /**
     * Default constructor.
     */
    public Review() {
        this.reviewDate = LocalDateTime.now();
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id The review ID
     * @param userId The user ID
     * @param bookId The book ID
     * @param rating The rating (1-5)
     * @param content The review content
     * @param reviewDate The review date
     * @param username The username
     * @param bookTitle The book title
     */
    public Review(int id, int userId, int bookId, int rating, String content, 
                 LocalDateTime reviewDate, String username, String bookTitle) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.content = content;
        this.reviewDate = reviewDate;
        this.username = username;
        this.bookTitle = bookTitle;
    }
    
    /**
     * Gets the review ID.
     * 
     * @return The review ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Sets the review ID.
     * 
     * @param id The review ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Gets the user ID.
     * 
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Sets the user ID.
     * 
     * @param userId The user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    /**
     * Gets the book ID.
     * 
     * @return The book ID
     */
    public int getBookId() {
        return bookId;
    }
    
    /**
     * Sets the book ID.
     * 
     * @param bookId The book ID
     */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    /**
     * Gets the rating.
     * 
     * @return The rating
     */
    public int getRating() {
        return rating;
    }
    
    /**
     * Sets the rating.
     * 
     * @param rating The rating
     */
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    /**
     * Gets the review content.
     * 
     * @return The review content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Sets the review content.
     * 
     * @param content The review content
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Gets the review date.
     * 
     * @return The review date
     */
    public LocalDateTime getReviewDate() {
        return reviewDate;
    }
    
    /**
     * Sets the review date.
     * 
     * @param reviewDate The review date
     */
    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }
    
    /**
     * Gets the username.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username.
     * 
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Gets the book title.
     * 
     * @return The book title
     */
    public String getBookTitle() {
        return bookTitle;
    }
    
    /**
     * Sets the book title.
     * 
     * @param bookTitle The book title
     */
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    @Override
    public String toString() {
        return username + " rated " + rating + " stars: " + content;
    }
}