package com.desafio.coupon.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    private final Instant futureDate = Instant.now().plus(10, ChronoUnit.DAYS);

    @Test
    void shouldCreateCouponSuccessfully() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.8"), futureDate, false);

        assertNotNull(coupon.getId());
        assertEquals("ABC123", coupon.getCode());
        assertEquals(CouponStatus.ACTIVE, coupon.getStatus());
        assertFalse(coupon.isRedeemed());
    }

    @Test
    void shouldSanitizeSpecialCharactersFromCode() {
        Coupon coupon = Coupon.create("AB-C12#3", "description", new BigDecimal("0.8"), futureDate, false);

        assertEquals("ABC123", coupon.getCode());
    }

    @Test
    void shouldThrowWhenCodeHasLessThan6AlphanumericCharacters() {
        assertThrows(IllegalArgumentException.class, () ->
                Coupon.create("AB-C1#", "description", new BigDecimal("0.8"), futureDate, false)
        );
    }

    @Test
    void shouldThrowWhenCodeHasMoreThan6AlphanumericCharacters() {
        assertThrows(IllegalArgumentException.class, () ->
                Coupon.create("ABC1234", "description", new BigDecimal("0.8"), futureDate, false)
        );
    }

    @Test
    void shouldThrowWhenDiscountValueIsBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () ->
                Coupon.create("ABC123", "description", new BigDecimal("0.3"), futureDate, false)
        );
    }

    @Test
    void shouldAcceptDiscountValueAtMinimumBoundary() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.5"), futureDate, false);

        assertEquals(new BigDecimal("0.5"), coupon.getDiscountValue());
    }

    @Test
    void shouldThrowWhenExpirationDateIsInThePast() {
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);

        assertThrows(IllegalArgumentException.class, () ->
                Coupon.create("ABC123", "description", new BigDecimal("0.8"), pastDate, false)
        );
    }

    @Test
    void shouldCreateCouponAsPublished() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.8"), futureDate, true);

        assertTrue(coupon.isPublished());
    }

    @Test
    void shouldDeleteCouponSuccessfully() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.8"), futureDate, false);
        coupon.delete();

        assertEquals(CouponStatus.DELETED, coupon.getStatus());
    }

    @Test
    void shouldThrowWhenDeletingAlreadyDeletedCoupon() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.8"), futureDate, false);
        coupon.delete();

        assertThrows(IllegalStateException.class, coupon::delete);
    }
}