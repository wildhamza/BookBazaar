package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.observers.CartObserver;
import com.bookshop.observers.CartEvent;
import com.bookshop.observers.CartEvent.EventType;
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
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }
    
    public boolean addToCart(Book book, int quantity) {
        if (book == null || quantity <= 0) {
            return false;
        }
        CartItem item = new CartItem();
        item.setId(cartItems.size());
        item.setBook(book);
        item.setQuantity(quantity);
        cartItems.add(item);
        notifyObservers(new CartEvent(0, EventType.ITEM_ADDED, item, book, quantity));
        return true;
    }
    
    public boolean updateCartItemQuantity(int index, int quantity) {
        if (index < 0 || index >= cartItems.size()) {
            return false;
        }
        CartItem item = cartItems.get(index);
        item.setQuantity(quantity);
        notifyObservers(new CartEvent(0, EventType.QUANTITY_CHANGED, item, item.getBook(), quantity));
        return true;
    }
    
    public boolean removeFromCart(int index) {
        if (index < 0 || index >= cartItems.size()) {
            return false;
        }
        CartItem item = cartItems.remove(index);
        notifyObservers(new CartEvent(0, EventType.ITEM_REMOVED, item, item.getBook(), 0));
        return true;
    }
    
    public boolean clearCart() {
        cartItems.clear();
        notifyObservers(new CartEvent(0));
        return true;
    }
    
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }
    
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal price = item.getBook().getPrice();
            BigDecimal quantity = new BigDecimal(item.getQuantity());
            total = total.add(price.multiply(quantity));
        }
        return total;
    }
    
    private void notifyObservers(CartEvent event) {
        for (CartObserver observer : observers) {
            observer.update(event);
        }
    }
}