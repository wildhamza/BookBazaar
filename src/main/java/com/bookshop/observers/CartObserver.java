package com.bookshop.observers;

/**
 * Observer interface for cart updates.
 * Implements the Observer design pattern.
 */
public interface CartObserver {
    
    /**
     * Called when the observed cart is updated.
     */
    void update();
}