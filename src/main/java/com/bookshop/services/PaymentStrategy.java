package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Strategy pattern interface for payment processing.
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
    
    /**
     * Gets the payment method type.
     * 
     * @return The payment method type (e.g., "Credit Card", "PayPal")
     */
    String getType();
}
