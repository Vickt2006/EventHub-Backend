package com.eventhub.repository;

import com.eventhub.entity.BookingSeat;
import com.eventhub.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    boolean existsByEventIdAndSeatId(Long eventId, Long seatId);

    @Query("""
        SELECT CASE WHEN COUNT(bs) > 0 THEN true ELSE false END
        FROM BookingSeat bs
        WHERE bs.event.id = :eventId
          AND bs.seat.id = :seatId
          AND bs.booking.status NOT IN :statuses
    """)
    boolean existsActiveBookingForSeat(
            @Param("eventId") Long eventId,
            @Param("seatId") Long seatId,
            @Param("statuses") List<BookingStatus> statuses
    );

    List<BookingSeat> findByEventId(Long eventId);
}