package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

public class CreditCardPayment implements PaymentStrategy {
    
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    
    public CreditCardPayment(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }
    
    @Override
    public boolean processPayment(User user, BigDecimal amount) {
        System.out.println("Processing credit card payment of " + amount + " for " + user.getUsername());
        return true;
    }
}