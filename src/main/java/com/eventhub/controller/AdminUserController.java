package com.eventhub.controller;

import com.eventhub.entity.Booking;
import com.eventhub.entity.BookingStatus;
import com.eventhub.entity.Event;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.BookingRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository users;
    private final EventRepository events;
    private final BookingRepository bookings;

    public AdminUserController(
            UserRepository users,
            EventRepository events,
            BookingRepository bookings) {

        this.users = users;
        this.events = events;
        this.bookings = bookings;
    }

    // =========================
    // GET ALL USERS
    // =========================

    @GetMapping
    public List<User> all() {
        return users.findAll();
    }

    // =========================
    // GET ALL ORGANIZERS
    // =========================

    @GetMapping("/organizers")
    public List<User> allOrganizers() {

        return users.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.ORGANIZER)
                .toList();
    }

    // =========================
    // GET ORGANIZER EVENTS
    // =========================

    @GetMapping("/{id}/events")
    public List<Event> organizerEvents(
            @PathVariable Long id) {

        User organizer = users.findById(id)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        if (organizer.getRole() != Role.ORGANIZER) {
            throw new ApiException("User is not an organizer");
        }

        return events.findByOrganizerId(id);
    }

    // =========================
    // ORGANIZER BOOKING STATS
    // =========================

    @GetMapping("/{id}/booking-stats")
    public Map<String, Object> organizerBookingStats(
            @PathVariable Long id) {

        User organizer = users.findById(id)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        if (organizer.getRole() != Role.ORGANIZER) {
            throw new ApiException("User is not an organizer");
        }

        List<Booking> organizerBookings =
                bookings.findByEventOrganizerId(id);

        long totalBookings = organizerBookings.size();

        long confirmedBookings = organizerBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();

        long cancelledBookings = organizerBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        long totalSeatsSold = organizerBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .mapToLong(b ->
                        b.getSeats() == null
                                ? 0
                                : b.getSeats().size())
                .sum();

        BigDecimal totalRevenue = organizerBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return Map.of(
                "organizerId", id,
                "totalBookings", totalBookings,
                "confirmedBookings", confirmedBookings,
                "cancelledBookings", cancelledBookings,
                "totalSeatsSold", totalSeatsSold,
                "totalRevenue", totalRevenue
        );
    }

    // =========================
    // CHANGE USER ROLE
    // =========================

    @PatchMapping("/{id}/role")
    public User changeRole(
            @PathVariable Long id,
            @RequestParam Role role) {

        User user = users.findById(id)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        user.setRole(role);

        return users.save(user);
    }

    // =========================
    // ENABLE / DISABLE USER
    // =========================

    @PatchMapping("/{id}/enabled")
    public User setEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {

        User user = users.findById(id)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        user.setEnabled(enabled);

        return users.save(user);
    }
}