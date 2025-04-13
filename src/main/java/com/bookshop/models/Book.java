package com.bookshop.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Book {
    
    private int id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String category;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private LocalDate publicationDate;
    private double averageRating;
    private int ratingCount;
    private int reviewCount;
    private String imageUrl;
    
    private int quantity;
    
    private int salesCount;
    
    public Book() {
        this.price = BigDecimal.ZERO;
    }
    
    public Book(int id, String title, String author, BigDecimal price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
    }
    
    public Book(int id, String title, String author, String isbn, String publisher,
            String category, String description, BigDecimal price, int stockQuantity, 
            LocalDate publicationDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.publicationDate = publicationDate;
    }
    
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
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
    
    public double getAverageRating() {
        return 0;
    }
    
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    public int getRatingCount() {
        return ratingCount;
    }
    
    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }
    
    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            return;
        }
        
        double currentTotal = averageRating * ratingCount;
        ratingCount++;
        averageRating = (currentTotal + rating) / ratingCount;
    }
    
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    public boolean isAvailable(int requestedQuantity) {
        return stockQuantity >= requestedQuantity;
    }
    
    public boolean reduceStock(int amount) {
        if (amount <= 0 || amount > stockQuantity) {
            return false;
        }
        
        stockQuantity -= amount;
        return true;
    }
    
    public void increaseStock(int amount) {
        if (amount > 0) {
            stockQuantity += amount;
        }
    }
    
    public int getQuantity() {
        return quantity > 0 ? quantity : stockQuantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public int getSalesCount() {
        return salesCount;
    }
    
    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }
    
    public void incrementSalesCount(int amount) {
        if (amount > 0) {
            salesCount += amount;
        }
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public int getReviewCount() {
        return 0;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public void increaseQuantity(int amount) {
        if (amount > 0) {
            this.quantity += amount;
        }
    }
    
    public boolean decreaseQuantity(int amount) {
        if (amount > 0 && amount <= this.quantity) {
            this.quantity -= amount;
            return true;
        }
        return false;
    }
}