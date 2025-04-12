package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class representing an item in an order.
 */
public class OrderItem {
    
    private int id;
    private int orderId;
    private int bookId;
    private int quantity;
    private BigDecimal price;
    
    // Additional fields for convenience (not stored in order_items table)
    private String bookTitle;
    private String bookAuthor;
    
    /**
     * Default constructor.
     */
    public OrderItem() {
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param id The order item ID
     * @param orderId The order ID
     * @param bookId The book ID
     * @param quantity The quantity
     * @param price The price
     * @param bookTitle The book title
     * @param bookAuthor The book author
     */
    public OrderItem(int id, int orderId, int bookId, int quantity, BigDecimal price, String bookTitle, String bookAuthor) {
        this.id = id;
        this.orderId = orderId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.price = price;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
    }
    
    /**
     * Gets the order item ID.
     * 
     * @return The order item ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Sets the order item ID.
     * 
     * @param id The order item ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Gets the order ID.
     * 
     * @return The order ID
     */
    public int getOrderId() {
        return orderId;
    }
    
    /**
     * Sets the order ID.
     * 
     * @param orderId The order ID
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
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
     * Gets the price.
     * 
     * @return The price
     */
    public BigDecimal getPrice() {
        return price;
    }
    
    /**
     * Sets the price.
     * 
     * @param price The price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
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
     * Gets the subtotal for this order item.
     * 
     * @return The subtotal
     */
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    @Override
    public String toString() {
        return bookTitle + " (" + quantity + " @ $" + price + ")";
    }
}