package com.eventhub.controller;

import com.eventhub.entity.Favorite;
import com.eventhub.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Add / Remove favorite
    @PostMapping("/{eventId}")
    public ResponseEntity<?> toggle(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        String result = favoriteService.toggle(
                eventId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Favorite " + result
                )
        );
    }

    // Get my favorites
    @GetMapping
    public ResponseEntity<List<Favorite>> mine(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                favoriteService.mine(authentication.getName())
        );
    }
}