package com.bookshop.models;

import java.math.BigDecimal;

/**
 * Model class for cart items.
 */
public class CartItem {
    
    private int id;
    private int userId;
    private int bookId;
    private int quantity;
    private String title;
    private String author;
    private BigDecimal price;
    private Book book;
    
    /**
     * Default constructor.
     */
    public CartItem() {
        // Default constructor
        this.price = BigDecimal.ZERO;
    }
    
    /**
     * Constructor with all database fields.
     * 
     * @param id The cart item ID
     * @param userId The user ID
     * @param bookId The book ID
     * @param quantity The quantity
     * @param title The book title
     * @param author The book author
     * @param price The book price
     */
    public CartItem(int id, int userId, int bookId, int quantity, String title, String author, BigDecimal price) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.title = title;
        this.author = author;
        this.price = price;
    }
    
    /**
     * Constructor with book object.
     * 
     * @param book The book
     * @param quantity The quantity
     */
    public CartItem(Book book, int quantity) {
        this.book = book;
        this.bookId = book.getId();
        this.quantity = quantity;
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.price = book.getPrice();
    }
    
    /**
     * Get the cart item ID.
     * 
     * @return The cart item ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set the cart item ID.
     * 
     * @param id The cart item ID to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get the user ID.
     * 
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Set the user ID.
     * 
     * @param userId The user ID to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    /**
     * Get the book ID.
     * 
     * @return The book ID
     */
    public int getBookId() {
        return bookId;
    }
    
    /**
     * Set the book ID.
     * 
     * @param bookId The book ID to set
     */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    /**
     * Get the quantity.
     * 
     * @return The quantity
     */
    public int getQuantity() {
        return quantity;
    }
    
    /**
     * Set the quantity.
     * 
     * @param quantity The quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    /**
     * Get the book title.
     * 
     * @return The book title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the book title.
     * 
     * @param title The book title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Get the book author.
     * 
     * @return The book author
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * Set the book author.
     * 
     * @param author The book author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }
    
    /**
     * Get the book price.
     * 
     * @return The book price
     */
    public BigDecimal getPrice() {
        return price;
    }
    
    /**
     * Set the book price.
     * 
     * @param price The book price to set
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    /**
     * Get the book.
     * 
     * @return The book
     */
    public Book getBook() {
        return book;
    }
    
    /**
     * Set the book.
     * 
     * @param book The book to set
     */
    public void setBook(Book book) {
        this.book = book;
        if (book != null) {
            this.bookId = book.getId();
            this.title = book.getTitle();
            this.author = book.getAuthor();
            this.price = book.getPrice();
        }
    }
    
    /**
     * Calculate the subtotal.
     * 
     * @return The subtotal
     */
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    /**
     * Get a string representation of the subtotal.
     * 
     * @return The subtotal as a string
     */
    public String getSubtotalString() {
        return "$" + getSubtotal().toString();
    }
    
    /**
     * Get the book title.
     * Alias for getTitle().
     * 
     * @return The book title
     */
    public String getBookTitle() {
        return title;
    }
    
    /**
     * Get the book author.
     * Alias for getAuthor().
     * 
     * @return The book author
     */
    public String getBookAuthor() {
        return author;
    }
    
    /**
     * Get the book price.
     * Alias for getPrice().
     * 
     * @return The book price
     */
    public BigDecimal getBookPrice() {
        return price;
    }
}