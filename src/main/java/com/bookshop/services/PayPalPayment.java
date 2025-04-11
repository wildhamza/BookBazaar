package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Concrete implementation of PaymentStrategy for PayPal payments.
 * Strategy pattern example.
 */
public class PayPalPayment implements PaymentStrategy {
    
    @Override
    public boolean processPayment(User user, BigDecimal amount) {
        // In a real application, this would connect to PayPal's API
        // For this implementation, we simulate a successful payment process
        System.out.println("Processing PayPal payment for " + user.getUsername());
        System.out.println("Amount: $" + amount);
        
        try {
            // Simulate processing time and verification
            Thread.sleep(1000);
            
            // Always return true for successful payment in this implementation
            // Real implementation would check PayPal account, funds, etc.
            return true;
        } catch (InterruptedException e) {
            System.err.println("Payment processing interrupted: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getType() {
        return "PayPal";
    }
}
