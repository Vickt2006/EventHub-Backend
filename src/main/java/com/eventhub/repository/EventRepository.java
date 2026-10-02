package com.eventhub.repository;

import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.EventType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByStatusAndCityIgnoreCase(
            EventStatus status,
            String city,
            Pageable pageable
    );

    Page<Event> findByStatusAndEventType(
            EventStatus status,
            EventType eventType,
            Pageable pageable
    );

    Page<Event> findByStatusAndTitleContainingIgnoreCase(
            EventStatus status,
            String title,
            Pageable pageable
    );

    Page<Event> findByStatus(
            EventStatus status,
            Pageable pageable
    );

    long countByStatus(EventStatus status);

    // Organizer ke apne events
    List<Event> findByOrganizerId(Long organizerId);
}