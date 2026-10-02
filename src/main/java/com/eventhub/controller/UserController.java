package com.eventhub.controller;

import com.eventhub.entity.Favorite;
import com.eventhub.entity.User;
import com.eventhub.dto.ProfileDtos.Update;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.UserRepository;
import jakarta.validation.Valid;
import com.eventhub.entity.Notification;
import com.eventhub.service.FavoriteService;
import com.eventhub.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class UserController {
    private final FavoriteService favorites;
    private final NotificationService notifications;
    private final UserRepository users;
    public UserController(FavoriteService favorites, NotificationService notifications, UserRepository users) {
        this.favorites = favorites; this.notifications = notifications; this.users = users;
    }

    @GetMapping("/profile")
    public User profile(Authentication authentication) {
        return users.findByEmailIgnoreCase(authentication.getName()).orElseThrow(() -> new ApiException("User not found"));
    }

    @PatchMapping("/profile")
    public User updateProfile(@Valid @RequestBody Update request, Authentication authentication) {
        User user = users.findByEmailIgnoreCase(authentication.getName()).orElseThrow(() -> new ApiException("User not found"));
        if (request.name() != null && !request.name().isBlank()) user.setName(request.name().trim());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.city() != null) user.setCity(request.city());
        if (request.profileImageUrl() != null) user.setProfileImageUrl(request.profileImageUrl());
        return users.save(user);
    }

    @GetMapping("/favorites")
    public List<Favorite> favorites(Authentication authentication) { return favorites.mine(authentication.getName()); }

    @GetMapping("/notifications")
    public List<Notification> notifications(Authentication authentication) { return notifications.mine(authentication.getName()); }

    @GetMapping("/notifications/unread")
    public Map<String, Long> unread(Authentication authentication) { return Map.of("count", notifications.unread(authentication.getName())); }

    @PatchMapping("/notifications/{id}/read")
    public void markRead(@PathVariable Long id, Authentication authentication) { notifications.markRead(id, authentication.getName()); }
}
