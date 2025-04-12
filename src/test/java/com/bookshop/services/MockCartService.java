package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.observers.CartObserver;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of CartService for unit testing
 */
public class MockCartService {
    
    private List<CartItem> cartItems;
    private List<CartObserver> observers;
    
    /**
     * Default constructor.
     */
    public MockCartService() {
        this.cartItems = new ArrayList<>();
        this.observers = new ArrayList<>();
    }
    
    /**
     * Add an observer to receive cart update notifications
     * 
     * @param observer The observer to add
     */
    public void addObserver(CartObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Remove an observer from receiving cart update notifications
     * 
     * @param observer The observer to remove
     */
    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notify all observers of cart update
     */
    private void notifyObservers() {
        for (CartObserver observer : observers) {
            observer.update();
        }
    }
    
    /**
     * Adds a book to the cart
     * 
     * @param book The book to add
     * @param quantity The quantity to add
     * @return true if successful
     */
    public boolean addToCart(Book book, int quantity) {
        if (book == null || quantity <= 0) {
            return false;
        }
        
        // Check if the book already exists in cart
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getBook().getId() == book.getId()) {
                // Update existing item
                int newQuantity = item.getQuantity() + quantity;
                item.setQuantity(newQuantity);
                notifyObservers();
                return true;
            }
        }
        
        // Add new cart item
        int id = cartItems.size(); // Simple ID assignment for mock
        CartItem item = new CartItem();
        item.setId(id);
        item.setBook(book);
        item.setQuantity(quantity);
        cartItems.add(item);
        
        notifyObservers();
        return true;
    }
    
    /**
     * Updates the quantity of an item in the cart
     * 
     * @param itemIndex The index of the item in the cart
     * @param quantity The new quantity
     * @return true if successful
     */
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
    
    /**
     * Removes an item from the cart
     * 
     * @param itemIndex The index of the item in the cart
     * @return true if successful
     */
    public boolean removeFromCart(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= cartItems.size()) {
            return false;
        }
        
        cartItems.remove(itemIndex);
        notifyObservers();
        return true;
    }
    
    /**
     * Gets all items in the cart
     * 
     * @return List of cart items
     */
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }
    
    /**
     * Clears the cart
     * 
     * @return true if successful
     */
    public boolean clearCart() {
        cartItems.clear();
        notifyObservers();
        return true;
    }
    
    /**
     * Calculates the total cost of items in the cart
     * 
     * @return The total cost
     */
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