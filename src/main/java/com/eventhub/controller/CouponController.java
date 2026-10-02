package com.eventhub.controller;

import com.eventhub.dto.CouponDtos.Create;
import com.eventhub.entity.Coupon;
import com.eventhub.repository.CouponRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class CouponController {
    private final CouponRepository repository;
    public CouponController(CouponRepository repository) { this.repository = repository; }

    @PostMapping
    public Coupon create(@Valid @RequestBody Create request) {
        Coupon coupon = new Coupon();
        coupon.setCode(request.code().trim().toUpperCase());
        coupon.setType(request.type()); coupon.setValue(request.value()); coupon.setMaxDiscount(request.maxDiscount());
        coupon.setMinOrderValue(request.minOrderValue() == null ? BigDecimal.ZERO : request.minOrderValue());
        coupon.setUsageLimit(request.usageLimit()); coupon.setStartsAt(request.startsAt()); coupon.setExpiresAt(request.expiresAt());
        return repository.save(coupon);
    }

    @GetMapping
    public List<Coupon> list() { return repository.findAll(); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repository.deleteById(id); }
}
