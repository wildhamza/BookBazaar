package com.bookshop.strategies.discount;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Interface for discount strategies implementing the Strategy design pattern.
 * Different discount implementations can be created by implementing this interface.
 */
public interface DiscountStrategy {
    
    /**
     * Determines if this discount strategy is applicable to the given user.
     * 
     * @param user The user to check
     * @return true if the discount is applicable, false otherwise
     */
    boolean isApplicable(User user);
    
    /**
     * Applies the discount to the given amount.
     * 
     * @param amount The original amount
     * @return The discounted amount
     */
    BigDecimal applyDiscount(BigDecimal amount);
    
    /**
     * Gets a description of this discount.
     * 
     * @return A string describing the discount
     */
    String getDescription();
}