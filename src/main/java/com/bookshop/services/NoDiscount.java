package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

public class NoDiscount implements DiscountStrategy {
    
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal amount) {
        return BigDecimal.ZERO;
    }
    
    @Override
    public boolean isApplicable(User user) {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "No Discount";
    }
}