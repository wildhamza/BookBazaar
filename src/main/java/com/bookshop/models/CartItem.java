package com.bookshop.models;

import java.math.BigDecimal;

public class CartItem {
    
    private int id;
    private int userId;
    private int bookId;
    private int quantity;
    private String title;
    private String author;
    private BigDecimal price;
    private Book book;
    
    public CartItem() {
        this.price = BigDecimal.ZERO;
    }
    
    public CartItem(int id, int userId, int bookId, int quantity, String title, String author, BigDecimal price) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.title = title;
        this.author = author;
        this.price = price;
    }
    
    public CartItem(Book book, int quantity) {
        this.book = book;
        this.bookId = book.getId();
        this.quantity = quantity;
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.price = book.getPrice();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getBookId() {
        return bookId;
    }
    
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
        if (book != null) {
            this.bookId = book.getId();
            this.title = book.getTitle();
            this.author = book.getAuthor();
            this.price = book.getPrice();
        }
    }
    
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    public String getSubtotalString() {
        return "€" + getSubtotal().toString();
    }
    
    public String getBookTitle() {
        return title;
    }
    
    public String getBookAuthor() {
        return author;
    }
    
    public BigDecimal getBookPrice() {
        return price;
    }
}