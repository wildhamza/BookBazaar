package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.observers.CartObserver;
import com.bookshop.observers.Subject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for shopping cart operations.
 * Implements Observer pattern to notify observers about cart changes.
 */
public class CartService implements Subject {
    
    private List<CartItem> cartItems;
    private List<CartObserver> observers;
    
    public CartService() {
        cartItems = new ArrayList<>();
        observers = new ArrayList<>();
    }
    
    /**
     * Gets all items in the cart.
     * 
     * @return A list of cart items
     */
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems); // Return a copy to prevent direct modification
    }
    
    /**
     * Adds a book to the cart.
     * 
     * @param book The book to add
     * @param quantity The quantity to add
     */
    public void addToCart(Book book, int quantity) {
        if (book == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid book or quantity");
        }
        
        // Check if the book is already in the cart
        for (CartItem item : cartItems) {
            if (item.getBook().getId() == book.getId()) {
                // Update quantity instead of adding a new item
                int newQuantity = item.getQuantity() + quantity;
                if (newQuantity <= book.getStockQuantity()) {
                    item.setQuantity(newQuantity);
                    notifyObservers();
                    return;
                } else {
                    throw new IllegalArgumentException("Not enough stock available");
                }
            }
        }
        
        // Book not in cart, add as new item
        if (quantity <= book.getStockQuantity()) {
            CartItem newItem = new CartItem(book, quantity);
            cartItems.add(newItem);
            notifyObservers();
        } else {
            throw new IllegalArgumentException("Not enough stock available");
        }
    }
    
    /**
     * Updates the quantity of a cart item.
     * 
     * @param book The book to update
     * @param quantity The new quantity
     */
    public void updateCartItemQuantity(Book book, int quantity) {
        if (book == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid book or quantity");
        }
        
        for (CartItem item : cartItems) {
            if (item.getBook().getId() == book.getId()) {
                if (quantity <= book.getStockQuantity()) {
                    item.setQuantity(quantity);
                    notifyObservers();
                    return;
                } else {
                    throw new IllegalArgumentException("Not enough stock available");
                }
            }
        }
        
        throw new IllegalArgumentException("Book not found in cart");
    }
    
    /**
     * Removes a book from the cart.
     * 
     * @param book The book to remove
     */
    public void removeFromCart(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Invalid book");
        }
        
        cartItems.removeIf(item -> item.getBook().getId() == book.getId());
        notifyObservers();
    }
    
    /**
     * Clears all items from the cart.
     */
    public void clearCart() {
        cartItems.clear();
        notifyObservers();
    }
    
    /**
     * Calculates the total cost of all items in the cart.
     * 
     * @return The total cost
     */
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }
    
    /**
     * Gets the number of items in the cart.
     * 
     * @return The total number of items
     */
    public int getCartItemCount() {
        return cartItems.size();
    }
    
    /**
     * Gets the total quantity of all items in the cart.
     * 
     * @return The total quantity
     */
    public int getCartQuantityCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
    
    // Observer pattern methods
    
    @Override
    public void addObserver(CartObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    @Override
    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        for (CartObserver observer : observers) {
            observer.update();
        }
    }
}
