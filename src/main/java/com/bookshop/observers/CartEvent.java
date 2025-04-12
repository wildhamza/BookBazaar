package com.bookshop.observers;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;

/**
 * Event class for cart updates.
 * Used with the Observer pattern to communicate cart changes.
 */
public class CartEvent {
    
    // Event types
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
    
    /**
     * Constructor for CartEvent.
     * 
     * @param userId The user ID
     * @param type The event type
     * @param item The cart item (may be null for CART_CLEARED events)
     * @param book The book (may be null for CART_CLEARED events)
     * @param quantity The quantity
     */
    public CartEvent(int userId, EventType type, CartItem item, Book book, int quantity) {
        this.userId = userId;
        this.type = type;
        this.item = item;
        this.book = book;
        this.quantity = quantity;
    }
    
    /**
     * Constructor for CART_CLEARED events.
     * 
     * @param userId The user ID
     */
    public CartEvent(int userId) {
        this(userId, EventType.CART_CLEARED, null, null, 0);
    }
    
    /**
     * Get the user ID.
     * 
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Get the event type.
     * 
     * @return The event type
     */
    public EventType getType() {
        return type;
    }
    
    /**
     * Get the cart item.
     * 
     * @return The cart item
     */
    public CartItem getItem() {
        return item;
    }
    
    /**
     * Get the book.
     * 
     * @return The book
     */
    public Book getBook() {
        return book;
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
     * Get a string representation of the event.
     * 
     * @return A string representation of the event
     */
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