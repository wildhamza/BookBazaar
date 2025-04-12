package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Premium member discount strategy - applies a 20% discount for premium loyalty members.
 * A premium loyalty member is a user with at least 10 previous orders.
 * This is a concrete implementation of the DiscountStrategy interface.
 */
public class PremiumMemberDiscount implements DiscountStrategy {
    
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.20"); // 20% discount
    private static final int MIN_ORDER_COUNT = 10;
    
    /**
     * Calculates a discount amount for a purchase.
     * Premium members get a 20% discount.
     * 
     * @param user The user making the purchase
     * @param amount The total purchase amount before discount
     * @return The discount amount
     */
    @Override
    public BigDecimal calculateDiscount(User user, BigDecimal amount) {
        if (!isApplicable(user)) {
            return BigDecimal.ZERO;
        }
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
        return user != null && user.getOrderCount() >= MIN_ORDER_COUNT;
    }
    
    /**
     * Gets a description of the discount.
     * 
     * @return The discount description
     */
    @Override
    public String getDescription() {
        return "20% Premium Member Discount (10+ orders)";
    }
}