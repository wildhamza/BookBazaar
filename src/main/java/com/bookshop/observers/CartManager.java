package com.bookshop.observers;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for cart observers.
 * Implements the Observer pattern as a Subject.
 */
public class CartManager {
    private List<CartObserver> observers = new ArrayList<>();
    
    /**
     * Add an observer to the list of observers.
     * 
     * @param observer The observer to add
     */
    public void addObserver(CartObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Remove an observer from the list of observers.
     * 
     * @param observer The observer to remove
     */
    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notify all observers of a cart event.
     * 
     * @param event The event to notify observers about
     */
    public void notifyObservers(CartEvent event) {
        for (CartObserver observer : observers) {
            observer.update(event);
        }
    }
} 