package com.desafio.coupon.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Coupon {

    private UUID id;
    private String code;
    private String description;
    private BigDecimal discountValue;
    private Instant expirationDate;
    private boolean published;
    private boolean redeemed;
    private CouponStatus status;

    private Coupon() {}

    public static Coupon create(
            String rawCode,
            String description,
            BigDecimal discountValue,
            Instant expirationDate,
            boolean published
    ) {
        String sanitizedCode = sanitizeCode(rawCode);

        if (sanitizedCode.length() != 6) {
            throw new IllegalArgumentException(
                    "Code must have exactly 6 alphanumeric characters after removing special characters"
            );
        }

        if (discountValue.compareTo(new BigDecimal("0.5")) < 0) {
            throw new IllegalArgumentException(
                    "Discount value must be at least 0.5"
            );
        }

        if (expirationDate.isBefore(Instant.now())) {
            throw new IllegalArgumentException(
                    "Expiration date cannot be in the past"
            );
        }

        Coupon coupon = new Coupon();
        coupon.id = UUID.randomUUID();
        coupon.code = sanitizedCode;
        coupon.description = description;
        coupon.discountValue = discountValue;
        coupon.expirationDate = expirationDate;
        coupon.published = published;
        coupon.redeemed = false;
        coupon.status = CouponStatus.ACTIVE;

        return coupon;
    }

    public void delete() {
        if (this.status == CouponStatus.DELETED) {
            throw new IllegalStateException("Coupon is already deleted");
        }
        this.status = CouponStatus.DELETED;
    }

    private static String sanitizeCode(String rawCode) {
        return rawCode.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static Coupon restore(
            UUID id,
            String code,
            String description,
            BigDecimal discountValue,
            Instant expirationDate,
            boolean published,
            boolean redeemed,
            CouponStatus status
    ) {
        Coupon coupon = new Coupon();
        coupon.id = id;
        coupon.code = code;
        coupon.description = description;
        coupon.discountValue = discountValue;
        coupon.expirationDate = expirationDate;
        coupon.published = published;
        coupon.redeemed = redeemed;
        coupon.status = status;
        return coupon;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public Instant getExpirationDate() { return expirationDate; }
    public boolean isPublished() { return published; }
    public boolean isRedeemed() { return redeemed; }
    public CouponStatus getStatus() { return status; }
}