package com.bookshop.models;

import java.time.LocalDateTime;

public class Review {
    
    private int id;
    private int userId;
    private int bookId;
    private int rating;
    private String content;
    private LocalDateTime reviewDate;
    
    private String username;
    private String bookTitle;
    
    public Review() {
        this.reviewDate = LocalDateTime.now();
    }
    
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
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getBookId() {
        return bookId;
    }
    
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getReviewDate() {
        return reviewDate;
    }
    
    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    @Override
    public String toString() {
        return username + " rated " + rating + " stars: " + content;
    }
}