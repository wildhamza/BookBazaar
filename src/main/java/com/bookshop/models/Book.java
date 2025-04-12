package com.bookshop.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for books.
 */
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
    
    // Used by Cart and Order items
    private int quantity;
    
    // For admin dashboard
    private int salesCount;
    
    /**
     * Default constructor.
     */
    public Book() {
        // Default constructor
        this.price = BigDecimal.ZERO;
    }
    
    /**
     * Parameterized constructor.
     * 
     * @param id The book ID
     * @param title The title
     * @param author The author
     * @param price The price
     */
    public Book(int id, String title, String author, BigDecimal price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
    }
    
    /**
     * Full parameterized constructor.
     * 
     * @param id The book ID
     * @param title The title
     * @param author The author
     * @param isbn The ISBN
     * @param publisher The publisher
     * @param category The category
     * @param description The description
     * @param price The price
     * @param stockQuantity The stock quantity
     * @param publicationDate The publication date
     */
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
    
    /**
     * Get the book ID.
     * 
     * @return The book ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set the book ID.
     * 
     * @param id The book ID to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get the title.
     * 
     * @return The title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the title.
     * 
     * @param title The title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Get the author.
     * 
     * @return The author
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * Set the author.
     * 
     * @param author The author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }
    
    /**
     * Get the ISBN.
     * 
     * @return The ISBN
     */
    public String getIsbn() {
        return isbn;
    }
    
    /**
     * Set the ISBN.
     * 
     * @param isbn The ISBN to set
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    /**
     * Get the publisher.
     * 
     * @return The publisher
     */
    public String getPublisher() {
        return publisher;
    }
    
    /**
     * Set the publisher.
     * 
     * @param publisher The publisher to set
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    /**
     * Get the category.
     * 
     * @return The category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Set the category.
     * 
     * @param category The category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Get the description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description.
     * 
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the price.
     * 
     * @return The price
     */
    public BigDecimal getPrice() {
        return price;
    }
    
    /**
     * Set the price.
     * 
     * @param price The price to set
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    /**
     * Get the stock quantity.
     * 
     * @return The stock quantity
     */
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    /**
     * Set the stock quantity.
     * 
     * @param stockQuantity The stock quantity to set
     */
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    /**
     * Get the publication date.
     * 
     * @return The publication date
     */
    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    
    /**
     * Set the publication date.
     * 
     * @param publicationDate The publication date to set
     */
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
    
    /**
     * Get the average rating.
     * 
     * @return The average rating
     */
    public double getAverageRating() {
        return averageRating;
    }
    
    /**
     * Set the average rating.
     * 
     * @param averageRating The average rating to set
     */
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    /**
     * Get the rating count.
     * 
     * @return The rating count
     */
    public int getRatingCount() {
        return ratingCount;
    }
    
    /**
     * Set the rating count.
     * 
     * @param ratingCount The rating count to set
     */
    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }
    
    /**
     * Add a new rating to the book and update the average.
     * 
     * @param rating The rating to add (1-5)
     */
    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            return; // Invalid rating
        }
        
        double currentTotal = averageRating * ratingCount;
        ratingCount++;
        averageRating = (currentTotal + rating) / ratingCount;
    }
    
    /**
     * Check if the book is in stock.
     * 
     * @return true if the book is in stock, false otherwise
     */
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    /**
     * Check if the book is available in the requested quantity.
     * 
     * @param requestedQuantity The requested quantity
     * @return true if the book is available in the requested quantity, false otherwise
     */
    public boolean isAvailable(int requestedQuantity) {
        return stockQuantity >= requestedQuantity;
    }
    
    /**
     * Reduce the stock quantity by the given amount.
     * 
     * @param amount The amount to reduce by
     * @return true if successful, false if not enough stock
     */
    public boolean reduceStock(int amount) {
        if (amount <= 0 || amount > stockQuantity) {
            return false;
        }
        
        stockQuantity -= amount;
        return true;
    }
    
    /**
     * Increase the stock quantity by the given amount.
     * 
     * @param amount The amount to increase by
     */
    public void increaseStock(int amount) {
        if (amount > 0) {
            stockQuantity += amount;
        }
    }
    
    /**
     * Get the quantity for cart or order item.
     * 
     * @return The quantity
     */
    public int getQuantity() {
        return quantity;
    }
    
    /**
     * Set the quantity for cart or order item.
     * 
     * @param quantity The quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    /**
     * Get the sales count.
     * 
     * @return The sales count
     */
    public int getSalesCount() {
        return salesCount;
    }
    
    /**
     * Set the sales count.
     * 
     * @param salesCount The sales count to set
     */
    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }
    
    /**
     * Increment the sales count.
     * 
     * @param amount The amount to increment by
     */
    public void incrementSalesCount(int amount) {
        if (amount > 0) {
            salesCount += amount;
        }
    }
    
    /**
     * Get the image URL.
     * 
     * @return The image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }
    
    /**
     * Set the image URL.
     * 
     * @param imageUrl The image URL to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    /**
     * Get the review count.
     * 
     * @return The review count
     */
    public int getReviewCount() {
        return reviewCount;
    }
    
    /**
     * Set the review count.
     * 
     * @param reviewCount The review count to set
     */
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    /**
     * Increase the quantity.
     * 
     * @param amount The amount to increase by
     */
    public void increaseQuantity(int amount) {
        if (amount > 0) {
            this.quantity += amount;
        }
    }
    
    /**
     * Decrease the quantity.
     * 
     * @param amount The amount to decrease by
     * @return true if successful, false if not enough quantity
     */
    public boolean decreaseQuantity(int amount) {
        if (amount > 0 && amount <= this.quantity) {
            this.quantity -= amount;
            return true;
        }
        return false;
    }
}