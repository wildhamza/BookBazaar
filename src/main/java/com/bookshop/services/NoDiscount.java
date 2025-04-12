package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Implementation of the Discount Strategy pattern for users with no discount.
 * Returns zero discount amount regardless of input.
 */
public class NoDiscount implements DiscountStrategy {
    
    /**
     * Calculates a discount amount, which is always zero for this strategy.
     * 
     * @param user The user making the purchase
     * @param amount The total purchase amount before discount
     * @return Zero discount amount
     */
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal amount) {
        return BigDecimal.ZERO;
    }
    
    /**
     * Determines if the discount is applicable to a user.
     * This strategy is applicable to all users (but provides no discount).
     * 
     * @param user The user to check
     * @return Always true
     */
    @Override
    public boolean isApplicable(User user) {
        return true; // This is the fallback strategy
    }
    
    /**
     * Gets a description of the discount.
     * 
     * @return The discount description
     */
    @Override
    public String getDescription() {
        return "No Discount";
    }
}