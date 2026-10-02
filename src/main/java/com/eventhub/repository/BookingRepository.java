package com.eventhub.repository;

import com.eventhub.entity.Booking;
import com.eventhub.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);

    List<Booking> findByEventId(Long eventId);

    long countByStatus(BookingStatus status);

    List<Booking> findByEventOrganizerId(Long organizerId);

}