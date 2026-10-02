package com.eventhub.service;

import com.eventhub.entity.Event;
import com.eventhub.entity.Favorite;
import com.eventhub.entity.User;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.FavoriteRepository;
import com.eventhub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favorites;
    private final UserRepository users;
    private final EventRepository events;

    public FavoriteService(
            FavoriteRepository favorites,
            UserRepository users,
            EventRepository events
    ) {
        this.favorites = favorites;
        this.users = users;
        this.events = events;
    }

    @Transactional
    public String toggle(Long eventId, String email) {

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("User not found"));

        if (favorites.existsByUserIdAndEventId(user.getId(), eventId)) {

            favorites.deleteByUserIdAndEventId(
                    user.getId(),
                    eventId
            );

            return "removed";
        }

        Event event = events.findById(eventId)
                .orElseThrow(() -> new ApiException("Event not found"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setEvent(event);

        favorites.save(favorite);

        return "added";
    }

    public List<Favorite> mine(String email) {

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("User not found"));

        return favorites.findByUserIdOrderByCreatedAtDesc(
                user.getId()
        );
    }
}