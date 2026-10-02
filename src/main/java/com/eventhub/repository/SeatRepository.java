package com.eventhub.repository;

import com.eventhub.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByVenueIdOrderByRowLabelAscSeatIndexAsc(Long venueId);
    long countByVenueId(Long venueId);
}
