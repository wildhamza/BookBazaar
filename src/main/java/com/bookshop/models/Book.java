package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class representing a book.
 */
public class Book {
    
    private int id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private int quantity;
    private String category;
    private String description;
    private String imageUrl;
    private String publisher;
    private int stockQuantity;
    private double averageRating;
    private int reviewCount;
    
    /**
     * Default constructor.
     */
    public Book() {
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id The book ID
     * @param title The book title
     * @param author The book author
     * @param isbn The book ISBN
     * @param price The book price
     * @param quantity The book quantity
     * @param category The book category
     * @param description The book description
     * @param imageUrl The book image URL
     */
    public Book(int id, String title, String author, String isbn, BigDecimal price, int quantity,
               String category, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the book ID.
     * 
     * @return The book ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the book ID.
     * 
     * @param id The book ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the book title.
     * 
     * @return The book title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the book title.
     * 
     * @param title The book title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the book author.
     * 
     * @return The book author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the book author.
     * 
     * @param author The book author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the book ISBN.
     * 
     * @return The book ISBN
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Sets the book ISBN.
     * 
     * @param isbn The book ISBN
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Gets the book price.
     * 
     * @return The book price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the book price.
     * 
     * @param price The book price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Gets the book quantity.
     * 
     * @return The book quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the book quantity.
     * 
     * @param quantity The book quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the book category.
     * 
     * @return The book category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the book category.
     * 
     * @param category The book category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the book description.
     * 
     * @return The book description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the book description.
     * 
     * @param description The book description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the book image URL.
     * 
     * @return The book image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the book image URL.
     * 
     * @param imageUrl The book image URL
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    /**
     * Gets the book publisher.
     * 
     * @return The book publisher
     */
    public String getPublisher() {
        return publisher;
    }
    
    /**
     * Sets the book publisher.
     * 
     * @param publisher The book publisher
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    /**
     * Gets the book stock quantity.
     * 
     * @return The book stock quantity
     */
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    /**
     * Sets the book stock quantity.
     * 
     * @param stockQuantity The book stock quantity
     */
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    /**
     * Gets the book average rating.
     * 
     * @return The book average rating
     */
    public double getAverageRating() {
        return averageRating;
    }
    
    /**
     * Sets the book average rating.
     * 
     * @param averageRating The book average rating
     */
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    /**
     * Gets the book review count.
     * 
     * @return The book review count
     */
    public int getReviewCount() {
        return reviewCount;
    }
    
    /**
     * Sets the book review count.
     * 
     * @param reviewCount The book review count
     */
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    /**
     * Decreases the quantity by the given amount.
     * 
     * @param amount The amount to decrease
     * @return true if successful, false if not enough quantity
     */
    public boolean decreaseQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Increases the quantity by the given amount.
     * 
     * @param amount The amount to increase
     */
    public void increaseQuantity(int amount) {
        quantity += amount;
    }
    
    /**
     * Checks if the book is in stock.
     * 
     * @return true if in stock, false otherwise
     */
    public boolean isInStock() {
        return quantity > 0;
    }
    
    /**
     * Checks if the book is available in the requested quantity.
     * 
     * @param requestedQuantity The requested quantity
     * @return true if available, false otherwise
     */
    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }
    
    @Override
    public String toString() {
        return title + " by " + author + " ($" + price + ")";
    }
}