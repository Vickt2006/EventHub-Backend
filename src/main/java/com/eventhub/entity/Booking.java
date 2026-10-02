package com.eventhub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="bookings", indexes={@Index(columnList="booking_code"), @Index(columnList="user_id")})
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="booking_code", nullable=false, unique=true)
    private String bookingCode;
    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    private User user;

    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    private Event event;
    @OneToMany(
    	    mappedBy="booking",
    	    cascade=CascadeType.ALL,
    	    orphanRemoval=true,
    	    fetch=FetchType.EAGER
    	)
    	private List<BookingSeat> seats = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private String couponCode;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private String qrCode;
    private LocalDateTime bookedAt = LocalDateTime.now();
    private LocalDateTime cancelledAt;

    public Booking() {}

    public Booking(Long id, String bookingCode, User user, Event event, List<BookingSeat> seats, BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal totalAmount, String couponCode, BookingStatus status, PaymentStatus paymentStatus, String qrCode, LocalDateTime bookedAt, LocalDateTime cancelledAt) {
        this.id = id;
        this.bookingCode = bookingCode;
        this.user = user;
        this.event = event;
        this.seats = seats;
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.couponCode = couponCode;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.qrCode = qrCode;
        this.bookedAt = bookedAt;
        this.cancelledAt = cancelledAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public List<BookingSeat> getSeats() { return seats; }
    public void setSeats(List<BookingSeat> seats) { this.seats = seats; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

}