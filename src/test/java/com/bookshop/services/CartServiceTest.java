package com.bookshop.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.observers.CartObserver;
import com.bookshop.observers.CartEvent;

import java.math.BigDecimal;
import java.util.List;

/**
 * Unit tests for the CartService class.
 */
public class CartServiceTest {
    
    private MockCartService cartService;
    private Book testBook;
    private TestCartObserver testObserver;
    
    @BeforeEach
    void setUp() {
        cartService = new MockCartService();
        
        // Create a test book
        testBook = new Book();
        testBook.setId(1);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setPrice(new BigDecimal("19.99"));
        testBook.setStockQuantity(10);
        
        // Create a test observer
        testObserver = new TestCartObserver();
        cartService.addObserver(testObserver);
    }
    
    @AfterEach
    void tearDown() {
        cartService.clearCart();
        cartService.removeObserver(testObserver);
    }
    
    @Test
    @DisplayName("Test adding an item to the cart")
    void testAddToCart() {
        int quantity = 2;
        cartService.addToCart(testBook, quantity);
        
        List<CartItem> cartItems = cartService.getCartItems();
        
        assertEquals(1, cartItems.size(), "Cart should have 1 item");
        assertEquals(testBook.getId(), cartItems.get(0).getBook().getId(), "Cart item should contain the correct book");
        assertEquals(quantity, cartItems.get(0).getQuantity(), "Cart item should have the correct quantity");
        assertTrue(testObserver.wasNotified, "Observer should be notified when an item is added to the cart");
    }
    
    @Test
    @DisplayName("Test updating an item quantity in the cart")
    void testUpdateCartItemQuantity() {
        // Add an item to the cart
        cartService.addToCart(testBook, 1);
        
        // Reset the observer
        testObserver.wasNotified = false;
        
        // Update the quantity
        int newQuantity = 3;
        cartService.updateCartItemQuantity(0, newQuantity);
        
        List<CartItem> cartItems = cartService.getCartItems();
        
        assertEquals(1, cartItems.size(), "Cart should still have 1 item");
        assertEquals(newQuantity, cartItems.get(0).getQuantity(), "Cart item should have the updated quantity");
        assertTrue(testObserver.wasNotified, "Observer should be notified when an item quantity is updated");
    }
    
    @Test
    @DisplayName("Test removing an item from the cart")
    void testRemoveFromCart() {
        // Add an item to the cart
        cartService.addToCart(testBook, 1);
        
        // Reset the observer
        testObserver.wasNotified = false;
        
        // Remove the item
        cartService.removeFromCart(0);
        
        List<CartItem> cartItems = cartService.getCartItems();
        
        assertTrue(cartItems.isEmpty(), "Cart should be empty after removing the item");
        assertTrue(testObserver.wasNotified, "Observer should be notified when an item is removed from the cart");
    }
    
    @Test
    @DisplayName("Test calculating the total cart price")
    void testCalculateTotal() {
        // Create a second test book
        Book testBook2 = new Book();
        testBook2.setId(2);
        testBook2.setTitle("Test Book 2");
        testBook2.setAuthor("Test Author 2");
        testBook2.setPrice(new BigDecimal("29.99"));
        testBook2.setStockQuantity(5);
        
        // Add both books to the cart
        cartService.addToCart(testBook, 2); // 2 x $19.99 = $39.98
        cartService.addToCart(testBook2, 1); // 1 x $29.99 = $29.99
        
        // Calculate the expected total
        BigDecimal expected = new BigDecimal("69.97"); // $39.98 + $29.99 = $69.97
        
        // Get the actual total
        BigDecimal actual = cartService.calculateTotal();
        
        // Compare (allowing for small rounding differences)
        assertEquals(expected.doubleValue(), actual.doubleValue(), 0.01,
                    "Cart total should be calculated correctly");
    }
    
    @Test
    @DisplayName("Test clearing the cart")
    void testClearCart() {
        // Add an item to the cart
        cartService.addToCart(testBook, 1);
        
        // Reset the observer
        testObserver.wasNotified = false;
        
        // Clear the cart
        cartService.clearCart();
        
        List<CartItem> cartItems = cartService.getCartItems();
        
        assertTrue(cartItems.isEmpty(), "Cart should be empty after clearing");
        assertTrue(testObserver.wasNotified, "Observer should be notified when the cart is cleared");
    }
    
    // A simple observer implementation for testing
    private static class TestCartObserver implements CartObserver {
        boolean wasNotified = false;
        
        @Override
        public void update(CartEvent event) {
            wasNotified = true;
        }
    }
    
    // Add more test cases as needed
}