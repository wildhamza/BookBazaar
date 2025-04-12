package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete implementation of DiscountStrategy for regular customers.
 * Customers with 5 or more orders receive a 10% discount.
 */
public class RegularCustomerDiscount implements DiscountStrategy {
    
    // The threshold for qualifying for discount
    private static final int ORDER_COUNT_THRESHOLD = 5;
    
    // The discount percentage (10%)
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.10");
    
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal originalAmount) {
        // Only apply discount if user is not admin and has enough orders
        if (!user.isAdmin() && user.getOrderCount() >= ORDER_COUNT_THRESHOLD) {
            return originalAmount.multiply(DISCOUNT_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        }
        
        // No discount
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getDescription() {
        return "Regular Customer Discount (10% off for 5+ orders)";
    }
}