package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Represents an item in a shopping cart.
 */
public class CartItem {
    
    private Book book;
    private int quantity;
    
    // Constructor
    public CartItem() {
        this.quantity = 1;
    }
    
    // Constructor with book
    public CartItem(Book book) {
        this.book = book;
        this.quantity = 1;
    }
    
    // Constructor with book and quantity
    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity > 0 ? quantity : 1;
    }
    
    // Getters and setters
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity > 0 ? quantity : 1;
    }
    
    /**
     * Increases the quantity by one.
     */
    public void increaseQuantity() {
        this.quantity++;
    }
    
    /**
     * Decreases the quantity by one, but not below 1.
     */
    public void decreaseQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }
    
    /**
     * Calculates the subtotal for this cart item (book price * quantity).
     * 
     * @return The subtotal for this cart item
     */
    public BigDecimal getSubtotal() {
        if (book == null) {
            return BigDecimal.ZERO;
        }
        
        return book.getPrice().multiply(new BigDecimal(quantity));
    }
    
    @Override
    public String toString() {
        return "CartItem{" +
                "book=" + (book != null ? book.getTitle() : "null") +
                ", quantity=" + quantity +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}