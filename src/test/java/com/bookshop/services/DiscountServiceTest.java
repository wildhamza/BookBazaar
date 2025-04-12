package com.bookshop.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.bookshop.models.User;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Unit tests for the DiscountService class.
 */
public class DiscountServiceTest {
    
    private DiscountService discountService;
    
    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
    }
    
    @Test
    @DisplayName("Test no discount for new customers")
    void testNoDiscountForNewCustomers() {
        User user = new User();
        user.setOrderCount(0);
        user.setRole(User.Role.CUSTOMER);
        
        BigDecimal originalAmount = new BigDecimal("100.00");
        BigDecimal discountedAmount = discountService.applyBestDiscount(user, originalAmount);
        
        assertEquals(originalAmount, discountedAmount, "New customers should not receive a discount");
    }
    
    @Test
    @DisplayName("Test regular customer discount (10% for 5+ orders)")
    void testRegularCustomerDiscount() {
        User user = new User();
        user.setOrderCount(5);
        user.setRole(User.Role.CUSTOMER);
        
        BigDecimal originalAmount = new BigDecimal("100.00");
        BigDecimal expectedDiscountedAmount = new BigDecimal("90.00");
        BigDecimal discountedAmount = discountService.applyBestDiscount(user, originalAmount);
        
        assertEquals(expectedDiscountedAmount, discountedAmount.setScale(2, RoundingMode.HALF_UP),
                    "Regular customers with 5+ orders should receive a 10% discount");
    }
    
    @Test
    @DisplayName("Test premium member discount (15% for 10+ orders)")
    void testPremiumMemberDiscount() {
        User user = new User();
        user.setOrderCount(10);
        user.setRole(User.Role.CUSTOMER);
        
        BigDecimal originalAmount = new BigDecimal("100.00");
        BigDecimal expectedDiscountedAmount = new BigDecimal("85.00");
        BigDecimal discountedAmount = discountService.applyBestDiscount(user, originalAmount);
        
        assertEquals(expectedDiscountedAmount, discountedAmount.setScale(2, RoundingMode.HALF_UP),
                    "Premium members with 10+ orders should receive a 15% discount");
    }
    
    @Test
    @DisplayName("Test no discount for admin users")
    void testNoDiscountForAdminUsers() {
        User user = new User();
        user.setOrderCount(20); // Even with many orders
        user.setRole(User.Role.ADMIN);
        
        BigDecimal originalAmount = new BigDecimal("100.00");
        BigDecimal discountedAmount = discountService.applyBestDiscount(user, originalAmount);
        
        assertEquals(originalAmount, discountedAmount, "Admin users should not receive a discount");
    }
    
    @Test
    @DisplayName("Test best discount is applied when multiple apply")
    void testBestDiscountIsApplied() {
        User user = new User();
        user.setOrderCount(15); // Qualifies for both regular and premium discount
        user.setRole(User.Role.CUSTOMER);
        
        BigDecimal originalAmount = new BigDecimal("100.00");
        BigDecimal regularDiscountAmount = new BigDecimal("90.00");
        BigDecimal premiumDiscountAmount = new BigDecimal("85.00");
        BigDecimal discountedAmount = discountService.applyBestDiscount(user, originalAmount);
        
        assertEquals(premiumDiscountAmount, discountedAmount.setScale(2, RoundingMode.HALF_UP),
                    "The best discount (premium 15%) should be applied when multiple discounts apply");
        assertNotEquals(regularDiscountAmount, discountedAmount.setScale(2, RoundingMode.HALF_UP),
                       "The regular discount (10%) should not be applied when premium discount is available");
    }
    
    // Add more test cases as needed
}