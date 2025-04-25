package com.bookshop.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.observers.CartEvent;
import com.bookshop.observers.CartObserver;

import java.math.BigDecimal;
import java.util.List;

public class CartServiceTest {
    
    private MockCartService cartService;
    private Book testBook;
    private TestCartObserver testObserver;
    
    @BeforeEach
    void setUp() {
        cartService = new MockCartService();
        
        testBook = new Book();
        testBook.setId(1);
        testBook.setTitle("The Great Gatsby");
        testBook.setAuthor("F. Scott Fitzgerald");
        testBook.setPrice(new BigDecimal("12.99"));
        testBook.setStockQuantity(50);
        testBook.setCategory("Fiction");
        testBook.setIsbn("9780743273565");
        testBook.setDescription("The story of eccentric millionaire Jay Gatsby and his passion for the beautiful Daisy Buchanan.");
        
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
        assertTrue(cartService.addToCart(testBook, 2), "Adding item to cart should succeed");
        
        List<CartItem> cartItems = cartService.getCartItems();
        assertEquals(1, cartItems.size(), "Cart should have 1 item");
        assertEquals(testBook.getId(), cartItems.get(0).getBook().getId(), "Cart item should contain the correct book");
        assertEquals(2, cartItems.get(0).getQuantity(), "Cart item should have the correct quantity");
        assertTrue(testObserver.wasNotified, "Observer should be notified when an item is added to the cart");
    }
    
    @Test
    @DisplayName("Test updating an item quantity in the cart")
    void testUpdateCartItemQuantity() {
        cartService.addToCart(testBook, 1);
        testObserver.wasNotified = false;
        
        assertTrue(cartService.updateCartItemQuantity(0, 3), "Updating cart item quantity should succeed");
        
        List<CartItem> cartItems = cartService.getCartItems();
        assertEquals(1, cartItems.size(), "Cart should still have 1 item");
        assertEquals(3, cartItems.get(0).getQuantity(), "Cart item should have the updated quantity");
        assertTrue(testObserver.wasNotified, "Observer should be notified when an item quantity is updated");
    }
    
    @Test
    @DisplayName("Test removing an item from the cart")
    void testRemoveFromCart() {
        cartService.addToCart(testBook, 1);
        testObserver.wasNotified = false;
        
        assertTrue(cartService.removeFromCart(0), "Removing item from cart should succeed");
        
        List<CartItem> cartItems = cartService.getCartItems();
        assertTrue(cartItems.isEmpty(), "Cart should be empty after removing the item");
        assertTrue(testObserver.wasNotified, "Observer should be notified when an item is removed from the cart");
    }
    
    @Test
    @DisplayName("Test calculating the total cart price")
    void testCalculateTotal() {
        Book testBook2 = new Book();
        testBook2.setId(2);
        testBook2.setTitle("To Kill a Mockingbird");
        testBook2.setAuthor("Harper Lee");
        testBook2.setPrice(new BigDecimal("14.99"));
        testBook2.setStockQuantity(45);
        testBook2.setCategory("Fiction");
        testBook2.setIsbn("9780061120084");
        
        cartService.addToCart(testBook, 2);  // 2 x 12.99 = 25.98
        cartService.addToCart(testBook2, 1); // 1 x 14.99 = 14.99
        
        BigDecimal expected = new BigDecimal("40.97"); // 25.98 + 14.99
        BigDecimal actual = cartService.calculateTotal();
        
        assertEquals(0, expected.compareTo(actual), "Cart total should be calculated correctly");
    }
    
    @Test
    @DisplayName("Test clearing the cart")
    void testClearCart() {
        cartService.addToCart(testBook, 1);
        testObserver.wasNotified = false;
        
        assertTrue(cartService.clearCart(), "Clearing cart should succeed");
        
        List<CartItem> cartItems = cartService.getCartItems();
        assertTrue(cartItems.isEmpty(), "Cart should be empty after clearing");
        assertTrue(testObserver.wasNotified, "Observer should be notified when the cart is cleared");
    }
    
    private static class TestCartObserver implements CartObserver {
        boolean wasNotified = false;
        
        @Override
        public void update(CartEvent event) {
            wasNotified = true;
            // Remove the throw statement
        }
    }
}