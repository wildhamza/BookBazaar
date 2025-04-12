package com.bookshop.utils;

import com.bookshop.models.Book;
import com.bookshop.models.BookDTO;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Factory pattern implementation for creating Book objects.
 * Creates Book objects from various sources (database, form, etc.).
 */
public class BookFactory {
    
    /**
     * Creates a Book object from a ResultSet (typically from a database query).
     * 
     * @param rs The ResultSet containing book data
     * @return A new Book object
     * @throws SQLException If a database error occurs
     */
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
        
        // These might be null if the query doesn't include them
        try {
            book.setAverageRating(rs.getDouble("average_rating"));
        } catch (SQLException e) {
            book.setAverageRating(0.0);
        }
        
        try {
            book.setReviewCount(rs.getInt("review_count"));
        } catch (SQLException e) {
            book.setReviewCount(0);
        }
        
        return book;
    }
    
    /**
     * Creates a basic Book object with essential data.
     * 
     * @param title The book title
     * @param author The book author
     * @param price The book price
     * @return A new Book object with basic data
     */
    public static Book createBasicBook(String title, String author, BigDecimal price) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        return book;
    }
    
    /**
     * Creates a complete Book object with all data fields.
     * 
     * @param title The book title
     * @param author The book author
     * @param publisher The book publisher
     * @param price The book price
     * @param category The book category
     * @param isbn The book ISBN
     * @param imageUrl The URL to the book image
     * @param description The book description
     * @param stockQuantity The initial stock quantity
     * @return A new Book object with all data
     */
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
    
    /**
     * Creates a Book object from a BookDTO.
     * This method is useful for converting between the DTO and domain model layers.
     * 
     * @param dto The BookDTO to convert
     * @return A new Book object populated with data from the DTO
     */
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
    
    /**
     * Creates a BookDTO from a Book domain object.
     * This method is useful for converting between the domain model and DTO layers.
     * 
     * @param book The Book to convert
     * @return A new BookDTO populated with data from the Book
     */
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
