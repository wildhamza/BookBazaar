package com.bookshop.models;

import java.math.BigDecimal;

public class OrderItem {
    
    private int id;
    private int orderId;
    private int bookId;
    private int quantity;
    private BigDecimal price;
    private Book book;
    
    private String bookTitle;
    private String bookAuthor;
    
    public OrderItem() {
        this.quantity = 1;
        this.price = BigDecimal.ZERO;
    }
    
    public OrderItem(int orderId, int bookId, int quantity, BigDecimal price) {
        this.orderId = orderId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.price = price;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
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
    }
    
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    public String getBookTitle() {
        if (book != null) {
            return book.getTitle();
        }
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getBookAuthor() {
        if (book != null) {
            return book.getAuthor();
        }
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
}