package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class representing an item in the shopping cart.
 */
public class CartItem {
    private Book book;
    private int quantity;
    
    // Default constructor
    public CartItem() {
    }
    
    // Constructor with book and quantity
    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
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
        this.quantity = quantity;
    }
    
    // Increment quantity by 1
    public void incrementQuantity() {
        quantity++;
    }
    
    // Decrement quantity by 1, with a minimum of 1
    public void decrementQuantity() {
        if (quantity > 1) {
            quantity--;
        }
    }
    
    // Calculate the subtotal for this cart item
    public BigDecimal getSubtotal() {
        return book.getPrice().multiply(new BigDecimal(quantity));
    }
    
    @Override
    public String toString() {
        return book.getTitle() + " x " + quantity;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CartItem that = (CartItem) obj;
        
        return book != null && book.getId() == that.book.getId();
    }
    
    @Override
    public int hashCode() {
        return book != null ? book.getId() : 0;
    }
}
