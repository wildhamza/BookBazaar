package com.bookshop.utils;

import com.bookshop.models.Book;
import com.bookshop.models.BookDTO;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookFactory {
    
    public static Book createFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setPrice(rs.getBigDecimal("price"));
        book.setCategory(rs.getString("category"));
        book.setIsbn(rs.getString("isbn"));
        book.setImageUrl(rs.getString("image_url"));
        book.setDescription(rs.getString("description"));
        book.setStockQuantity(rs.getInt("stock_quantity"));
        
        // Get average rating and review count
        try {
            double avgRating = rs.getDouble("average_rating");
            int reviewCount = rs.getInt("review_count");
            
            // If the rating is NaN (no reviews), set it to 0
            if (Double.isNaN(avgRating)) {
                avgRating = 0.0;
            }
            
            book.setAverageRating(avgRating);
            book.setReviewCount(reviewCount);
        } catch (SQLException e) {
            // If columns don't exist, set default values
            book.setAverageRating(0.0);
            book.setReviewCount(0);
        }
        
        return book;
    }
    
    public static Book createBasicBook(String title, String author, BigDecimal price) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        return book;
    }
    
    public static Book createCompleteBook(
            String title, String author, String publisher, 
            BigDecimal price, String category, String isbn, 
            String imageUrl, String description, int stockQuantity) {
        
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setPrice(price);
        book.setCategory(category);
        book.setIsbn(isbn);
        book.setImageUrl(imageUrl);
        book.setDescription(description);
        book.setStockQuantity(stockQuantity);
        
        return book;
    }
    
    public static Book createBookFromDTO(BookDTO dto) {
        Book book = new Book();
        book.setId(dto.getId());
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setCategory(dto.getCategory());
        book.setDescription(dto.getDescription());
        book.setPrice(dto.getPrice());
        book.setStockQuantity(dto.getStockQuantity());
        book.setPublicationDate(dto.getPublicationDate());
        book.setImageUrl(dto.getImageUrl());
        return book;
    }
    
    public static BookDTO createDTOFromBook(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setPublisher(book.getPublisher());
        dto.setCategory(book.getCategory());
        dto.setDescription(book.getDescription());
        dto.setPrice(book.getPrice());
        dto.setStockQuantity(book.getStockQuantity());
        dto.setPublicationDate(book.getPublicationDate());
        dto.setImageUrl(book.getImageUrl());
        return dto;
    }
}
