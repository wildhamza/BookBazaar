package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.observers.CartObserver;
import com.bookshop.observers.CartEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MockCartService {
    
    private List<CartItem> cartItems;
    private List<CartObserver> observers;
    
    public MockCartService() {
        this.cartItems = new ArrayList<>();
        this.observers = new ArrayList<>();
    }
    
    public void addObserver(CartObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }
    
    private void notifyObservers() {
        CartEvent event = new CartEvent(0, CartEvent.EventType.QUANTITY_CHANGED, null, null, 0);
        for (CartObserver observer : observers) {
            observer.update(event);
        }
    }
    
    public boolean addToCart(Book book, int quantity) {
        if (book == null || quantity <= 0) {
            return false;
        }
        
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getBook().getId() == book.getId()) {
                int newQuantity = item.getQuantity() + quantity;
                item.setQuantity(newQuantity);
                notifyObservers();
                return true;
            }
        }
        
        int id = cartItems.size(); 
        CartItem item = new CartItem();
        item.setId(id);
        item.setBook(book);
        item.setQuantity(quantity);
        cartItems.add(item);
        
        notifyObservers();
        return true;
    }
    
    public boolean updateCartItemQuantity(int itemIndex, int quantity) {
        if (itemIndex < 0 || itemIndex >= cartItems.size()) {
            return false;
        }
        
        if (quantity <= 0) {
            return removeFromCart(itemIndex);
        }
        
        cartItems.get(itemIndex).setQuantity(quantity);
        notifyObservers();
        return true;
    }
    
    public boolean removeFromCart(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= cartItems.size()) {
            return false;
        }
        
        cartItems.remove(itemIndex);
        notifyObservers();
        return true;
    }
    
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }
    
    public boolean clearCart() {
        cartItems.clear();
        notifyObservers();
        return true;
    }
    
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : cartItems) {
            BigDecimal price = item.getBook().getPrice();
            int quantity = item.getQuantity();
            total = total.add(price.multiply(new BigDecimal(quantity)));
        }
        
        return total;
    }
}