package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

/**
 * Credit card payment strategy.
 * This is a concrete implementation of the PaymentStrategy interface.
 */
public class CreditCardPayment implements PaymentStrategy {
    
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    
    /**
     * Constructor with payment details.
     * 
     * @param cardNumber The credit card number
     * @param expiryDate The expiry date
     * @param cvv The CVV security code
     */
    public CreditCardPayment(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }
    
    /**
     * Processes a credit card payment.
     * In a real application, this would connect to a payment processor API.
     * 
     * @param user The user making the payment
     * @param amount The payment amount
     * @return true if the payment was successful, false otherwise
     */
    @Override
    public boolean processPayment(User user, BigDecimal amount) {
        // In a real application, this would call a payment processor API
        // For this demo, we'll just return true to simulate a successful payment
        System.out.println("Processing credit card payment of " + amount + " for " + user.getUsername());
        return true;
    }
}