package com.bookshop.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing an order in the bookshop system.
 */
public class Order {
    
    /**
     * Enum representing the possible order statuses.
     */
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
    private Status status;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String shippingAddress;
    private BigDecimal discountAmount;
    private List<OrderItem> items;
    
    /**
     * Default constructor.
     */
    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.status = Status.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
    }
    
    /**
     * Constructor with basic order information.
     * 
     * @param userId The user ID
     * @param totalAmount The total amount
     * @param paymentMethod The payment method
     */
    public Order(int userId, BigDecimal totalAmount, String paymentMethod) {
        this();
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }
    
    /**
     * Get the order ID.
     * 
     * @return The order ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set the order ID.
     * 
     * @param id The order ID
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
     * @param userId The user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    /**
     * Get the order date.
     * 
     * @return The order date
     */
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    /**
     * Set the order date.
     * 
     * @param orderDate The order date
     */
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    /**
     * Get the order status.
     * 
     * @return The order status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Set the order status.
     * 
     * @param status The order status
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
    /**
     * Set the order status from a string.
     * 
     * @param statusStr The status string
     */
    public void setStatus(String statusStr) {
        try {
            this.status = Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to PENDING if status is invalid
            this.status = Status.PENDING;
        }
    }
    
    /**
     * Get the total amount.
     * 
     * @return The total amount
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    /**
     * Set the total amount.
     * 
     * @param totalAmount The total amount
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    /**
     * Get the payment method.
     * 
     * @return The payment method
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    /**
     * Set the payment method.
     * 
     * @param paymentMethod The payment method
     */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    /**
     * Get the shipping address.
     * 
     * @return The shipping address
     */
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    /**
     * Set the shipping address.
     * 
     * @param shippingAddress The shipping address
     */
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    /**
     * Get the discount amount.
     * 
     * @return The discount amount
     */
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    /**
     * Set the discount amount.
     * 
     * @param discountAmount The discount amount
     */
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    /**
     * Get the final amount after discount.
     * 
     * @return The final amount
     */
    public BigDecimal getFinalAmount() {
        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }
        
        if (discountAmount == null) {
            return totalAmount;
        }
        
        return totalAmount.subtract(discountAmount);
    }
    
    /**
     * Get the order items.
     * 
     * @return The order items
     */
    public List<OrderItem> getItems() {
        return items;
    }
    
    /**
     * Get the order items (alias for getItems() for compatibility).
     * 
     * @return The order items
     */
    public List<OrderItem> getOrderItems() {
        return items;
    }
    
    /**
     * Set the order items.
     * 
     * @param items The order items
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    /**
     * Add an item to the order.
     * 
     * @param item The item to add
     */
    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
    
    /**
     * Calculate the total of all items in the order.
     * 
     * @return The order total
     */
    public BigDecimal calculateTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}