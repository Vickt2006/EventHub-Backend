package com.eventhub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="seats", uniqueConstraints=@UniqueConstraint(columnNames={"venue_id","seat_number"}))
public class Seat {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    private Venue venue;
    @Column(name="row_label", nullable=false)
    private String rowLabel;
    @Column(name="seat_index", nullable=false)
    private Integer seatIndex;
    @Column(name="seat_number", nullable=false)
    private String seatNumber;
    private String category;
    private BigDecimal priceMultiplier = BigDecimal.ONE;
    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    public Seat() {}

    public Seat(Long id, Venue venue, String rowLabel, Integer seatIndex, String seatNumber, String category, BigDecimal priceMultiplier, SeatStatus status) {
        this.id = id;
        this.venue = venue;
        this.rowLabel = rowLabel;
        this.seatIndex = seatIndex;
        this.seatNumber = seatNumber;
        this.category = category;
        this.priceMultiplier = priceMultiplier;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }

    public Integer getSeatIndex() { return seatIndex; }
    public void setSeatIndex(Integer seatIndex) { this.seatIndex = seatIndex; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPriceMultiplier() { return priceMultiplier; }
    public void setPriceMultiplier(BigDecimal priceMultiplier) { this.priceMultiplier = priceMultiplier; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

}