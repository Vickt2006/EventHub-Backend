package com.eventhub.controller;

import com.eventhub.dto.BookingDtos.Create;
import com.eventhub.entity.Booking;
import com.eventhub.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }

    @PostMapping
    public Booking book(@Valid @RequestBody Create request, Authentication authentication) {
        return service.book(request, authentication.getName());
    }

    @GetMapping("/mine")
    public List<Booking> mine(Authentication authentication) { return service.mine(authentication.getName()); }

    @GetMapping("/{code}")
    public Booking get(@PathVariable String code, Authentication authentication) {
        return service.get(code, authentication.getName());
    }

    @PostMapping("/{code}/cancel")
    public Booking cancel(@PathVariable String code, Authentication authentication) {
        return service.cancel(code, authentication.getName());
    }
}
