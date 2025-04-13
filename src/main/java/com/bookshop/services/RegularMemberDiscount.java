package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

public class RegularMemberDiscount implements DiscountStrategy {
    
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.10");
    private static final int MIN_ORDER_COUNT = 5;
    private static final int MAX_ORDER_COUNT = 9;
    
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal amount) {
        return amount.multiply(DISCOUNT_PERCENTAGE);
    }
    
    @Override
    public boolean isApplicable(User user) {
        return user != null && 
               user.getOrderCount() >= MIN_ORDER_COUNT && 
               user.getOrderCount() <= MAX_ORDER_COUNT;
    }
    
    @Override
    public String getDescription() {
        return "10% Regular Member Discount";
    }
}