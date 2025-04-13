package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

public interface DiscountStrategy {
    
    BigDecimal calculateDiscount(User user, BigDecimal amount);
    
    boolean isApplicable(User user);
    
    String getDescription();
}