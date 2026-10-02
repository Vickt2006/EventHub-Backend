package com.eventhub.controller;

import com.eventhub.entity.Payment;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository payments;
    private final UserRepository users;
    public PaymentController(PaymentRepository payments, UserRepository users) { this.payments = payments; this.users = users; }

    @GetMapping("/booking/{bookingId}")
    public Payment byBooking(@PathVariable Long bookingId, Authentication authentication) {
        String email = authentication.getName();
        Payment payment = payments.findByBookingId(bookingId).orElseThrow(() -> new ApiException("Payment not found"));
        if (!payment.getBooking().getUser().getEmail().equalsIgnoreCase(email)) throw new ApiException("Not your payment");
        return payment;
    }
}
