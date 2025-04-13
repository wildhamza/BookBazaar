package com.bookshop.observers;

public interface Subject {
    
    void addObserver(CartObserver observer);
    
    void removeObserver(CartObserver observer);
    
    void notifyObservers();
}
