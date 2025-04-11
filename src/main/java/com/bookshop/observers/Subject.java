package com.bookshop.observers;

/**
 * Subject interface for the Observer pattern.
 * Classes implementing this interface can have observers that are notified of changes.
 */
public interface Subject {
    
    /**
     * Adds an observer to this subject.
     * 
     * @param observer The observer to add
     */
    void addObserver(CartObserver observer);
    
    /**
     * Removes an observer from this subject.
     * 
     * @param observer The observer to remove
     */
    void removeObserver(CartObserver observer);
    
    /**
     * Notifies all observers of a change.
     */
    void notifyObservers();
}
