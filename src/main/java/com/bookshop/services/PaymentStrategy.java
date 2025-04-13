package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

public interface PaymentStrategy {
    
    boolean processPayment(User user, BigDecimal amount);
}