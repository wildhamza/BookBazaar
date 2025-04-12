package com.bookshop.services;

import com.bookshop.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the loyalty discount system.
 */
public class LoyaltyDiscountTest {
    
    private DiscountService discountService;
    private User standardUser;
    private User regularMember;
    private User premiumMember;
    
    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
        
        // Set up test users
        standardUser = new User();
        standardUser.setOrderCount(2); // Standard customer (< 5 orders)
        
        regularMember = new User();
        regularMember.setOrderCount(7); // Regular member (5+ orders)
        
        premiumMember = new User();
        premiumMember.setOrderCount(12); // Premium member (10+ orders)
    }
    
    @Test
    void testNoDiscountForStandardUser() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal discountedPrice = discountService.calculateDiscountedPrice(originalPrice, standardUser);
        
        // Standard user should get no discount
        assertEquals(originalPrice, discountedPrice);
    }
    
    @Test
    void testRegularMemberDiscount() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal expectedPrice = new BigDecimal("90.00"); // 10% discount
        BigDecimal discountedPrice = discountService.calculateDiscountedPrice(originalPrice, regularMember);
        
        // Regular member should get 10% discount
        assertEquals(0, expectedPrice.compareTo(discountedPrice),
                "Regular member should receive a 10% discount");
    }
    
    @Test
    void testPremiumMemberDiscount() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal expectedPrice = new BigDecimal("85.00"); // 15% discount
        BigDecimal discountedPrice = discountService.calculateDiscountedPrice(originalPrice, premiumMember);
        
        // Premium member should get 15% discount
        assertEquals(0, expectedPrice.compareTo(discountedPrice),
                "Premium member should receive a 15% discount");
    }
    
    @Test
    void testDiscountStrategySelection() {
        // Standard user should get NoDiscount strategy
        DiscountStrategy strategy = discountService.getDiscountStrategy(standardUser);
        assertTrue(strategy instanceof NoDiscount);
        
        // Regular member should get RegularMemberDiscount strategy
        strategy = discountService.getDiscountStrategy(regularMember);
        assertTrue(strategy instanceof RegularMemberDiscount);
        
        // Premium member should get PremiumMemberDiscount strategy
        strategy = discountService.getDiscountStrategy(premiumMember);
        assertTrue(strategy instanceof PremiumMemberDiscount);
    }
    
    @Test
    void testDiscountStrategyCalculations() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        
        // Test NoDiscount strategy directly
        DiscountStrategy noDiscount = new NoDiscount();
        assertEquals(0, originalPrice.compareTo(noDiscount.calculateDiscount(null, originalPrice)));
        
        // Test RegularMemberDiscount strategy directly
        DiscountStrategy regularDiscount = new RegularMemberDiscount();
        BigDecimal regularExpected = new BigDecimal("90.00");
        assertEquals(0, regularExpected.compareTo(regularDiscount.calculateDiscount(regularMember, originalPrice)));
        
        // Test PremiumMemberDiscount strategy directly
        DiscountStrategy premiumDiscount = new PremiumMemberDiscount();
        BigDecimal premiumExpected = new BigDecimal("85.00");
        assertEquals(0, premiumExpected.compareTo(premiumDiscount.calculateDiscount(premiumMember, originalPrice)));
    }
} 