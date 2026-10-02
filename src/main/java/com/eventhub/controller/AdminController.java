package com.eventhub.controller;

import com.eventhub.entity.Booking;
import com.eventhub.entity.BookingStatus;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.PaymentStatus;
import com.eventhub.entity.Role;
import com.eventhub.entity.Venue;

import com.eventhub.repository.BookingRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.repository.VenueRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository users;
    private final EventRepository events;
    private final BookingRepository bookings;
    private final VenueRepository venues;

    public AdminController(
            UserRepository users,
            EventRepository events,
            BookingRepository bookings,
            VenueRepository venues) {

        this.users = users;
        this.events = events;
        this.bookings = bookings;
        this.venues = venues;
    }

    // =========================
    // ADMIN DASHBOARD
    // =========================

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {

        return Map.of(
                "users", users.count(),
                "organizers", users.countByRole(Role.ORGANIZER),
                "events", events.count(),
                "publishedEvents",
                events.countByStatus(EventStatus.PUBLISHED),
                "bookings", bookings.count(),
                "confirmedBookings",
                bookings.countByStatus(BookingStatus.CONFIRMED)
        );
    }

    // =========================
    // GET ALL EVENTS
    // =========================

    @GetMapping("/events")
    public List<Event> allEvents() {
        return events.findAll();
    }

    // =========================
    // GET PENDING EVENTS
    // =========================

    @GetMapping("/events/pending")
    public List<Event> pending() {

        return events.findAll()
                .stream()
                .filter(e ->
                        e.getStatus() == EventStatus.PENDING_APPROVAL)
                .toList();
    }

    // =========================
    // CHANGE EVENT STATUS
    // =========================

    @PatchMapping("/events/{id}/status")
    public Event changeEventStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        Event event = events.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        String status = request.get("status");

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }

        try {
            event.setStatus(
                    EventStatus.valueOf(status.toUpperCase())
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid event status: " + status);
        }

        return events.save(event);
    }

    // =========================
    // EVENT BOOKINGS
    // =========================

    @GetMapping("/events/{id}/bookings")
    public List<Booking> eventBookings(
            @PathVariable Long id) {

        if (!events.existsById(id)) {
            throw new RuntimeException("Event not found");
        }

        return bookings.findByEventId(id);
    }

    // =========================
    // ALL BOOKINGS
    // =========================

    @GetMapping("/bookings")
    public List<Booking> allBookings() {
        return bookings.findAll();
    }

    // =========================
    // BOOKING DETAILS
    // =========================

    @GetMapping("/bookings/{id}")
    public Booking bookingDetails(
            @PathVariable Long id) {

        return bookings.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));
    }

    // =========================
    // CANCEL BOOKING
    // =========================

    @PatchMapping("/bookings/{id}/cancel")
    public Booking cancelBooking(
            @PathVariable Long id) {

        Booking booking = bookings.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException(
                    "Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        if (booking.getPaymentStatus() != null) {
            booking.setPaymentStatus(
                    PaymentStatus.REFUNDED
            );
        }

        return bookings.save(booking);
    }

    // =========================
    // CHANGE BOOKING STATUS
    // =========================

    @PatchMapping("/bookings/{id}/status")
    public Booking changeBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        Booking booking = bookings.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        String status = request.get("status");

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }

        BookingStatus newStatus;

        try {
            newStatus = BookingStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid booking status: " + status);
        }

        booking.setStatus(newStatus);

        // CANCELLED -> REFUNDED
        if (newStatus == BookingStatus.CANCELLED) {

            booking.setCancelledAt(LocalDateTime.now());

            if (booking.getPaymentStatus() != null) {
                booking.setPaymentStatus(
                        PaymentStatus.REFUNDED
                );
            }
        }

        // CONFIRMED -> PAID
        else if (newStatus == BookingStatus.CONFIRMED) {

            booking.setCancelledAt(null);

            if (booking.getPaymentStatus() != null) {
                booking.setPaymentStatus(
                        PaymentStatus.PAID
                );
            }
        }

        // COMPLETED -> PAID
        else if (newStatus == BookingStatus.COMPLETED) {

            booking.setCancelledAt(null);

            if (booking.getPaymentStatus() != null) {
                booking.setPaymentStatus(
                        PaymentStatus.PAID
                );
            }
        }

        // OTHER STATUS
        else {
            booking.setCancelledAt(null);
        }

        return bookings.save(booking);
    }

    // =========================
    // ALL VENUES
    // =========================

    @GetMapping("/venues")
    public List<Venue> allVenues() {
        return venues.findAll();
    }

    // =========================
    // CREATE VENUE
    // =========================

    @PostMapping("/venues")
    public Venue createVenue(
            @RequestBody Venue request) {

        Venue venue = new Venue();

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setState(request.getState());
        venue.setCountry(request.getCountry());
        venue.setPincode(request.getPincode());
        venue.setDescription(request.getDescription());
        venue.setImageUrl(request.getImageUrl());
        venue.setTotalSeats(request.getTotalSeats());
        venue.setActive(request.isActive());

        return venues.save(venue);
    }

    // =========================
    // UPDATE VENUE
    // =========================

    @PatchMapping("/venues/{id}")
    public Venue updateVenue(
            @PathVariable Long id,
            @RequestBody Venue request) {

        Venue venue = venues.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Venue not found"));

        if (request.getName() != null) {
            venue.setName(request.getName());
        }

        if (request.getAddress() != null) {
            venue.setAddress(request.getAddress());
        }

        if (request.getCity() != null) {
            venue.setCity(request.getCity());
        }

        if (request.getState() != null) {
            venue.setState(request.getState());
        }

        if (request.getCountry() != null) {
            venue.setCountry(request.getCountry());
        }

        if (request.getPincode() != null) {
            venue.setPincode(request.getPincode());
        }

        if (request.getDescription() != null) {
            venue.setDescription(request.getDescription());
        }

        if (request.getImageUrl() != null) {
            venue.setImageUrl(request.getImageUrl());
        }

        if (request.getTotalSeats() != null) {
            venue.setTotalSeats(request.getTotalSeats());
        }

        venue.setActive(request.isActive());

        return venues.save(venue);
    }

    // =========================
    // ENABLE / DISABLE VENUE
    // =========================

    @PatchMapping("/venues/{id}/active")
    public Venue setVenueActive(
            @PathVariable Long id,
            @RequestParam boolean active) {

        Venue venue = venues.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Venue not found"));

        venue.setActive(active);

        return venues.save(venue);
    }
}