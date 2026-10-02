package com.eventhub.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class BookingDtos {
    private BookingDtos() {}
    public record Create(@NotNull Long eventId, @NotEmpty List<Long> seatIds, String couponCode, String paymentMethod) {}
    public record Lock(@NotNull Long eventId, @NotEmpty List<Long> seatIds) {}
}
