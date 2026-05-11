package com.desafio.coupon.api;

import com.desafio.coupon.domain.CouponStatus;
import com.desafio.coupon.infra.CouponEntity;
import com.desafio.coupon.infra.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    // ─── POST ───────────────────────────────────────────

    @Test
    void shouldCreateCouponSuccessfully() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "code": "ABC123",
                        "description": "Test coupon",
                        "discountValue": 0.8,
                        "expirationDate": "2030-01-01T00:00:00Z",
                        "published": false
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.redeemed").value(false));
    }

    @Test
    void shouldSanitizeCodeOnCreate() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "code": "AB-C12#3",
                        "description": "Test coupon",
                        "discountValue": 0.8,
                        "expirationDate": "2030-01-01T00:00:00Z",
                        "published": false
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    void shouldRejectDiscountValueBelowMinimum() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "code": "ABC123",
                        "description": "Test coupon",
                        "discountValue": 0.3,
                        "expirationDate": "2030-01-01T00:00:00Z",
                        "published": false
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectExpirationDateInThePast() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "code": "ABC123",
                        "description": "Test coupon",
                        "discountValue": 0.8,
                        "expirationDate": "2020-01-01T00:00:00Z",
                        "published": false
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateCouponAsPublished() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "code": "ABC123",
                        "description": "Test coupon",
                        "discountValue": 0.8,
                        "expirationDate": "2030-01-01T00:00:00Z",
                        "published": true
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    void shouldRejectRequestWithMissingCode() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "description": "Test coupon",
                    "discountValue": 0.8,
                    "expirationDate": "2030-01-01T00:00:00Z"
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void shouldRejectRequestWithMissingDescription() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "code": "ABC123",
                    "discountValue": 0.8,
                    "expirationDate": "2030-01-01T00:00:00Z"
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void shouldRejectRequestWithMissingDiscountValue() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "code": "ABC123",
                    "description": "Test coupon",
                    "expirationDate": "2030-01-01T00:00:00Z"
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.discountValue").exists());
    }

    @Test
    void shouldRejectRequestWithMissingExpirationDate() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "code": "ABC123",
                    "description": "Test coupon",
                    "discountValue": 0.8
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.expirationDate").exists());
    }

    @Test
    void shouldRejectRequestWithAllFieldsMissing() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptDiscountValueAboveMinimumBoundary() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "code": "ABC123",
                    "description": "Test coupon",
                    "discountValue": 0.5,
                    "expirationDate": "2030-01-01T00:00:00Z",
                    "published": false
                }
            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.discountValue").value(0.5));
    }

    @Test
    void shouldReturnAllFieldsOnCreate() throws Exception {
        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "code": "ABC123",
                    "description": "Test coupon",
                    "discountValue": 0.8,
                    "expirationDate": "2030-01-01T00:00:00Z",
                    "published": false
                }
            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.description").value("Test coupon"))
                .andExpect(jsonPath("$.discountValue").value(0.8))
                .andExpect(jsonPath("$.expirationDate").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.redeemed").value(false));
    }

    // ─── GET ───────────────────────────────────────────

    @Test
    void shouldFindCouponById() throws Exception {
        CouponEntity entity = buildEntity(CouponStatus.ACTIVE);
        repository.save(entity);

        mockMvc.perform(get("/coupon/{id}", entity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entity.getId().toString()))
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    void shouldReturnNotFoundWhenCouponDoesNotExist() throws Exception {
        mockMvc.perform(get("/coupon/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE ───────────────────────────────────────────

    @Test
    void shouldDeleteCouponSuccessfully() throws Exception {
        CouponEntity entity = buildEntity(CouponStatus.ACTIVE);
        repository.save(entity);

        mockMvc.perform(delete("/coupon/{id}", entity.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentCoupon() throws Exception {
        mockMvc.perform(delete("/coupon/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenDeletingAlreadyDeletedCoupon() throws Exception {
        CouponEntity entity = buildEntity(CouponStatus.DELETED);
        repository.save(entity);

        mockMvc.perform(delete("/coupon/{id}", entity.getId()))
                .andExpect(status().isConflict());
    }

    // ─── Helper ───────────────────────────────────────────

    private CouponEntity buildEntity(CouponStatus status) {
        CouponEntity entity = new CouponEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode("ABC123");
        entity.setDescription("Test coupon");
        entity.setDiscountValue(new BigDecimal("0.8"));
        entity.setExpirationDate(Instant.now().plus(10, ChronoUnit.DAYS));
        entity.setPublished(false);
        entity.setRedeemed(false);
        entity.setStatus(status);
        return entity;
    }
}