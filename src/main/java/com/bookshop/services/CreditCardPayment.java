package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Concrete implementation of PaymentStrategy for credit card payments.
 * Strategy pattern example.
 */
public class CreditCardPayment implements PaymentStrategy {
    
    @Override
    public boolean processPayment(User user, BigDecimal amount) {
        // In a real application, this would connect to a payment processor API
        // For this implementation, we simulate a successful payment process
        System.out.println("Processing credit card payment for " + user.getUsername());
        System.out.println("Amount: $" + amount);
        
        try {
            // Simulate processing time and verification
            Thread.sleep(1000);
            
            // Always return true for successful payment in this implementation
            // Real implementation would check card validity, funds, etc.
            return true;
        } catch (InterruptedException e) {
            System.err.println("Payment processing interrupted: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getType() {
        return "Credit Card";
    }
}
