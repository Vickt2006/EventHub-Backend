package com.eventhub.config;

import com.eventhub.service.SeatHoldService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SeatHoldCleanupJob {
    private final SeatHoldService service;
    public SeatHoldCleanupJob(SeatHoldService service) { this.service = service; }

    @Scheduled(fixedDelay = 60000)
    public void cleanup() { service.cleanupExpired(LocalDateTime.now()); }
}
