package com.eventhub.controller;

import com.eventhub.entity.Seat;
import com.eventhub.entity.Venue;
import com.eventhub.repository.SeatRepository;
import com.eventhub.repository.VenueRepository;
import com.eventhub.exception.ApiException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueRepository venues;
    private final SeatRepository seats;
    public VenueController(VenueRepository venues, SeatRepository seats) { this.venues = venues; this.seats = seats; }

    @GetMapping("/public")
    public List<Venue> list(@RequestParam(required = false) String city) {
        return city == null || city.isBlank() ? venues.findAll() : venues.findByCityIgnoreCaseAndActiveTrue(city.trim());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Venue create(@RequestBody Venue venue) { return venues.save(venue); }

    @PostMapping("/{id}/generate-seats")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> generate(@PathVariable Long id,
                                        @RequestParam(defaultValue = "10") int rows,
                                        @RequestParam(defaultValue = "10") int perRow) {
        Venue venue = venues.findById(id).orElseThrow(() -> new ApiException("Venue not found"));
        int safeRows = Math.min(Math.max(rows, 1), 26), safePerRow = Math.min(Math.max(perRow, 1), 50), count = 0;
        for (int r = 0; r < safeRows; r++) {
            for (int n = 1; n <= safePerRow; n++) {
                String row = String.valueOf((char) ('A' + r));
                Seat seat = new Seat(); seat.setVenue(venue); seat.setRowLabel(row); seat.setSeatIndex(n); seat.setSeatNumber(row + "-" + n);
                seat.setCategory(r < 2 ? "PREMIUM" : "REGULAR"); seat.setPriceMultiplier(r < 2 ? BigDecimal.valueOf(1.5) : BigDecimal.ONE);
                seats.save(seat); count++;
            }
        }
        venue.setTotalSeats(safeRows * safePerRow); venues.save(venue);
        return Map.of("created", count);
    }
}
