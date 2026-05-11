package com.desafio.coupon.application;

import com.desafio.coupon.api.dto.CreateCouponRequest;
import com.desafio.coupon.api.dto.CouponResponse;
import com.desafio.coupon.domain.Coupon;
import com.desafio.coupon.infra.CouponEntity;
import com.desafio.coupon.infra.CouponMapper;
import com.desafio.coupon.infra.CouponRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository repository;
    private final CouponMapper mapper;

    public CouponResponse create(CreateCouponRequest request) {
        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );

        repository.save(mapper.toEntity(coupon));

        return mapper.toResponse(coupon);
    }

    public CouponResponse findById(UUID id) {
        CouponEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found"));

        return mapper.toResponse(mapper.toDomain(entity));
    }

    public void delete(UUID id) {
        CouponEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found"));

        Coupon coupon = mapper.toDomain(entity);
        coupon.delete();

        repository.save(mapper.toEntity(coupon));
    }
}