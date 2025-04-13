package com.bookshop.observers;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;

public class CartEvent {
    
    public enum EventType {
        ITEM_ADDED,
        ITEM_REMOVED,
        QUANTITY_CHANGED,
        CART_CLEARED
    }
    
    private final int userId;
    private final EventType type;
    private final CartItem item;
    private final Book book;
    private final int quantity;
    
    public CartEvent(int userId, EventType type, CartItem item, Book book, int quantity) {
        this.userId = userId;
        this.type = type;
        this.item = item;
        this.book = book;
        this.quantity = quantity;
    }
    
    public CartEvent(int userId) {
        this(userId, EventType.CART_CLEARED, null, null, 0);
    }
    
    public int getUserId() {
        return userId;
    }
    
    public EventType getType() {
        return type;
    }
    
    public CartItem getItem() {
        return item;
    }
    
    public Book getBook() {
        return book;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    @Override
    public String toString() {
        switch (type) {
            case ITEM_ADDED:
                return "Added " + quantity + " of '" + book.getTitle() + "' to cart";
            case ITEM_REMOVED:
                return "Removed '" + book.getTitle() + "' from cart";
            case QUANTITY_CHANGED:
                return "Changed quantity of '" + book.getTitle() + "' to " + quantity;
            case CART_CLEARED:
                return "Cart cleared";
            default:
                return "Unknown cart event";
        }
    }
} 