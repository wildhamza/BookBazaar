package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class representing an item in a user's shopping cart.
 */
public class CartItem {
    
    private int id;
    private int userId;
    private int bookId;
    private int quantity;
    
    // Additional properties for convenience (not stored in the cart_items table)
    private String bookTitle;
    private String bookAuthor;
    private BigDecimal bookPrice;
    
    /**
     * Default constructor.
     */
    public CartItem() {
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id The cart item ID
     * @param userId The user ID
     * @param bookId The book ID
     * @param quantity The quantity
     * @param bookTitle The book title
     * @param bookAuthor The book author
     * @param bookPrice The book price
     */
    public CartItem(int id, int userId, int bookId, int quantity, String bookTitle, String bookAuthor, BigDecimal bookPrice) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookPrice = bookPrice;
    }
    
    /**
     * Gets the cart item ID.
     * 
     * @return The cart item ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Sets the cart item ID.
     * 
     * @param id The cart item ID
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
     * Gets the quantity.
     * 
     * @return The quantity
     */
    public int getQuantity() {
        return quantity;
    }
    
    /**
     * Sets the quantity.
     * 
     * @param quantity The quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
    
    /**
     * Gets the book author.
     * 
     * @return The book author
     */
    public String getBookAuthor() {
        return bookAuthor;
    }
    
    /**
     * Sets the book author.
     * 
     * @param bookAuthor The book author
     */
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    
    /**
     * Gets the book price.
     * 
     * @return The book price
     */
    public BigDecimal getBookPrice() {
        return bookPrice;
    }
    
    /**
     * Sets the book price.
     * 
     * @param bookPrice The book price
     */
    public void setBookPrice(BigDecimal bookPrice) {
        this.bookPrice = bookPrice;
    }
    
    /**
     * Gets the subtotal (price * quantity).
     * 
     * @return The subtotal
     */
    public BigDecimal getSubtotal() {
        return bookPrice.multiply(new BigDecimal(quantity));
    }
    
    @Override
    public String toString() {
        return bookTitle + " (" + quantity + " @ $" + bookPrice + ")";
    }
}