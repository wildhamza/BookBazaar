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

public class CartService {
    
    public interface CartUpdateListener {
        void onCartUpdated(int userId);
    }
    
    private final List<CartUpdateListener> listeners = new CopyOnWriteArrayList<>();
    
    private static CartService instance;
    
    public static CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }
    
    private CartService() { 
        this.bookService = new BookService();
    }
    
    public void addCartUpdateListener(CartUpdateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeCartUpdateListener(CartUpdateListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyCartUpdated(int userId) {
        for (CartUpdateListener listener : listeners) {
            listener.onCartUpdated(userId);
        }
    }
    
    private BookService bookService;
    
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
    
    public boolean addToCart(int userId, int bookId, int quantity) throws SQLException {
        Book book = bookService.getBookById(bookId);
        if (book == null || !book.isAvailable(quantity)) {
            return false;
        }
        
        String checkQuery = "SELECT id, quantity FROM cart_items WHERE user_id = ? AND book_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
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
    
    public boolean updateCartItemQuantity(int itemId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return removeFromCart(itemId);
        }
        
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
        
        Book book = bookService.getBookById(bookId);
        if (book == null || !book.isAvailable(quantity)) {
            return false;
        }
        
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
    
    public boolean removeFromCart(int itemId) throws SQLException {
        int userId;
        String getUserQuery = "SELECT user_id FROM cart_items WHERE id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(getUserQuery)) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                return false;
            }
            
            userId = rs.getInt("user_id");
        }
        
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
    
    public List<CartItem> getCartItems(int userId) throws SQLException {
        List<CartItem> cartItems = new ArrayList<>();
        
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
    
    public List<CartItem> getCartItems() throws SQLException {
        User currentUser = com.bookshop.utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        return getCartItems(currentUser.getId());
    }
    
    public boolean clearCart(int userId) throws SQLException {
        String query = "DELETE FROM cart_items WHERE user_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                notifyCartUpdated(userId);
            }
            return true;
        }
    }
    
    public boolean clearCart() throws SQLException {
        User currentUser = com.bookshop.utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return clearCart(currentUser.getId());
    }
    
    public BigDecimal calculateTotal(int userId) throws SQLException {
        List<CartItem> items = getCartItems(userId);
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : items) {
            total = total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        
        return total;
    }
    
    public BigDecimal calculateTotal() throws SQLException {
        User currentUser = com.bookshop.utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return BigDecimal.ZERO;
        }
        return calculateTotal(currentUser.getId());
    }
    
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
    
    public int getCartItemCount() throws SQLException {
        User currentUser = com.bookshop.utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return 0;
        }
        return getCartItemCount(currentUser.getId());
    }
}