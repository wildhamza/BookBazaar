package com.bookshop.services;

import com.bookshop.models.User;
import com.bookshop.strategies.discount.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for applying discounts to orders based on various discount strategies.
 * Implements the Strategy design pattern.
 */
public class DiscountService {
    
    private List<DiscountStrategy> discountStrategies;
    
    /**
     * Constructor that initializes available discount strategies.
     */
    public DiscountService() {
        // Initialize the list of discount strategies
        discountStrategies = new ArrayList<>();
        
        // Add available discount strategies
        discountStrategies.add(new RegularLoyaltyDiscount());
        discountStrategies.add(new PremiumLoyaltyDiscount());
        
        // Additional discount strategies can be added here
        // e.g., discountStrategies.add(new HolidayDiscount());
    }
    
    /**
     * Apply the best applicable discount for a user to an amount.
     * 
     * @param user The user to apply discounts for
     * @param amount The original amount to apply discounts to
     * @return The discounted amount (or original amount if no discounts apply)
     */
    public BigDecimal applyBestDiscount(User user, BigDecimal amount) {
        if (user == null || amount == null) {
            return amount;
        }
        
        BigDecimal bestDiscountedAmount = amount;
        
        // Try each discount strategy and find the one that gives the best discount
        for (DiscountStrategy strategy : discountStrategies) {
            if (strategy.isApplicable(user)) {
                BigDecimal discountedAmount = strategy.applyDiscount(amount);
                
                // If this strategy gives a better discount (lower amount), use it
                if (discountedAmount.compareTo(bestDiscountedAmount) < 0) {
                    bestDiscountedAmount = discountedAmount;
                }
            }
        }
        
        return bestDiscountedAmount;
    }
    
    /**
     * Calculate the discount amount (the difference between original and discounted amount).
     * 
     * @param user The user to calculate discount for
     * @param amount The original amount
     * @return The discount amount
     */
    public BigDecimal calculateDiscountAmount(User user, BigDecimal amount) {
        BigDecimal discountedAmount = applyBestDiscount(user, amount);
        return amount.subtract(discountedAmount);
    }
    
    /**
     * Calculate the discount percentage that was applied.
     * 
     * @param user The user to calculate discount percentage for
     * @param amount The original amount
     * @return The discount percentage as a whole number (e.g., 10 for 10%)
     */
    public int calculateDiscountPercentage(User user, BigDecimal amount) {
        BigDecimal discountAmount = calculateDiscountAmount(user, amount);
        
        // Avoid division by zero
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        
        // Calculate percentage
        BigDecimal percentage = discountAmount.multiply(new BigDecimal(100)).divide(amount, 0, BigDecimal.ROUND_HALF_UP);
        return percentage.intValue();
    }
    
    /**
     * Get a description of the applied discount.
     * 
     * @param user The user to get discount description for
     * @return A string describing the discount, or "No discount" if none applied
     */
    public String getDiscountDescription(User user) {
        if (user == null) {
            return "No discount";
        }
        
        for (DiscountStrategy strategy : discountStrategies) {
            if (strategy.isApplicable(user)) {
                return strategy.getDescription();
            }
        }
        
        return "No discount";
    }
    
    /**
     * Add a custom discount strategy.
     * 
     * @param strategy The discount strategy to add
     */
    public void addDiscountStrategy(DiscountStrategy strategy) {
        if (strategy != null) {
            discountStrategies.add(strategy);
        }
    }
}