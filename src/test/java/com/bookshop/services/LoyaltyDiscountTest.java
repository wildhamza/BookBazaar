package com.bookshop.services;

import com.bookshop.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class LoyaltyDiscountTest {
    
    private DiscountService discountService;
    private User standardUser;
    private User regularMember;
    private User premiumMember;
    
    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
        
        standardUser = new User();
        standardUser.setOrderCount(2); 
        
        regularMember = new User();
        regularMember.setOrderCount(7); 
        
        premiumMember = new User();
        premiumMember.setOrderCount(12); 
    }
    
    @Test
    void testNoDiscountForStandardUser() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal discountedPrice = discountService.calculateDiscountedPrice(originalPrice, standardUser);
        
        assertEquals(originalPrice, discountedPrice);
    }
    
    @Test
    void testRegularMemberDiscount() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal expectedPrice = new BigDecimal("90.00"); 
        BigDecimal discountedPrice = discountService.calculateDiscountedPrice(originalPrice, regularMember);
        
        assertEquals(0, expectedPrice.compareTo(discountedPrice),
                "Regular member should receive a 10% discount");
    }
    
    @Test
    void testPremiumMemberDiscount() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal expectedPrice = new BigDecimal("85.00"); 
        BigDecimal discountedPrice = discountService.calculateDiscountedPrice(originalPrice, premiumMember);
        
        assertEquals(0, expectedPrice.compareTo(discountedPrice),
                "Premium member should receive a 15% discount");
    }
    
    @Test
    void testDiscountStrategySelection() {
        DiscountStrategy strategy = discountService.getDiscountStrategy(standardUser);
        assertTrue(strategy instanceof NoDiscount);
        
        strategy = discountService.getDiscountStrategy(regularMember);
        assertTrue(strategy instanceof RegularMemberDiscount);
        
        strategy = discountService.getDiscountStrategy(premiumMember);
        assertTrue(strategy instanceof PremiumMemberDiscount);
    }
    
    @Test
    void testDiscountStrategyCalculations() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        
        DiscountStrategy noDiscount = new NoDiscount();
        assertEquals(0, originalPrice.compareTo(noDiscount.calculateDiscount(null, originalPrice)));
        
        DiscountStrategy regularDiscount = new RegularMemberDiscount();
        BigDecimal regularExpected = new BigDecimal("90.00");
        assertEquals(0, regularExpected.compareTo(regularDiscount.calculateDiscount(regularMember, originalPrice)));
        
        DiscountStrategy premiumDiscount = new PremiumMemberDiscount();
        BigDecimal premiumExpected = new BigDecimal("85.00");
        assertEquals(0, premiumExpected.compareTo(premiumDiscount.calculateDiscount(premiumMember, originalPrice)));
    }
} 