package com.eventhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public final class ReviewDtos {
    private ReviewDtos() {}
    public record Create(@Min(1) @Max(5) int rating, String comment) {}
}
