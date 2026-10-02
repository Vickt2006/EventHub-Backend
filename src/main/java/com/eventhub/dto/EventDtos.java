package com.eventhub.dto;

import com.eventhub.entity.EventStatus;
import com.eventhub.entity.EventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class EventDtos {

    private EventDtos() {
    }

    public record Create(
            @NotBlank String title,
            String description,
            @NotNull EventType eventType,
            @NotNull LocalDateTime startTime,
            LocalDateTime endTime,
            @NotBlank String city,
            String language,
            String genre,
            String ageRating,
            String posterUrl,
            String bannerUrl,
            @NotNull @Positive BigDecimal basePrice,
            BigDecimal taxPercent,
            @NotNull Long venueId
    ) {
    }

    public record Status(
            @NotNull EventStatus status
    ) {
    }
}