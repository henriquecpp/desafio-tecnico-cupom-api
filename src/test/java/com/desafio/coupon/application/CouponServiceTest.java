package com.desafio.coupon.application;

import com.desafio.coupon.api.dto.CreateCouponRequest;
import com.desafio.coupon.api.dto.CouponResponse;
import com.desafio.coupon.domain.Coupon;
import com.desafio.coupon.domain.CouponStatus;
import com.desafio.coupon.infra.CouponEntity;
import com.desafio.coupon.infra.CouponMapper;
import com.desafio.coupon.infra.CouponRepository;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
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

@Epic("Gerenciamento de Cupons")
@Feature("Serviço de Cupom")
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
    @Story("Criar cupom")
    @Description("Deve criar e persistir cupom com sucesso")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Buscar cupom")
    @Description("Deve retornar cupom ao buscar por ID existente")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Buscar cupom")
    @Description("Deve lançar exceção ao buscar cupom com ID inexistente")
    @Severity(SeverityLevel.NORMAL)
    void shouldThrowWhenCouponNotFoundOnGet() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(id));
    }

    @Test
    @Story("Deletar cupom")
    @Description("Deve persistir cupom com status DELETED ao deletar")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Deletar cupom")
    @Description("Deve lançar exceção ao deletar cupom com ID inexistente")
    @Severity(SeverityLevel.NORMAL)
    void shouldThrowWhenCouponNotFoundOnDelete() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
    }
}
