package com.bookshop.observers;

/**
 * Observer pattern interface for cart updates.
 * This interface is implemented by classes that need to be notified when the cart changes.
 */
public interface CartObserver {
    
    /**
     * Called when the observed subject (cart) changes.
     */
    void update();
}
