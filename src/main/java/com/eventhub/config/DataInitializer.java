package com.eventhub.config;

import com.eventhub.entity.Role;
import com.eventhub.entity.Seat;
import com.eventhub.entity.Venue;
import com.eventhub.entity.User;
import com.eventhub.repository.SeatRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.repository.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(UserRepository users, VenueRepository venues, SeatRepository seats, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmailIgnoreCase("admin@eventhub.com")) {
                User admin = new User();
                admin.setName("EventHub Admin");
                admin.setEmail("admin@eventhub.com");
                admin.setPassword(encoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                users.save(admin);
            }

            if (venues.count() == 0) {
                Venue venue = new Venue();
                venue.setName("EventHub Arena"); venue.setAddress("Main Road"); venue.setCity("Pune");
                venue.setState("Maharashtra"); venue.setCountry("India"); venue.setPincode("411001");
                venue.setTotalSeats(100); venue.setActive(true);
                venue = venues.save(venue);
                for (int row = 0; row < 10; row++) {
                    for (int number = 1; number <= 10; number++) {
                        Seat seat = new Seat();
                        String rowLabel = String.valueOf((char) ('A' + row));
                        seat.setVenue(venue); seat.setRowLabel(rowLabel); seat.setSeatIndex(number);
                        seat.setSeatNumber(rowLabel + "-" + number);
                        seat.setCategory(row < 2 ? "PREMIUM" : "REGULAR");
                        seat.setPriceMultiplier(row < 2 ? BigDecimal.valueOf(1.5) : BigDecimal.ONE);
                        seats.save(seat);
                    }
                }
            }
        };
    }
}
