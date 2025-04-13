package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

public class PremiumMemberDiscount implements DiscountStrategy {
    
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.15");
    
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal amount) {
        return amount.multiply(DISCOUNT_PERCENTAGE);
    }
    
    @Override
    public boolean isApplicable(User user) {
        return user != null && user.getOrderCount() >= 10;
    }
    
    @Override
    public String getDescription() {
        return "15% Premium Member Discount";
    }
}