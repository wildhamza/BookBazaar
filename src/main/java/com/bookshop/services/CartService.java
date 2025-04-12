package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.observers.CartObserver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling shopping cart operations.
 * Implements the Observer and Singleton design patterns.
 */
public class CartService {
    
    private static CartService instance;
    private List<CartItem> cartItems;
    private List<CartObserver> observers;
    
    /**
     * Private constructor for Singleton pattern.
     */
    private CartService() {
        this.cartItems = new ArrayList<>();
        this.observers = new ArrayList<>();
    }
    
    /**
     * Gets the singleton instance of the CartService.
     * 
     * @return The singleton instance
     */
    public static synchronized CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }
    
    /**
     * Adds an observer to be notified of cart updates.
     * 
     * @param observer The observer to add
     */
    public void addObserver(CartObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Removes an observer.
     * 
     * @param observer The observer to remove
     */
    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notifies all observers of a cart update.
     */
    private void notifyObservers() {
        for (CartObserver observer : observers) {
            observer.update();
        }
    }
    
    /**
     * Gets all items in the cart.
     * 
     * @return A list of cart items
     */
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems); // Return a copy to prevent external modification
    }
    
    /**
     * Adds a book to the cart.
     * 
     * @param book The book to add
     * @param quantity The quantity to add
     */
    public void addToCart(Book book, int quantity) {
        if (book == null || quantity <= 0) {
            return;
        }
        
        // Check if the book is already in the cart
        for (CartItem item : cartItems) {
            if (item.getBook().getId() == book.getId()) {
                // Update the quantity
                item.setQuantity(item.getQuantity() + quantity);
                notifyObservers();
                return;
            }
        }
        
        // Add a new cart item
        cartItems.add(new CartItem(book, quantity));
        notifyObservers();
    }
    
    /**
     * Updates the quantity of an item in the cart.
     * 
     * @param index The index of the item to update
     * @param quantity The new quantity
     */
    public void updateCartItemQuantity(int index, int quantity) {
        if (index >= 0 && index < cartItems.size() && quantity > 0) {
            cartItems.get(index).setQuantity(quantity);
            notifyObservers();
        }
    }
    
    /**
     * Removes an item from the cart.
     * 
     * @param index The index of the item to remove
     */
    public void removeFromCart(int index) {
        if (index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
            notifyObservers();
        }
    }
    
    /**
     * Clears the cart.
     */
    public void clearCart() {
        cartItems.clear();
        notifyObservers();
    }
    
    /**
     * Calculates the total price of all items in the cart.
     * 
     * @return The total price
     */
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : cartItems) {
            total = total.add(item.getSubtotal());
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Gets the number of items in the cart.
     * 
     * @return The number of items
     */
    public int getItemCount() {
        int count = 0;
        
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        
        return count;
    }
    
    /**
     * Checks if the cart is empty.
     * 
     * @return true if the cart is empty, false otherwise
     */
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
}