package com.desafio.coupon.domain;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Gerenciamento de Cupons")
@Feature("Domínio do Cupom")
class CouponTest {

    private final Instant futureDate = Instant.now().plus(10, ChronoUnit.DAYS);

    @Test
    @Story("Criar cupom")
    @Description("Deve criar cupom com dados válidos")
    @Severity(SeverityLevel.CRITICAL)
    void shouldCreateCouponSuccessfully() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.8"), futureDate, false);

        assertNotNull(coupon.getId());
        assertEquals("ABC123", coupon.getCode());
        assertEquals(CouponStatus.ACTIVE, coupon.getStatus());
        assertFalse(coupon.isRedeemed());
    }

    @Test
    @Story("Criar cupom")
    @Description("Deve remover caracteres especiais do code antes de salvar")
    @Severity(SeverityLevel.CRITICAL)
    void shouldSanitizeSpecialCharactersFromCode() {
        Coupon coupon = Coupon.create("AB-C12#3", "description", new BigDecimal("0.8"), futureDate, false);

        assertEquals("ABC123", coupon.getCode());
    }

    @Test
    @Story("Criar cupom")
    @Description("Deve lançar exceção quando code tiver menos de 6 caracteres alfanuméricos após sanitização")
    @Severity(SeverityLevel.CRITICAL)
    void shouldThrowWhenCodeHasLessThan6AlphanumericCharacters() {
        assertThrows(IllegalArgumentException.class, () ->
                Coupon.create("AB-C1#", "description", new BigDecimal("0.8"), futureDate, false)
        );
    }

    @Test
    @Story("Criar cupom")
    @Description("Deve lançar exceção quando code tiver mais de 6 caracteres alfanuméricos")
    @Severity(SeverityLevel.CRITICAL)
    void shouldThrowWhenCodeHasMoreThan6AlphanumericCharacters() {
        assertThrows(IllegalArgumentException.class, () ->
                Coupon.create("ABC1234", "description", new BigDecimal("0.8"), futureDate, false)
        );
    }

    @Test
    @Story("Criar cupom")
    @Description("Deve lançar exceção quando discountValue for menor que 0.5")
    @Severity(SeverityLevel.CRITICAL)
    void shouldThrowWhenDiscountValueIsBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () ->
                Coupon.create("ABC123", "description", new BigDecimal("0.3"), futureDate, false)
        );
    }

    @Test
    @Story("Criar cupom")
    @Description("Deve aceitar discountValue igual a 0.5 (valor mínimo permitido)")
    @Severity(SeverityLevel.NORMAL)
    void shouldAcceptDiscountValueAtMinimumBoundary() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.5"), futureDate, false);

        assertEquals(new BigDecimal("0.5"), coupon.getDiscountValue());
    }

    @Test
    @Story("Criar cupom")
    @Description("Deve lançar exceção quando expirationDate estiver no passado")
    @Severity(SeverityLevel.CRITICAL)
    void shouldThrowWhenExpirationDateIsInThePast() {
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);

        assertThrows(IllegalArgumentException.class, () ->
                Coupon.create("ABC123", "description", new BigDecimal("0.8"), pastDate, false)
        );
    }

    @Test
    @Story("Criar cupom")
    @Description("Deve criar cupom com published igual a true")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateCouponAsPublished() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.8"), futureDate, true);

        assertTrue(coupon.isPublished());
    }

    @Test
    @Story("Deletar cupom")
    @Description("Deve alterar status para DELETED ao deletar cupom")
    @Severity(SeverityLevel.CRITICAL)
    void shouldDeleteCouponSuccessfully() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.8"), futureDate, false);
        coupon.delete();

        assertEquals(CouponStatus.DELETED, coupon.getStatus());
    }

    @Test
    @Story("Deletar cupom")
    @Description("Deve lançar exceção ao tentar deletar cupom já deletado")
    @Severity(SeverityLevel.CRITICAL)
    void shouldThrowWhenDeletingAlreadyDeletedCoupon() {
        Coupon coupon = Coupon.create("ABC123", "description", new BigDecimal("0.8"), futureDate, false);
        coupon.delete();

        assertThrows(IllegalStateException.class, coupon::delete);
    }
}
