package com.eventhub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="payments")
public class Payment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true)
    private String transactionId;
    @OneToOne(fetch=FetchType.EAGER, optional=false)
    private Booking booking;
    private BigDecimal amount;
    private String method;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String providerReference;
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(Long id, String transactionId, Booking booking, BigDecimal amount, String method, PaymentStatus status, String providerReference, LocalDateTime paidAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.booking = booking;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.providerReference = providerReference;
        this.paidAt = paidAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

}