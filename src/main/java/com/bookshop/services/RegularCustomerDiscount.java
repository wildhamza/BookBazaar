package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class RegularCustomerDiscount implements DiscountStrategy {
    
    private static final int ORDER_COUNT_THRESHOLD = 5;
    
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.10");
    
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal originalAmount) {
        if (!user.isAdmin() && user.getOrderCount() >= ORDER_COUNT_THRESHOLD) {
            return originalAmount.multiply(DISCOUNT_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getDescription() {
        return "Regular Customer Discount (10% off for 5+ orders)";
    }

    @Override
    public boolean isApplicable(User user) {
        return !user.isAdmin() && user.getOrderCount() >= ORDER_COUNT_THRESHOLD;
    }
}