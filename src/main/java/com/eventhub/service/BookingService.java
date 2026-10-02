package com.eventhub.service;

import com.eventhub.dto.BookingDtos.Create;
import com.eventhub.entity.Booking;
import com.eventhub.entity.BookingSeat;
import com.eventhub.entity.BookingStatus;
import com.eventhub.entity.Coupon;
import com.eventhub.entity.CouponType;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.Payment;
import com.eventhub.entity.PaymentStatus;
import com.eventhub.entity.Seat;
import com.eventhub.entity.SeatStatus;
import com.eventhub.entity.SeatHold;
import com.eventhub.entity.User;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.BookingRepository;
import com.eventhub.repository.BookingSeatRepository;
import com.eventhub.repository.CouponRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.repository.SeatRepository;
import com.eventhub.repository.SeatHoldRepository;
import com.eventhub.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final BookingSeatRepository bookingSeats;
    private final EventRepository events;
    private final SeatRepository seats;
    private final UserRepository users;
    private final CouponRepository coupons;
    private final PaymentRepository payments;
    private final SeatHoldRepository holds;

    public BookingService(
            BookingRepository bookings,
            BookingSeatRepository bookingSeats,
            EventRepository events,
            SeatRepository seats,
            UserRepository users,
            CouponRepository coupons,
            PaymentRepository payments,
            SeatHoldRepository holds) {

        this.bookings = bookings;
        this.bookingSeats = bookingSeats;
        this.events = events;
        this.seats = seats;
        this.users = users;
        this.coupons = coupons;
        this.payments = payments;
        this.holds = holds;
    }

    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @Transactional
    public Booking book(Create request, String email) {

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        Event event = events.findById(request.eventId())
                .orElseThrow(() ->
                        new ApiException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ApiException("Event is not available");
        }

        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException("Event already started");
        }

        List<Long> ids = request.seatIds()
                .stream()
                .distinct()
                .toList();

        if (ids.size() != request.seatIds().size()) {
            throw new ApiException("Duplicate seat selected");
        }

        List<Seat> selectedSeats = seats.findAllById(ids);

        if (selectedSeats.size() != ids.size()) {
            throw new ApiException("One or more seats are invalid");
        }

        for (Seat seat : selectedSeats) {

            if (!seat.getVenue().getId()
                    .equals(event.getVenue().getId())) {

                throw new ApiException(
                        "Seat belongs to another venue: "
                                + seat.getSeatNumber()
                );
            }

            if (seat.getStatus() != SeatStatus.AVAILABLE
                    || bookingSeats.existsActiveBookingForSeat(
                            event.getId(),
                            seat.getId(),
                            List.of(BookingStatus.CANCELLED)
                    )) {

                throw new ApiException(
                        "Seat unavailable: "
                                + seat.getSeatNumber()
                );
            }

            SeatHold hold = holds
                    .findByEventIdAndSeatId(
                            event.getId(),
                            seat.getId()
                    )
                    .orElse(null);

            if (hold != null
                    && hold.getExpiresAt()
                            .isAfter(LocalDateTime.now())
                    && !hold.getUser().getId()
                            .equals(user.getId())) {

                throw new ApiException(
                        "Seat temporarily locked: "
                                + seat.getSeatNumber()
                );
            }
        }

        BigDecimal subtotal = selectedSeats.stream()
                .map(seat ->
                        event.getBasePrice()
                                .multiply(seat.getPriceMultiplier())
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Coupon coupon = null;

        BigDecimal discount = BigDecimal.ZERO;

        if (request.couponCode() != null
                && !request.couponCode().isBlank()) {

            coupon = coupons
                    .findByCodeIgnoreCase(
                            request.couponCode().trim()
                    )
                    .orElseThrow(() ->
                            new ApiException("Invalid coupon"));

            validateCoupon(coupon, subtotal);

            discount = calculateDiscount(coupon, subtotal);
        }

        BigDecimal taxable = subtotal
                .subtract(discount)
                .max(BigDecimal.ZERO);

        BigDecimal taxPercent =
                event.getTaxPercent() == null
                        ? BigDecimal.ZERO
                        : event.getTaxPercent();

        BigDecimal tax = taxable
                .multiply(taxPercent)
                .divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal total = taxable.add(tax);

        Booking booking = new Booking();

        booking.setBookingCode(
                "EH"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 10)
                                .toUpperCase()
        );

        booking.setUser(user);
        booking.setEvent(event);
        booking.setSubtotal(subtotal);
        booking.setDiscount(discount);
        booking.setTax(tax);
        booking.setTotalAmount(total);

        booking.setCouponCode(
                coupon == null
                        ? null
                        : coupon.getCode()
        );

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);

        booking.setQrCode(
                "EVENTHUB:" + UUID.randomUUID()
        );

        for (Seat seat : selectedSeats) {

            BookingSeat bookingSeat = new BookingSeat();

            bookingSeat.setBooking(booking);
            bookingSeat.setEvent(event);
            bookingSeat.setSeat(seat);

            bookingSeat.setPrice(
                    event.getBasePrice()
                            .multiply(seat.getPriceMultiplier())
            );

            booking.getSeats().add(bookingSeat);
        }

        Booking saved = bookings.save(booking);

        if (coupon != null) {

            coupon.setUsedCount(
                    coupon.getUsedCount() + 1
            );

            coupons.save(coupon);
        }

        Payment payment = new Payment();

        payment.setTransactionId(
                "TXN"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 12)
                                .toUpperCase()
        );

        payment.setBooking(saved);
        payment.setAmount(total);

        payment.setMethod(
                request.paymentMethod() == null
                        ? "DEMO"
                        : request.paymentMethod()
        );

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        payments.save(payment);

        for (Long seatId : ids) {

            holds.findByEventIdAndSeatId(
                    event.getId(),
                    seatId
            ).ifPresent(holds::delete);
        }

        return saved;
    }

    // =========================================================
    // COUPON VALIDATION
    // =========================================================

    private void validateCoupon(
            Coupon coupon,
            BigDecimal subtotal) {

        LocalDateTime now = LocalDateTime.now();

        if (!coupon.isActive()) {
            throw new ApiException("Coupon is inactive");
        }

        if (coupon.getStartsAt() != null
                && now.isBefore(coupon.getStartsAt())) {

            throw new ApiException("Coupon not started");
        }

        if (coupon.getExpiresAt() != null
                && now.isAfter(coupon.getExpiresAt())) {

            throw new ApiException("Coupon expired");
        }

        if (coupon.getUsageLimit() != null
                && coupon.getUsedCount()
                        >= coupon.getUsageLimit()) {

            throw new ApiException(
                    "Coupon usage limit reached"
            );
        }

        if (subtotal.compareTo(
                coupon.getMinOrderValue()) < 0) {

            throw new ApiException(
                    "Minimum order value not reached"
            );
        }
    }

    // =========================================================
    // CALCULATE DISCOUNT
    // =========================================================

    private BigDecimal calculateDiscount(
            Coupon coupon,
            BigDecimal subtotal) {

        BigDecimal discount =
                coupon.getType() == CouponType.PERCENTAGE
                        ? subtotal
                                .multiply(coupon.getValue())
                                .divide(
                                        BigDecimal.valueOf(100),
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        : coupon.getValue();

        if (coupon.getMaxDiscount() != null) {

            discount = discount.min(
                    coupon.getMaxDiscount()
            );
        }

        return discount
                .min(subtotal)
                .max(BigDecimal.ZERO);
    }

    // =========================================================
    // MY BOOKINGS
    // =========================================================

    public List<Booking> mine(String email) {

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        return bookings.findByUserIdOrderByBookedAtDesc(
                user.getId()
        );
    }

    // =========================================================
    // GET BOOKING
    // =========================================================

    public Booking get(String code, String email) {

        Booking booking = bookings
                .findByBookingCode(code)
                .orElseThrow(() ->
                        new ApiException("Booking not found"));

        if (!booking.getUser().getEmail()
                .equalsIgnoreCase(email)) {

            throw new ApiException(
                    "This booking belongs to another user"
            );
        }

        return booking;
    }

    // =========================================================
    // CANCEL BOOKING
    // =========================================================

    @Transactional
    public Booking cancel(
            String code,
            String email) {

        Booking booking = bookings
                .findByBookingCode(code)
                .orElseThrow(() ->
                        new ApiException("Booking not found"));

        if (!booking.getUser().getEmail()
                .equalsIgnoreCase(email)) {

            throw new ApiException(
                    "This booking belongs to another user"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {

            throw new ApiException(
                    "Booking already cancelled"
            );
        }

        if (booking.getEvent().getStartTime()
                .isBefore(LocalDateTime.now())) {

            throw new ApiException(
                    "Cancellation window closed"
            );
        }

        booking.setStatus(
                BookingStatus.CANCELLED
        );

        booking.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        booking.setCancelledAt(
                LocalDateTime.now()
        );

        Payment payment =
                payments.findByBookingId(
                        booking.getId()
                ).orElse(null);

        if (payment != null) {

            payment.setStatus(
                    PaymentStatus.REFUNDED
            );
        }

        return bookings.save(booking);
    }

    // =========================================================
    // ORGANIZER - EVENT BOOKINGS
    // =========================================================

    public List<Booking> organizerEventBookings(
            Long eventId,
            String email) {

        User organizer = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        Event event = events.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found"));

        if (event.getOrganizer() == null
                || !event.getOrganizer()
                        .getId()
                        .equals(organizer.getId())) {

            throw new ApiException(
                    "You are not allowed to view bookings for this event"
            );
        }

        return bookings.findByEventId(eventId);
    }

    // =========================================================
    // ORGANIZER - EVENT BOOKING SUMMARY
    // =========================================================

    public java.util.Map<String, Object>
    organizerEventBookingSummary(
            Long eventId,
            String email) {

        User organizer = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("User not found"));

        Event event = events.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found"));

        if (event.getOrganizer() == null
                || !event.getOrganizer()
                        .getId()
                        .equals(organizer.getId())) {

            throw new ApiException(
                    "You are not allowed to view this event"
            );
        }

        List<Booking> eventBookings =
                bookings.findByEventId(eventId);

        long totalBookings =
                eventBookings.size();

        long confirmedBookings =
                eventBookings.stream()
                        .filter(b ->
                                b.getStatus()
                                        == BookingStatus.CONFIRMED)
                        .count();

        long cancelledBookings =
                eventBookings.stream()
                        .filter(b ->
                                b.getStatus()
                                        == BookingStatus.CANCELLED)
                        .count();

        long totalSeatsSold =
                eventBookings.stream()
                        .filter(b ->
                                b.getStatus()
                                        == BookingStatus.CONFIRMED)
                        .mapToLong(b ->
                                b.getSeats() == null
                                        ? 0
                                        : b.getSeats().size()
                        )
                        .sum();

        BigDecimal totalRevenue =
                eventBookings.stream()
                        .filter(b ->
                                b.getStatus()
                                        == BookingStatus.CONFIRMED)
                        .map(Booking::getTotalAmount)
                        .filter(amount ->
                                amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return java.util.Map.of(
                "eventId", eventId,
                "totalBookings", totalBookings,
                "confirmedBookings", confirmedBookings,
                "cancelledBookings", cancelledBookings,
                "totalSeatsSold", totalSeatsSold,
                "totalRevenue", totalRevenue
        );
    }

    // =========================================================
    // AVAILABLE SEATS
    // =========================================================

    public List<Seat> seats(Long eventId) {

        Event event = events.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found"));

        Set<Long> taken =
                bookingSeats.findByEventId(eventId)
                        .stream()
                        .filter(item ->
                                item.getBooking()
                                        .getStatus()
                                        != BookingStatus.CANCELLED
                        )
                        .map(item ->
                                item.getSeat().getId()
                        )
                        .collect(Collectors.toSet());

        return seats
                .findByVenueIdOrderByRowLabelAscSeatIndexAsc(
                        event.getVenue().getId()
                )
                .stream()
                .filter(seat ->
                        !taken.contains(seat.getId())
                                && seat.getStatus()
                                        == SeatStatus.AVAILABLE
                )
                .toList();
    }
}