package com.bookshop.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order in the bookshop system.
 */
public class Order {
    
    public enum Status {
        PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }
    
    private int id;
    private int userId;
    private LocalDateTime orderDate;
    private Status status;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItem> items;
    
    // Constructor
    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = Status.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.finalAmount = BigDecimal.ZERO;
        this.items = new ArrayList<>();
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
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        updateFinalAmount();
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        updateFinalAmount();
    }
    
    public BigDecimal getFinalAmount() {
        return finalAmount;
    }
    
    private void updateFinalAmount() {
        this.finalAmount = this.totalAmount.subtract(this.discountAmount);
        // Ensure final amount is never negative
        if (this.finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.finalAmount = BigDecimal.ZERO;
        }
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
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    /**
     * Adds an item to the order and updates the total amount.
     * 
     * @param item The item to add to the order
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
        
        // Update the total amount
        BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
        this.totalAmount = this.totalAmount.add(itemTotal);
        updateFinalAmount();
    }
    
    /**
     * Removes an item from the order and updates the total amount.
     * 
     * @param itemIndex The index of the item to remove
     */
    public void removeItem(int itemIndex) {
        if (itemIndex >= 0 && itemIndex < this.items.size()) {
            OrderItem item = this.items.get(itemIndex);
            
            // Update the total amount
            BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            this.totalAmount = this.totalAmount.subtract(itemTotal);
            updateFinalAmount();
            
            // Remove the item
            this.items.remove(itemIndex);
        }
    }
    
    /**
     * Applies a discount to the order.
     * 
     * @param discountAmount The amount to discount
     */
    public void applyDiscount(BigDecimal discountAmount) {
        setDiscountAmount(discountAmount);
    }
    
    /**
     * Applies a percentage discount to the order.
     * 
     * @param discountPercentage The percentage to discount (e.g., 10 for 10%)
     */
    public void applyDiscountPercentage(int discountPercentage) {
        BigDecimal percentage = new BigDecimal(discountPercentage).divide(new BigDecimal(100));
        BigDecimal discount = this.totalAmount.multiply(percentage);
        setDiscountAmount(discount);
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderDate=" + orderDate +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", discountAmount=" + discountAmount +
                ", finalAmount=" + finalAmount +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", items=" + items.size() +
                '}';
    }
}