package com.bookshop.services;

import com.bookshop.models.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for applying discounts based on user loyalty levels.
 * Implements the Strategy pattern for different discount types.
 */
public class DiscountService {
    
    /**
     * Determines the appropriate discount strategy for a user based on their loyalty level.
     * 
     * @param user The user to determine discount strategy for
     * @return The appropriate discount strategy
     */
    public DiscountStrategy getDiscountStrategy(User user) {
        if (user == null) {
            return new NoDiscount();
        }
        
        if (user.getOrderCount() >= 10) {
            // Premium member (10+ orders)
            return new PremiumMemberDiscount();
        } else if (user.getOrderCount() >= 5) {
            // Regular member (5+ orders)
            return new RegularMemberDiscount();
        } else {
            // Standard customer (no discount)
            return new NoDiscount();
        }
    }
    
    /**
     * Calculates the discounted price for a user based on their loyalty level.
     * 
     * @param originalPrice The original price
     * @param user The user to calculate discount for
     * @return The discounted price
     */
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, User user) {
        DiscountStrategy strategy = getDiscountStrategy(user);
        BigDecimal discountAmount = strategy.calculateDiscount(user, originalPrice);
        return originalPrice.subtract(discountAmount);
    }
    
    /**
     * Gets a human-readable description of the discount applied for a user.
     * 
     * @param user The user to get discount description for
     * @return The discount description
     */
    public String getDiscountDescription(User user) {
        return getDiscountStrategy(user).getDescription();
    }
}