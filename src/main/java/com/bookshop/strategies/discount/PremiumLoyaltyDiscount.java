package com.bookshop.strategies.discount;

import com.bookshop.models.User;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Premium loyalty discount strategy that provides a 15% discount
 * for customers with 10 or more orders.
 * Implements the Strategy design pattern.
 */
public class PremiumLoyaltyDiscount implements DiscountStrategy {
    
    // Discount percentage for premium loyalty members (15%)
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.15");
    
    @Override
    public boolean isApplicable(User user) {
        // Check if the user is eligible for the premium loyalty discount
        return user != null && user.isPremiumLoyaltyMember();
    }
    
    @Override
    public BigDecimal applyDiscount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        
        // Calculate the discounted amount (amount - (amount * discount percentage))
        BigDecimal discountAmount = amount.multiply(DISCOUNT_PERCENTAGE);
        return amount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getDescription() {
        return "Premium Loyalty Discount (15%)";
    }
}