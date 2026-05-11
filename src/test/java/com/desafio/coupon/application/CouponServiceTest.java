package com.desafio.coupon.application;

import com.desafio.coupon.api.dto.CreateCouponRequest;
import com.desafio.coupon.api.dto.CouponResponse;
import com.desafio.coupon.domain.Coupon;
import com.desafio.coupon.domain.CouponStatus;
import com.desafio.coupon.infra.CouponEntity;
import com.desafio.coupon.infra.CouponMapper;
import com.desafio.coupon.infra.CouponRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository repository;

    @Mock
    private CouponMapper mapper;

    @InjectMocks
    private CouponService service;

    private final Instant futureDate = Instant.now().plus(10, ChronoUnit.DAYS);

    @Test
    void shouldCreateCouponSuccessfully() {
        CreateCouponRequest request = new CreateCouponRequest(
                "ABC123", "description", new BigDecimal("0.8"), futureDate, false
        );

        CouponEntity entity = new CouponEntity();
        CouponResponse response = new CouponResponse(
                UUID.randomUUID(), "ABC123", "description",
                new BigDecimal("0.8"), futureDate, CouponStatus.ACTIVE, false, false
        );

        when(mapper.toEntity(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(response);

        CouponResponse result = service.create(request);

        assertNotNull(result);
        assertEquals("ABC123", result.code());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void shouldFindCouponByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        CouponEntity entity = new CouponEntity();
        CouponResponse response = new CouponResponse(
                id, "ABC123", "description",
                new BigDecimal("0.8"), futureDate, CouponStatus.ACTIVE, false, false
        );

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(
                Coupon.restore(
                        id, "ABC123", "description",
                        new BigDecimal("0.8"), futureDate, false, false, CouponStatus.ACTIVE
                )
        );
        when(mapper.toResponse(any())).thenReturn(response);

        CouponResponse result = service.findById(id);

        assertNotNull(result);
        assertEquals(id, result.id());
    }

    @Test
    void shouldThrowWhenCouponNotFoundOnGet() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void shouldDeleteCouponSuccessfully() {
        UUID id = UUID.randomUUID();
        CouponEntity entity = new CouponEntity();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(
                Coupon.restore(
                        id, "ABC123", "description",
                        new BigDecimal("0.8"), futureDate, false, false, CouponStatus.ACTIVE
                )
        );
        when(mapper.toEntity(any())).thenReturn(entity);

        service.delete(id);

        verify(repository, times(1)).save(entity);
    }

    @Test
    void shouldThrowWhenCouponNotFoundOnDelete() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
    }
}