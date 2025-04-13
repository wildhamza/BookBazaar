package com.bookshop.observers;

public interface CartObserver {
    void update(CartEvent event);
}