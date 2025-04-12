package com.bookshop.strategies.discount;

import com.bookshop.models.User;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Regular loyalty discount strategy that provides a 10% discount
 * for customers with 5 or more orders.
 * Implements the Strategy design pattern.
 */
public class RegularLoyaltyDiscount implements DiscountStrategy {
    
    // Discount percentage for regular loyalty members (10%)
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.10");
    
    @Override
    public boolean isApplicable(User user) {
        // Check if the user is eligible for the regular loyalty discount
        return user != null && user.isRegularLoyaltyMember();
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
        return "Regular Loyalty Discount (10%)";
    }
}