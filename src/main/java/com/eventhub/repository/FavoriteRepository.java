package com.eventhub.repository;

import com.eventhub.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    void deleteByUserIdAndEventId(Long userId, Long eventId);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
}
