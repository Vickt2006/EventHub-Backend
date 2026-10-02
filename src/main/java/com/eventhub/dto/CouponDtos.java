package com.eventhub.dto;

import com.eventhub.entity.CouponType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class CouponDtos {
    private CouponDtos() {}
    public record Create(@NotBlank String code, @NotNull CouponType type, @NotNull @Positive BigDecimal value,
                         BigDecimal maxDiscount, BigDecimal minOrderValue, Integer usageLimit,
                         LocalDateTime startsAt, LocalDateTime expiresAt) {}
}
