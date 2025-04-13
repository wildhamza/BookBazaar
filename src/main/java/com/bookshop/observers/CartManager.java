package com.bookshop.observers;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private List<CartObserver> observers = new ArrayList<>();
    
    public void addObserver(CartObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(CartEvent event) {
        for (CartObserver observer : observers) {
            observer.update(event);
        }
    }
}