package com.eventhub.repository;

import com.eventhub.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    Optional<SeatHold> findByEventIdAndSeatId(Long eventId, Long seatId);
    List<SeatHold> findByExpiresAtBefore(LocalDateTime time);
    List<SeatHold> findByUserIdOrderByExpiresAtDesc(Long userId);
}
