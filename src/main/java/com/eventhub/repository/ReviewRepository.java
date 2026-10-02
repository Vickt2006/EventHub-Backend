package com.eventhub.repository;

import com.eventhub.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEventIdAndVisibleTrueOrderByCreatedAtDesc(Long eventId);
    Optional<Review> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.event.id = :eventId and r.visible = true")
    Double averageRating(@Param("eventId") Long eventId);
}
