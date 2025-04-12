package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete implementation of DiscountStrategy for premium members.
 * Premium members (with 10 or more orders) receive a 15% discount.
 */
public class PremiumMemberDiscount implements DiscountStrategy {
    
    // The discount percentage (15%)
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.15");
    
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal originalAmount) {
        // Apply discount if user is a premium member
        if (!user.isAdmin() && user.isPremiumMember()) {
            return originalAmount.multiply(DISCOUNT_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        }
        
        // No discount
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getDescription() {
        return "Premium Member Discount (15% off for premium members)";
    }
}