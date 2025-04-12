package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Interface defining a discount strategy.
 * This is an implementation of the Strategy design pattern.
 */
public interface DiscountStrategy {
    
    /**
     * Calculates a discount amount for a purchase.
     * 
     * @param user The user making the purchase
     * @param amount The total purchase amount before discount
     * @return The discount amount
     */
    BigDecimal calculateDiscount(User user, BigDecimal amount);
    
    /**
     * Determines if the discount is applicable to a user.
     * 
     * @param user The user to check
     * @return true if the discount is applicable, false otherwise
     */
    boolean isApplicable(User user);
    
    /**
     * Gets a description of the discount.
     * 
     * @return The discount description
     */
    String getDescription();
}