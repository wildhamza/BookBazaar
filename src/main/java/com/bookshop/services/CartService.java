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

/**
 * Service class for managing user shopping carts.
 */
public class CartService {
    
    private Connection connection;
    private BookService bookService;
    
    /**
     * Default constructor.
     */
    public CartService() {
        try {
            this.connection = com.bookshop.utils.DatabaseConnection.getInstance().getConnection();
            this.bookService = new BookService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        
        try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Update existing item
                int itemId = rs.getInt("id");
                int currentQuantity = rs.getInt("quantity");
                int newQuantity = currentQuantity + quantity;
                
                String updateQuery = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setInt(2, itemId);
                    return updateStmt.executeUpdate() > 0;
                }
            } else {
                // Add new item
                String insertQuery = "INSERT INTO cart_items (user_id, book_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, bookId);
                    insertStmt.setInt(3, quantity);
                    return insertStmt.executeUpdate() > 0;
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
        String getBookIdQuery = "SELECT book_id FROM cart_items WHERE id = ?";
        int bookId;
        
        try (PreparedStatement stmt = connection.prepareStatement(getBookIdQuery)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                return false;
            }
            
            bookId = rs.getInt("book_id");
        }
        
        // Check if the book is available in the requested quantity
        Book book = bookService.getBookById(bookId);
        if (book == null || !book.isAvailable(quantity)) {
            return false;
        }
        
        // Update the quantity
        String updateQuery = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
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
        String query = "DELETE FROM cart_items WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
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
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
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
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
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
     * Calculates the total cost of items in the user's cart.
     * 
     * @param userId The user ID
     * @return The total cost
     * @throws SQLException If a database error occurs
     */
    public BigDecimal calculateTotal(int userId) throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : getCartItems(userId)) {
            total = total.add(item.getSubtotal());
        }
        
        return total;
    }
    
    /**
     * Calculates the total cost of items in the current user's cart.
     * 
     * @return The total cost
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
     * Gets the count of items in the user's cart.
     * 
     * @param userId The user ID
     * @return The count of items
     * @throws SQLException If a database error occurs
     */
    public int getCartItemCount(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM cart_items WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
}