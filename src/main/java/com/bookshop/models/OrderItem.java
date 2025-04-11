package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class representing an item within an order.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int bookId;
    private String bookTitle; // For display purposes
    private String bookAuthor; // For display purposes
    private int quantity;
    private BigDecimal priceAtPurchase;
    
    // Default constructor
    public OrderItem() {
    }
    
    // Constructor for new items
    public OrderItem(Book book, int quantity) {
        this.bookId = book.getId();
        this.bookTitle = book.getTitle();
        this.bookAuthor = book.getAuthor();
        this.quantity = quantity;
        this.priceAtPurchase = book.getPrice();
    }
    
    // Constructor with all fields
    public OrderItem(int id, int orderId, int bookId, String bookTitle, String bookAuthor,
                     int quantity, BigDecimal priceAtPurchase) {
        this.id = id;
        this.orderId = orderId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }
    
    // Calculate subtotal
    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(new BigDecimal(quantity));
    }
    
    @Override
    public String toString() {
        return bookTitle + " x " + quantity;
    }
}
