package com.eventhub.controller;

import com.eventhub.dto.EventDtos.Create;
import com.eventhub.dto.EventDtos.Status;
import com.eventhub.dto.ReviewDtos;

import com.eventhub.entity.Booking;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventType;
import com.eventhub.entity.Review;
import com.eventhub.entity.Seat;

import com.eventhub.service.BookingService;
import com.eventhub.service.EventService;
import com.eventhub.service.FavoriteService;
import com.eventhub.service.ReviewService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;

    public EventController(
            EventService eventService,
            BookingService bookingService,
            ReviewService reviewService,
            FavoriteService favoriteService) {

        this.eventService = eventService;
        this.bookingService = bookingService;
        this.reviewService = reviewService;
        this.favoriteService = favoriteService;
    }

    // =========================================================
    // PUBLIC EVENTS
    // =========================================================

    @GetMapping("/public")
    public Page<Event> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        int safeSize = Math.min(Math.max(size, 1), 50);

        return eventService.search(
                city,
                type,
                q,
                PageRequest.of(
                        Math.max(page, 0),
                        safeSize,
                        Sort.by("startTime").ascending()
                )
        );
    }

    // =========================================================
    // ORGANIZER - MY EVENTS
    // =========================================================

    @GetMapping("/organizer/my-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<Event> myEvents(
            Authentication authentication) {

        return eventService.myEvents(
                authentication.getName()
        );
    }

    // =========================================================
    // ORGANIZER - DASHBOARD
    // =========================================================

    @GetMapping("/organizer/dashboard")
    @PreAuthorize("hasRole('ORGANIZER')")
    public Map<String, Long> organizerDashboard(
            Authentication authentication) {

        return eventService.organizerDashboard(
                authentication.getName()
        );
    }

    // =========================================================
    // ORGANIZER - BOOKING STATISTICS
    // =========================================================

    @GetMapping("/organizer/booking-stats")
    @PreAuthorize("hasRole('ORGANIZER')")
    public Map<String, Object> organizerBookingStats(
            Authentication authentication) {

        return eventService.organizerBookingStats(
                authentication.getName()
        );
    }

    // =========================================================
    // ORGANIZER - EVENT PERFORMANCE
    // =========================================================

    @GetMapping("/organizer/event-performance")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<Map<String, Object>> organizerEventPerformance(
            Authentication authentication) {

        return eventService.organizerEventPerformance(
                authentication.getName()
        );
    }

    // =========================================================
    // ORGANIZER - EVENT BOOKINGS
    // =========================================================

    @GetMapping("/{id}/bookings")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<Booking> eventBookings(
            @PathVariable Long id,
            Authentication authentication) {

        return bookingService.organizerEventBookings(
                id,
                authentication.getName()
        );
    }

    // =========================================================
    // ORGANIZER - EVENT BOOKING SUMMARY
    // =========================================================

    @GetMapping("/{id}/booking-summary")
    @PreAuthorize("hasRole('ORGANIZER')")
    public Map<String, Object> eventBookingSummary(
            @PathVariable Long id,
            Authentication authentication) {

        return bookingService.organizerEventBookingSummary(
                id,
                authentication.getName()
        );
    }

    // =========================================================
    // GET EVENT
    // =========================================================

    @GetMapping("/{id}")
    public Event get(
            @PathVariable Long id) {

        return eventService.get(id);
    }

    // =========================================================
    // CREATE EVENT
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public Event create(
            @Valid @RequestBody Create request,
            Authentication authentication) {

        return eventService.create(
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public Event update(
            @PathVariable Long id,
            @Valid @RequestBody Create request,
            Authentication authentication) {

        return eventService.update(
                id,
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // CHANGE EVENT STATUS
    // ADMIN -> ANY STATUS
    // ORGANIZER -> ONLY CANCEL OWN EVENT
    // =========================================================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Event status(
            @PathVariable Long id,
            @Valid @RequestBody Status request,
            Authentication authentication) {

        return eventService.changeStatus(
                id,
                request.status(),
                authentication.getName()
        );
    }

    // =========================================================
    // EVENT SEATS
    // =========================================================

    @GetMapping("/{id}/seats")
    public List<Seat> seats(
            @PathVariable Long id) {

        return bookingService.seats(id);
    }

    // =========================================================
    // EVENT REVIEWS
    // =========================================================

    @GetMapping("/{id}/reviews")
    public Map<String, Object> reviews(
            @PathVariable Long id) {

        return Map.of(
                "average", reviewService.average(id),
                "reviews", reviewService.list(id)
        );
    }

    // =========================================================
    // ADD REVIEW
    // =========================================================

    @PostMapping("/{id}/reviews")
    public Review review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDtos.Create request,
            Authentication authentication) {

        return reviewService.add(
                id,
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // FAVORITE EVENT
    // =========================================================

    @PostMapping("/{id}/favorite")
    public Map<String, String> favorite(
            @PathVariable Long id,
            Authentication authentication) {

        return Map.of(
                "result",
                favoriteService.toggle(
                        id,
                        authentication.getName()
                )
        );
    }
}