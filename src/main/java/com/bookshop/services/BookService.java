package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.BookDTO;
import com.bookshop.repositories.BookRepository;
import com.bookshop.repositories.BookRepositoryImpl;
import com.bookshop.utils.BookFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for book-related operations.
 * This class follows the Service Layer pattern, providing an abstraction layer between
 * controllers and the data access layer (repositories).
 */
public class BookService {
    
    private final BookRepository repository;
    
    /**
     * Constructor with the repository dependency.
     * 
     * @param repository The BookRepository implementation to use
     */
    public BookService(BookRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Default constructor that creates a default repository implementation.
     */
    public BookService() {
        this(new BookRepositoryImpl());
    }
    
    /**
     * Gets all books.
     * 
     * @return A list of all books
     * @throws SQLException If a database access error occurs
     */
    public List<Book> getAllBooks() throws SQLException {
        System.out.println("BookService: getAllBooks called");
        
        try {
            List<Book> books = repository.findAll();
            System.out.println("BookService: Found " + books.size() + " books in total");
            return books;
        } catch (SQLException e) {
            System.err.println("BookService: SQLException in getAllBooks: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("BookService: Unexpected exception in getAllBooks: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error retrieving books: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets books by category.
     * 
     * @param category The category to filter by
     * @return A list of books in the specified category
     * @throws SQLException If a database access error occurs
     */
    public List<Book> getBooksByCategory(String category) throws SQLException {
        return repository.findByCategory(category);
    }
    
    /**
     * Gets a book by its ID.
     * 
     * @param id The book ID
     * @return The book, or null if not found
     * @throws SQLException If a database access error occurs
     */
    public Book getBookById(int id) throws SQLException {
        return repository.findById(id);
    }
    
    /**
     * Gets a book by its ID and returns it as a DTO.
     * 
     * @param id The book ID
     * @return The book DTO, or null if not found
     * @throws SQLException If a database access error occurs
     */
    public BookDTO getBookDTOById(int id) throws SQLException {
        Book book = repository.findById(id);
        if (book == null) {
            return null;
        }
        return BookFactory.createDTOFromBook(book);
    }
    
    /**
     * Adds a new book.
     * 
     * @param book The book to add
     * @return The ID of the newly added book
     * @throws SQLException If a database access error occurs
     */
    public int addBook(Book book) throws SQLException {
        return repository.save(book);
    }
    
    /**
     * Adds a new book from a DTO.
     * 
     * @param bookDTO The book DTO to add
     * @return The ID of the newly added book
     * @throws SQLException If a database access error occurs
     */
    public int addBook(BookDTO bookDTO) throws SQLException {
        Book book = BookFactory.createBookFromDTO(bookDTO);
        return repository.save(book);
    }
    
    /**
     * Updates an existing book.
     * 
     * @param book The book to update
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateBook(Book book) throws SQLException {
        return repository.update(book);
    }
    
    /**
     * Updates an existing book from a DTO.
     * 
     * @param bookDTO The book DTO to update
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateBook(BookDTO bookDTO) throws SQLException {
        Book book = BookFactory.createBookFromDTO(bookDTO);
        return repository.update(book);
    }
    
    /**
     * Deletes a book by its ID.
     * 
     * @param id The book ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean deleteBook(int id) throws SQLException {
        return repository.delete(id);
    }
    
    /**
     * Updates the stock quantity of a book.
     * 
     * @param bookId The book ID
     * @param newQuantity The new stock quantity
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateBookQuantity(int bookId, int newQuantity) throws SQLException {
        return repository.updateStockQuantity(bookId, newQuantity);
    }
    
    /**
     * Searches for books by title, author, or description.
     * 
     * @param query The search query
     * @return A list of books matching the search criteria
     * @throws SQLException If a database access error occurs
     */
    public List<Book> searchBooks(String query) throws SQLException {
        return repository.search(query);
    }
    
    /**
     * Searches for books by title, author, or description and returns them as DTOs.
     * 
     * @param query The search query
     * @return A list of book DTOs matching the search criteria
     * @throws SQLException If a database access error occurs
     */
    public List<BookDTO> searchBooksAsDTO(String query) throws SQLException {
        List<Book> books = repository.search(query);
        return books.stream()
            .map(BookFactory::createDTOFromBook)
            .collect(Collectors.toList());
    }
    
    /**
     * Updates the stock quantity of a book by adding (or subtracting) the specified amount.
     * 
     * @param bookId The ID of the book
     * @param quantityChange The amount to add (positive) or subtract (negative)
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateStockQuantity(int bookId, int quantityChange) throws SQLException {
        return repository.updateStockQuantityByDelta(bookId, quantityChange);
    }
}