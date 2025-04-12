package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class representing an item within an order.
 */
public class OrderItem {
    
    private int id;
    private int orderId;
    private int bookId;
    private int quantity;
    private BigDecimal price;
    private Book book; // For convenience in UI display
    
    // Additional fields for UI display
    private String bookTitle;
    private String bookAuthor;
    
    /**
     * Default constructor.
     */
    public OrderItem() {
        this.quantity = 1;
        this.price = BigDecimal.ZERO;
    }
    
    /**
     * Constructor with basic order item information.
     * 
     * @param orderId The order ID
     * @param bookId The book ID
     * @param quantity The quantity
     * @param price The price at time of order
     */
    public OrderItem(int orderId, int bookId, int quantity, BigDecimal price) {
        this.orderId = orderId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.price = price;
    }
    
    /**
     * Get the order item ID.
     * 
     * @return The order item ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set the order item ID.
     * 
     * @param id The order item ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get the order ID.
     * 
     * @return The order ID
     */
    public int getOrderId() {
        return orderId;
    }
    
    /**
     * Set the order ID.
     * 
     * @param orderId The order ID
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    /**
     * Get the book ID.
     * 
     * @return The book ID
     */
    public int getBookId() {
        return bookId;
    }
    
    /**
     * Set the book ID.
     * 
     * @param bookId The book ID
     */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    /**
     * Get the quantity.
     * 
     * @return The quantity
     */
    public int getQuantity() {
        return quantity;
    }
    
    /**
     * Set the quantity.
     * 
     * @param quantity The quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
     * @param price The price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    /**
     * Get the book object.
     * 
     * @return The book
     */
    public Book getBook() {
        return book;
    }
    
    /**
     * Set the book object.
     * 
     * @param book The book
     */
    public void setBook(Book book) {
        this.book = book;
    }
    
    /**
     * Calculate the subtotal for this order item.
     * 
     * @return The subtotal
     */
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    /**
     * Get the book title.
     * 
     * @return The book title
     */
    public String getBookTitle() {
        if (book != null) {
            return book.getTitle();
        }
        return bookTitle;
    }
    
    /**
     * Set the book title for display purposes.
     * 
     * @param bookTitle The book title
     */
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    /**
     * Get the book author.
     * 
     * @return The book author
     */
    public String getBookAuthor() {
        if (book != null) {
            return book.getAuthor();
        }
        return bookAuthor;
    }
    
    /**
     * Set the book author for display purposes.
     * 
     * @param bookAuthor The book author
     */
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
}