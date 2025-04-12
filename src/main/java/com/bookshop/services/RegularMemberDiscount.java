package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Implementation of the Discount Strategy pattern for regular members.
 * Provides a 10% discount for users with 5 or more orders but less than 10.
 */
public class RegularMemberDiscount implements DiscountStrategy {
    
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.10"); // 10% discount
    private static final int MIN_ORDER_COUNT = 5;
    private static final int MAX_ORDER_COUNT = 9; // Less than 10 (Premium members start at 10)
    
    /**
     * Calculates a discount amount for a purchase.
     * Regular members get a 10% discount.
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
     * Regular member discount applies to users with at least 5 previous orders.
     * 
     * @param user The user to check
     * @return true if the discount is applicable, false otherwise
     */
    @Override
    public boolean isApplicable(User user) {
        return user != null && 
               user.getOrderCount() >= MIN_ORDER_COUNT && 
               user.getOrderCount() <= MAX_ORDER_COUNT;
    }
    
    /**
     * Gets a description of the discount.
     * 
     * @return The discount description
     */
    @Override
    public String getDescription() {
        return "10% Regular Member Discount";
    }
}