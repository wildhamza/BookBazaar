package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Implementation of the Discount Strategy pattern for premium members.
 * Provides a 15% discount for users with 10 or more orders.
 */
public class PremiumMemberDiscount implements DiscountStrategy {
    
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.15"); // 15% discount
    
    /**
     * Calculates a discount amount for a purchase.
     * Premium members get a 15% discount.
     * 
     * @param user The user making the purchase
     * @param amount The total purchase amount before discount
     * @return The discount amount
     */
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal amount) {
        return amount.multiply(DISCOUNT_PERCENTAGE);
    }
    
    /**
     * Determines if the discount is applicable to a user.
     * Premium member discount applies to users with at least 10 previous orders.
     * 
     * @param user The user to check
     * @return true if the discount is applicable, false otherwise
     */
    @Override
    public boolean isApplicable(User user) {
        return user != null && user.getOrderCount() >= 10;
    }
    
    /**
     * Gets a description of the discount.
     * 
     * @return The discount description
     */
    @Override
    public String getDescription() {
        return "15% Premium Member Discount";
    }
}