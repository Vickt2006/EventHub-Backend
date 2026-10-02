package com.eventhub.service;

import com.eventhub.dto.BookingDtos.Lock;
import com.eventhub.entity.BookingStatus;
import com.eventhub.entity.Event;
import com.eventhub.entity.Seat;
import com.eventhub.entity.SeatHold;
import com.eventhub.entity.SeatStatus;
import com.eventhub.entity.User;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.BookingSeatRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.SeatHoldRepository;
import com.eventhub.repository.SeatRepository;
import com.eventhub.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeatHoldService {

    private static final int HOLD_MINUTES = 10;

    private final SeatHoldRepository holds;
    private final EventRepository events;
    private final SeatRepository seats;
    private final UserRepository users;
    private final BookingSeatRepository bookingSeats;

    public SeatHoldService(
            SeatHoldRepository holds,
            EventRepository events,
            SeatRepository seats,
            UserRepository users,
            BookingSeatRepository bookingSeats) {

        this.holds = holds;
        this.events = events;
        this.seats = seats;
        this.users = users;
        this.bookingSeats = bookingSeats;
    }

    // =========================================================
    // LOCK SEATS
    // =========================================================

    @Transactional
    public List<SeatHold> lock(
            Lock request,
            String email) {

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        Event event = events.findById(request.eventId())
                .orElseThrow(() ->
                        new ApiException("Event not found"));

        LocalDateTime now = LocalDateTime.now();

        cleanupExpired(now);

        List<SeatHold> result = new ArrayList<>();

        for (Long seatId : request.seatIds()
                .stream()
                .distinct()
                .toList()) {

            Seat seat = seats.findById(seatId)
                    .orElseThrow(() ->
                            new ApiException(
                                    "Seat not found: " + seatId
                            ));

            // Check venue and seat availability
            if (!seat.getVenue().getId()
                    .equals(event.getVenue().getId())
                    || seat.getStatus() != SeatStatus.AVAILABLE) {

                throw new ApiException(
                        "Seat is not available for this event: "
                                + seat.getSeatNumber()
                );
            }

            // Only active bookings block the seat.
            // CANCELLED bookings do not block the seat.
            if (bookingSeats.existsActiveBookingForSeat(
                    event.getId(),
                    seat.getId(),
                    List.of(BookingStatus.CANCELLED)
            )) {

                throw new ApiException(
                        "Seat already booked: "
                                + seat.getSeatNumber()
                );
            }

            SeatHold existing =
                    holds.findByEventIdAndSeatId(
                            event.getId(),
                            seat.getId()
                    ).orElse(null);

            // Another user's active hold
            if (existing != null
                    && existing.getExpiresAt().isAfter(now)
                    && !existing.getUser().getId()
                    .equals(user.getId())) {

                throw new ApiException(
                        "Seat temporarily locked: "
                                + seat.getSeatNumber()
                );
            }

            // Remove previous hold of same user
            if (existing != null) {
                holds.delete(existing);
            }

            SeatHold hold = new SeatHold();

            hold.setEvent(event);
            hold.setSeat(seat);
            hold.setUser(user);

            hold.setExpiresAt(
                    now.plusMinutes(HOLD_MINUTES)
            );

            result.add(
                    holds.save(hold)
            );
        }

        return result;
    }

    // =========================================================
    // RELEASE SEAT HOLD
    // =========================================================

    @Transactional
    public void release(
            Long holdId,
            String email) {

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        SeatHold hold = holds.findById(holdId)
                .orElseThrow(() ->
                        new ApiException("Seat hold not found"));

        if (!hold.getUser().getId()
                .equals(user.getId())) {

            throw new ApiException(
                    "Not your seat hold"
            );
        }

        holds.delete(hold);
    }

    // =========================================================
    // CLEANUP EXPIRED HOLDS
    // =========================================================

    @Transactional
    public void cleanupExpired(
            LocalDateTime now) {

        holds.deleteAll(
                holds.findByExpiresAtBefore(now)
        );
    }

    // =========================================================
    // MY SEAT HOLDS
    // =========================================================

    public List<SeatHold> mine(
            String email) {

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        cleanupExpired(
                LocalDateTime.now()
        );

        return holds.findByUserIdOrderByExpiresAtDesc(
                user.getId()
        );
    }
}