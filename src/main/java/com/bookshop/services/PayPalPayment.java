package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * PayPal payment strategy.
 * This is a concrete implementation of the PaymentStrategy interface.
 */
public class PayPalPayment implements PaymentStrategy {
    
    private String email;
    private String password;
    
    /**
     * Constructor with payment details.
     * 
     * @param email The PayPal email address
     * @param password The PayPal password
     */
    public PayPalPayment(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    /**
     * Processes a PayPal payment.
     * In a real application, this would connect to the PayPal API.
     * 
     * @param user The user making the payment
     * @param amount The payment amount
     * @return true if the payment was successful, false otherwise
     */
    @Override
    public boolean processPayment(User user, BigDecimal amount) {
        // In a real application, this would call the PayPal API
        // For this demo, we'll just return true to simulate a successful payment
        System.out.println("Processing PayPal payment of " + amount + " for " + user.getUsername());
        return true;
    }
}