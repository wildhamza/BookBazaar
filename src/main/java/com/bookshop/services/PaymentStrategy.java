package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Interface defining a payment strategy.
 * This is an implementation of the Strategy design pattern.
 */
public interface PaymentStrategy {
    
    /**
     * Processes a payment.
     * 
     * @param user The user making the payment
     * @param amount The payment amount
     * @return true if the payment was successful, false otherwise
     */
    boolean processPayment(User user, BigDecimal amount);
}