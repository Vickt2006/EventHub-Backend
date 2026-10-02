package com.eventhub.service;

import com.eventhub.dto.ReviewDtos.Create;
import com.eventhub.entity.Event;
import com.eventhub.entity.Review;
import com.eventhub.entity.User;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.ReviewRepository;
import com.eventhub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviews;
    private final UserRepository users;
    private final EventRepository events;

    public ReviewService(ReviewRepository reviews, UserRepository users, EventRepository events) {
        this.reviews = reviews;
        this.users = users;
        this.events = events;
    }

    public Review add(Long eventId, Create request, String email) {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException("User not found"));
        Event event = events.findById(eventId).orElseThrow(() -> new ApiException("Event not found"));
        if (reviews.findByUserIdAndEventId(user.getId(), eventId).isPresent()) throw new ApiException("You already reviewed this event");
        Review review = new Review();
        review.setUser(user);
        review.setEvent(event);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setVisible(true);
        return reviews.save(review);
    }

    public List<Review> list(Long eventId) {
        return reviews.findByEventIdAndVisibleTrueOrderByCreatedAtDesc(eventId);
    }

    public Double average(Long eventId) {
        return reviews.averageRating(eventId);
    }
}
