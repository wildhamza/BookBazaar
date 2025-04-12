package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.CartItem;
import com.bookshop.models.User;
import com.bookshop.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service class for managing user shopping carts.
 */
public class CartService {
    
    // Observer pattern - interface for cart update listeners
    public interface CartUpdateListener {
        void onCartUpdated(int userId);
    }
    
    // List of listeners to notify on cart changes
    private final List<CartUpdateListener> listeners = new CopyOnWriteArrayList<>();
    
    // Singleton instance
    private static CartService instance;
    
    /**
     * Gets the singleton instance of CartService.
     * 
     * @return The CartService instance
     */
    public static CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private CartService() { 
        this.bookService = new BookService();
    }
    
    /**
     * Add a listener to be notified of cart updates.
     * 
     * @param listener The listener to add
     */
    public void addCartUpdateListener(CartUpdateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a cart update listener.
     * 
     * @param listener The listener to remove
     */
    public void removeCartUpdateListener(CartUpdateListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners that a cart has been updated.
     * 
     * @param userId The ID of the user whose cart was updated
     */
    private void notifyCartUpdated(int userId) {
        for (CartUpdateListener listener : listeners) {
            listener.onCartUpdated(userId);
        }
    }
    
    private BookService bookService;
    
    /**
     * Gets a fresh database connection.
     * 
     * @return A new database connection
     * @throws SQLException If a database error occurs
     */
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Adds an item to the user's cart.
     * 
     * @param userId The user ID
     * @param bookId The book ID
     * @param quantity The quantity
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean addToCart(int userId, int bookId, int quantity) throws SQLException {
        // Check if the book exists
        Book book = bookService.getBookById(bookId);
        if (book == null || !book.isAvailable(quantity)) {
            return false;
        }
        
        // Check if the item already exists in the cart
        String checkQuery = "SELECT id, quantity FROM cart_items WHERE user_id = ? AND book_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Update existing item
                int itemId = rs.getInt("id");
                int currentQuantity = rs.getInt("quantity");
                int newQuantity = currentQuantity + quantity;
                
                String updateQuery = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                try (Connection updateConn = getConnection();
                     PreparedStatement updateStmt = updateConn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setInt(2, itemId);
                    int rowsAffected = updateStmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        notifyCartUpdated(userId);
                        return true;
                    }
                    return false;
                }
            } else {
                // Add new item
                String insertQuery = "INSERT INTO cart_items (user_id, book_id, quantity) VALUES (?, ?, ?)";
                try (Connection insertConn = getConnection();
                     PreparedStatement insertStmt = insertConn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, bookId);
                    insertStmt.setInt(3, quantity);
                    int rowsAffected = insertStmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        notifyCartUpdated(userId);
                        return true;
                    }
                    return false;
                }
            }
        }
    }
    
    /**
     * Updates the quantity of an item in the cart.
     * 
     * @param itemId The cart item ID
     * @param quantity The new quantity
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateCartItemQuantity(int itemId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return removeFromCart(itemId);
        }
        
        // Get the book ID to check availability
        String getBookIdQuery = "SELECT book_id, user_id FROM cart_items WHERE id = ?";
        int bookId;
        int userId;
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(getBookIdQuery)) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                return false;
            }
            
            bookId = rs.getInt("book_id");
            userId = rs.getInt("user_id");
        }
        
        // Check if the book is available in the requested quantity
        Book book = bookService.getBookById(bookId);
        if (book == null || !book.isAvailable(quantity)) {
            return false;
        }
        
        // Update the quantity
        String updateQuery = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            
            stmt.setInt(1, quantity);
            stmt.setInt(2, itemId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                notifyCartUpdated(userId);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Removes an item from the cart.
     * 
     * @param itemId The cart item ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean removeFromCart(int itemId) throws SQLException {
        // First get the user ID for notification
        int userId;
        String getUserQuery = "SELECT user_id FROM cart_items WHERE id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(getUserQuery)) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                return false; // Item doesn't exist
            }
            
            userId = rs.getInt("user_id");
        }
        
        // Now delete the item
        String deleteQuery = "DELETE FROM cart_items WHERE id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            
            stmt.setInt(1, itemId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                notifyCartUpdated(userId);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Gets all items in the user's cart.
     * 
     * @param userId The user ID
     * @return A list of cart items
     * @throws SQLException If a database error occurs
     */
    public List<CartItem> getCartItems(int userId) throws SQLException {
        List<CartItem> cartItems = new ArrayList<>();
        
        // Join with books to get additional details
        String query = "SELECT ci.id, ci.user_id, ci.book_id, ci.quantity, " +
                      "b.title, b.author, b.price " +
                      "FROM cart_items ci " +
                      "JOIN books b ON ci.book_id = b.id " +
                      "WHERE ci.user_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CartItem item = new CartItem(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("book_id"),
                    rs.getInt("quantity"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getBigDecimal("price")
                );
                cartItems.add(item);
            }
        }
        
        return cartItems;
    }
    
    /**
     * Gets all items in the user's cart.
     * Uses the current user if userId is not provided.
     * 
     * @return A list of cart items
     * @throws SQLException If a database error occurs
     */
    public List<CartItem> getCartItems() throws SQLException {
        User currentUser = com.bookshop.utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        return getCartItems(currentUser.getId());
    }
    
    /**
     * Clears the user's cart.
     * 
     * @param userId The user ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean clearCart(int userId) throws SQLException {
        String query = "DELETE FROM cart_items WHERE user_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                notifyCartUpdated(userId);
            }
            return true; // Return true even if no items were in the cart
        }
    }
    
    /**
     * Clears the current user's cart.
     * 
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean clearCart() throws SQLException {
        User currentUser = com.bookshop.utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return clearCart(currentUser.getId());
    }
    
    /**
     * Calculates the total price of items in the user's cart.
     * 
     * @param userId The user ID
     * @return The total price
     * @throws SQLException If a database error occurs
     */
    public BigDecimal calculateTotal(int userId) throws SQLException {
        List<CartItem> items = getCartItems(userId);
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : items) {
            total = total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        
        return total;
    }
    
    /**
     * Calculates the total price of items in the current user's cart.
     * 
     * @return The total price
     * @throws SQLException If a database error occurs
     */
    public BigDecimal calculateTotal() throws SQLException {
        User currentUser = com.bookshop.utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return BigDecimal.ZERO;
        }
        return calculateTotal(currentUser.getId());
    }
    
    /**
     * Gets the number of items in the user's cart.
     * 
     * @param userId The user ID
     * @return The number of items
     * @throws SQLException If a database error occurs
     */
    public int getCartItemCount(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM cart_items WHERE user_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
        }
    }
    
    /**
     * Gets the number of items in the current user's cart.
     * 
     * @return The number of items
     * @throws SQLException If a database error occurs
     */
    public int getCartItemCount() throws SQLException {
        User currentUser = com.bookshop.utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return 0;
        }
        return getCartItemCount(currentUser.getId());
    }
}