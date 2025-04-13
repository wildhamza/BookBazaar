package com.bookshop.services;

import com.bookshop.models.User;
import java.math.BigDecimal;

public class PayPalPayment implements PaymentStrategy {
    
    private String email;
    private String password;
    
    public PayPalPayment(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    @Override
    public boolean processPayment(User user, BigDecimal amount) {
        System.out.println("Processing PayPal payment of " + amount + " for " + user.getUsername());
        return true;
    }
}