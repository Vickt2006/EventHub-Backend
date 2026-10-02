package com.eventhub.service;

import com.eventhub.entity.Notification;
import com.eventhub.entity.User;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.NotificationRepository;
import com.eventhub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotificationService(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    public List<Notification> mine(String email) {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException("User not found"));
        return notifications.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public long unread(String email) {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException("User not found"));
        return notifications.countByUserIdAndReadFalse(user.getId());
    }

    public void markRead(Long id, String email) {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException("User not found"));
        Notification notification = notifications.findById(id).orElseThrow(() -> new ApiException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) throw new ApiException("Not your notification");
        notification.setRead(true);
        notifications.save(notification);
    }
}
