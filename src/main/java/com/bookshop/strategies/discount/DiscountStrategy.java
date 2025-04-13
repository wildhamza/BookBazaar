package com.bookshop.strategies.discount;

import com.bookshop.models.User;
import java.math.BigDecimal;

public interface DiscountStrategy {
    boolean isApplicable(User user);
    BigDecimal applyDiscount(BigDecimal amount);
    String getDescription();
}