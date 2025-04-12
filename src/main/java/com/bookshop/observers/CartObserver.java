package com.bookshop.observers;

/**
 * Observer interface for cart updates.
 * Implements the Observer pattern.
 */
public interface CartObserver {
    
    /**
     * Called when the cart is updated.
     * 
     * @param event The event describing the update
     */
    void update(CartEvent event);
}