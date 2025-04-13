package com.bookshop.services;

import com.bookshop.models.Book;
import com.bookshop.models.BookDTO;
import com.bookshop.repositories.BookRepository;
import com.bookshop.repositories.BookRepositoryImpl;
import com.bookshop.utils.BookFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class BookService {
    
    private final BookRepository repository;
    
    public BookService(BookRepository repository) {
        this.repository = repository;
    }
    
    public BookService() {
        this(new BookRepositoryImpl());
    }
    
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
    
    public List<Book> getBooksByCategory(String category) throws SQLException {
        return repository.findByCategory(category);
    }
    
    public Book getBookById(int id) throws SQLException {
        return repository.findById(id);
    }
    
    public BookDTO getBookDTOById(int id) throws SQLException {
        Book book = repository.findById(id);
        if (book == null) {
            return null;
        }
        return BookFactory.createDTOFromBook(book);
    }
    
    public int addBook(Book book) throws SQLException {
        return repository.save(book);
    }
    
    public int addBook(BookDTO bookDTO) throws SQLException {
        Book book = BookFactory.createBookFromDTO(bookDTO);
        return repository.save(book);
    }
    
    public boolean updateBook(Book book) throws SQLException {
        return repository.update(book);
    }
    
    public boolean updateBook(BookDTO bookDTO) throws SQLException {
        Book book = BookFactory.createBookFromDTO(bookDTO);
        return repository.update(book);
    }
    
    public boolean deleteBook(int id) throws SQLException {
        return repository.delete(id);
    }
    
    public boolean updateBookQuantity(int bookId, int newQuantity) throws SQLException {
        return repository.updateStockQuantity(bookId, newQuantity);
    }
    
    public List<Book> searchBooks(String query) throws SQLException {
        return repository.search(query);
    }
    
    public List<BookDTO> searchBooksAsDTO(String query) throws SQLException {
        List<Book> books = repository.search(query);
        return books.stream()
            .map(BookFactory::createDTOFromBook)
            .collect(Collectors.toList());
    }
    
    public boolean updateStockQuantity(int bookId, int quantityChange) throws SQLException {
        return repository.updateStockQuantityByDelta(bookId, quantityChange);
    }
}