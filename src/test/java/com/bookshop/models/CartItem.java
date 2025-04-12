package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class for cart items used in tests.
 * Simplified version of the real CartItem class.
 */
public class CartItem {
    private int id;
    private Book book;
    private int quantity;
    
    public CartItem() {
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
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
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return book != null ? book.getPrice() : BigDecimal.ZERO;
    }
    
    public BigDecimal getSubtotal() {
        return getPrice().multiply(new BigDecimal(quantity));
    }
} 