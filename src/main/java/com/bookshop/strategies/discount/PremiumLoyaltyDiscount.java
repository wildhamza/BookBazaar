package com.bookshop.strategies.discount;

import com.bookshop.models.User;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PremiumLoyaltyDiscount implements DiscountStrategy {
    
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.15");
    
    @Override
    public boolean isApplicable(User user) {
        return user != null && user.isPremiumLoyaltyMember();
    }
    
    @Override
    public BigDecimal applyDiscount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountAmount = amount.multiply(DISCOUNT_PERCENTAGE);
        return amount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getDescription() {
        return "Premium Loyalty Discount (15%)";
    }
}