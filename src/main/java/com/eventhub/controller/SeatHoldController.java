package com.eventhub.controller;

import com.eventhub.dto.BookingDtos.Lock;
import com.eventhub.entity.SeatHold;
import com.eventhub.service.SeatHoldService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seat-holds")
public class SeatHoldController {
    private final SeatHoldService service;
    public SeatHoldController(SeatHoldService service) { this.service = service; }

    @PostMapping
    public List<SeatHold> lock(@Valid @RequestBody Lock request, Authentication authentication) {
        return service.lock(request, authentication.getName());
    }

    @GetMapping("/mine")
    public List<SeatHold> mine(Authentication authentication) { return service.mine(authentication.getName()); }

    @DeleteMapping("/{id}")
    public void release(@PathVariable Long id, Authentication authentication) { service.release(id, authentication.getName()); }
}
