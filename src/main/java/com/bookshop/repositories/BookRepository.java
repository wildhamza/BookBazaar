package com.bookshop.repositories;

import com.bookshop.models.Book;
import java.sql.SQLException;
import java.util.List;

public interface BookRepository extends Repository<Book, Integer> {
    
    List<Book> findByCategory(String category) throws SQLException;
    
    List<Book> search(String query) throws SQLException;
    
    boolean updateStockQuantity(int bookId, int newQuantity) throws SQLException;
    
    boolean updateStockQuantityByDelta(int bookId, int quantityChange) throws SQLException;
} 