package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Interface for the Strategy pattern for applying different discount strategies.
 */
public interface DiscountStrategy {
    
    /**
     * Calculate the discount amount to apply.
     * 
     * @param user The user to calculate discount for
     * @param originalAmount The original amount before discount
     * @return The discount amount to be subtracted from the original amount
     */
    BigDecimal calculateDiscount(User user, BigDecimal originalAmount);
    
    /**
     * Get a description of the discount.
     * 
     * @return A description of the discount for display purposes
     */
    String getDescription();
}