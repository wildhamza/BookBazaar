package com.bookshop.observers;

/**
 * Observer interface for cart updates.
 * Used by test classes to observe changes in the shopping cart.
 */
public interface CartObserver {
    
    /**
     * Called when the cart is updated.
     */
    void update();
} 