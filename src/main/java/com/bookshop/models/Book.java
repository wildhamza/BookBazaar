package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class representing a book in the system.
 */
public class Book {
    private int id;
    private String title;
    private String author;
    private String publisher;
    private BigDecimal price;
    private String category;
    private String isbn;
    private String imageUrl;
    private String description;
    private int stockQuantity;
    private double averageRating;
    private int reviewCount;
    
    // Default constructor
    public Book() {
    }
    
    // Constructor with all fields
    public Book(int id, String title, String author, String publisher, BigDecimal price, 
                String category, String isbn, String imageUrl, String description, 
                int stockQuantity) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.category = category;
        this.isbn = isbn;
        this.imageUrl = imageUrl;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.averageRating = 0.0;
        this.reviewCount = 0;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    public int getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    @Override
    public String toString() {
        return title + " by " + author;
    }
}
