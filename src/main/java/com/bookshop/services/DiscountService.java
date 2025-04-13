package com.bookshop.services;

import com.bookshop.models.User;

import java.math.BigDecimal; 

public class DiscountService {
    
    public DiscountStrategy getDiscountStrategy(User user) {
        if (user == null) {
            return new NoDiscount();
        }
        
        if (user.getOrderCount() >= 10) {
            return new PremiumMemberDiscount();
        } else if (user.getOrderCount() >= 5) {
            return new RegularMemberDiscount();
        } else {
            return new NoDiscount();
        }
    }
    
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, User user) {
        DiscountStrategy strategy = getDiscountStrategy(user);
        BigDecimal discountAmount = strategy.calculateDiscount(user, originalPrice);
        return originalPrice.subtract(discountAmount);
    }
    
    public String getDiscountDescription(User user) {
        return getDiscountStrategy(user).getDescription();
    }
}