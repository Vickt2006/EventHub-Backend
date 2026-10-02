package com.eventhub.service;

import com.eventhub.dto.EventDtos.Create;
import com.eventhub.entity.Booking;
import com.eventhub.entity.BookingStatus;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.EventType;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.entity.Venue;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.BookingRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.repository.VenueRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    private final EventRepository events;
    private final VenueRepository venues;
    private final UserRepository users;
    private final BookingRepository bookings;

    public EventService(
            EventRepository events,
            VenueRepository venues,
            UserRepository users,
            BookingRepository bookings) {

        this.events = events;
        this.venues = venues;
        this.users = users;
        this.bookings = bookings;
    }

    // =========================================================
    // CREATE EVENT
    // =========================================================

    public Event create(Create request, String email) {

        User organizer = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        Venue venue = venues.findById(request.venueId())
                .orElseThrow(() ->
                        new ApiException("Venue not found"));

        Event event = new Event();

        event.setTitle(request.title().trim());
        event.setDescription(request.description());
        event.setEventType(request.eventType());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setCity(request.city());
        event.setLanguage(request.language());
        event.setGenre(request.genre());
        event.setAgeRating(request.ageRating());
        event.setPosterUrl(request.posterUrl());
        event.setBannerUrl(request.bannerUrl());
        event.setBasePrice(request.basePrice());

        event.setTaxPercent(
                request.taxPercent() == null
                        ? BigDecimal.ZERO
                        : request.taxPercent()
        );

        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setCapacity(venue.getTotalSeats());

        // ADMIN -> PUBLISHED
        // ORGANIZER -> PENDING_APPROVAL

        event.setStatus(
                organizer.getRole() == Role.ADMIN
                        ? EventStatus.PUBLISHED
                        : EventStatus.PENDING_APPROVAL
        );

        return events.save(event);
    }

    // =========================================================
    // UPDATE EVENT
    // =========================================================

    public Event update(
            Long id,
            Create request,
            String email) {

        Event event = get(id);

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        // ADMIN can update any event
        // ORGANIZER can update only own event

        if (user.getRole() != Role.ADMIN) {

            if (event.getOrganizer() == null ||
                    !event.getOrganizer()
                            .getId()
                            .equals(user.getId())) {

                throw new ApiException(
                        "You are not allowed to update this event"
                );
            }
        }

        Venue venue = venues.findById(request.venueId())
                .orElseThrow(() ->
                        new ApiException("Venue not found"));

        event.setTitle(request.title().trim());
        event.setDescription(request.description());
        event.setEventType(request.eventType());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setCity(request.city());
        event.setLanguage(request.language());
        event.setGenre(request.genre());
        event.setAgeRating(request.ageRating());
        event.setPosterUrl(request.posterUrl());
        event.setBannerUrl(request.bannerUrl());
        event.setBasePrice(request.basePrice());

        event.setTaxPercent(
                request.taxPercent() == null
                        ? BigDecimal.ZERO
                        : request.taxPercent()
        );

        event.setVenue(venue);
        event.setCapacity(venue.getTotalSeats());

        // Organizer update requires admin approval again

        if (user.getRole() == Role.ORGANIZER) {
            event.setStatus(EventStatus.PENDING_APPROVAL);
        }

        return events.save(event);
    }

    // =========================================================
    // ORGANIZER - MY EVENTS
    // =========================================================

    public List<Event> myEvents(String email) {

        User organizer = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        return events.findByOrganizerId(organizer.getId());
    }

    // =========================================================
    // ORGANIZER - DASHBOARD
    // =========================================================

    public Map<String, Long> organizerDashboard(String email) {

        List<Event> myEvents = myEvents(email);

        long total = myEvents.size();

        long published = myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .count();

        long pendingApproval = myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.PENDING_APPROVAL)
                .count();

        long cancelled = myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.CANCELLED)
                .count();

        return Map.of(
                "totalEvents", total,
                "publishedEvents", published,
                "pendingApproval", pendingApproval,
                "cancelledEvents", cancelled
        );
    }

    // =========================================================
    // ORGANIZER - BOOKING STATISTICS
    // =========================================================

    public Map<String, Object> organizerBookingStats(String email) {

        User organizer = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        List<Booking> organizerBookings =
                bookings.findByEventOrganizerId(organizer.getId());

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
                                : b.getSeats().size()
                )
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
                "totalBookings", totalBookings,
                "confirmedBookings", confirmedBookings,
                "cancelledBookings", cancelledBookings,
                "totalSeatsSold", totalSeatsSold,
                "totalRevenue", totalRevenue
        );
    }

    
 // =========================================================
 // ORGANIZER - EVENT PERFORMANCE
 // =========================================================

 public List<Map<String, Object>> organizerEventPerformance(String email) {

     User organizer = users.findByEmailIgnoreCase(email)
             .orElseThrow(() ->
                     new ApiException("User not found"));

     List<Event> myEvents =
             events.findByOrganizerId(organizer.getId());

     return myEvents.stream()
             .map(event -> {

                 List<Booking> eventBookings =
                         bookings.findByEventId(event.getId());

                 long totalBookings = eventBookings.size();

                 long confirmedBookings = eventBookings.stream()
                         .filter(b ->
                                 b.getStatus() == BookingStatus.CONFIRMED)
                         .count();

                 long totalSeatsSold = eventBookings.stream()
                         .filter(b ->
                                 b.getStatus() == BookingStatus.CONFIRMED)
                         .mapToLong(b ->
                                 b.getSeats() == null
                                         ? 0
                                         : b.getSeats().size()
                         )
                         .sum();

                 BigDecimal totalRevenue = eventBookings.stream()
                         .filter(b ->
                                 b.getStatus() == BookingStatus.CONFIRMED)
                         .map(Booking::getTotalAmount)
                         .filter(amount -> amount != null)
                         .reduce(
                                 BigDecimal.ZERO,
                                 BigDecimal::add
                         );

                 return Map.<String, Object>of(
                         "eventId", event.getId(),
                         "title", event.getTitle(),
                         "status", event.getStatus(),
                         "totalBookings", totalBookings,
                         "confirmedBookings", confirmedBookings,
                         "totalSeatsSold", totalSeatsSold,
                         "totalRevenue", totalRevenue
                 );
             })
             .toList();
 }
    // =========================================================
    // SEARCH PUBLIC EVENTS
    // =========================================================

    public Page<Event> search(
            String city,
            EventType type,
            String query,
            Pageable pageable) {

        if (city != null && !city.isBlank()) {

            return events.findByStatusAndCityIgnoreCase(
                    EventStatus.PUBLISHED,
                    city.trim(),
                    pageable
            );
        }

        if (type != null) {

            return events.findByStatusAndEventType(
                    EventStatus.PUBLISHED,
                    type,
                    pageable
            );
        }

        if (query != null && !query.isBlank()) {

            return events.findByStatusAndTitleContainingIgnoreCase(
                    EventStatus.PUBLISHED,
                    query.trim(),
                    pageable
            );
        }

        return events.findByStatus(
                EventStatus.PUBLISHED,
                pageable
        );
    }

    // =========================================================
    // GET EVENT
    // =========================================================

    public Event get(Long id) {

        return events.findById(id)
                .orElseThrow(() ->
                        new ApiException("Event not found"));
    }

    // =========================================================
    // CHANGE EVENT STATUS
    // =========================================================

    public Event changeStatus(
            Long id,
            EventStatus status,
            String email) {

        Event event = get(id);

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        // ADMIN can change any status

        if (user.getRole() == Role.ADMIN) {

            event.setStatus(status);

            return events.save(event);
        }

        // ORGANIZER

        if (user.getRole() == Role.ORGANIZER) {

            // Organizer can modify only own event

            if (event.getOrganizer() == null ||
                    !event.getOrganizer()
                            .getId()
                            .equals(user.getId())) {

                throw new ApiException(
                        "You are not allowed to change this event status"
                );
            }

            // Organizer can only CANCEL

            if (status != EventStatus.CANCELLED) {

                throw new ApiException(
                        "Organizer can only cancel their own event"
                );
            }

            event.setStatus(EventStatus.CANCELLED);

            return events.save(event);
        }

        throw new ApiException(
                "You are not allowed to change event status"
        );
    }
}