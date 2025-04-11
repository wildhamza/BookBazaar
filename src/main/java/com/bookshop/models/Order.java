package com.bookshop.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing an order.
 */
public class Order {
    public enum Status {
        PENDING,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
    
    private int id;
    private int userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private Status status;
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItem> orderItems;
    
    // Default constructor
    public Order() {
        this.orderItems = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.status = Status.PENDING;
    }
    
    // Constructor with user ID
    public Order(int userId) {
        this();
        this.userId = userId;
    }
    
    // Constructor with all fields
    public Order(int id, int userId, LocalDateTime orderDate, BigDecimal totalAmount, 
                Status status, String shippingAddress, String paymentMethod) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.orderItems = new ArrayList<>();
    }

    // Getters and setters
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

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        recalculateTotal();
    }
    
    // Calculate total from order items
    public void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            total = total.add(item.getSubtotal());
        }
        this.totalAmount = total;
    }
    
    // Helper method for formatting date
    public String getFormattedDate() {
        return orderDate.toString().substring(0, 16).replace('T', ' ');
    }
}
