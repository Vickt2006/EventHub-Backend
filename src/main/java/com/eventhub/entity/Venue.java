package com.eventhub.entity;

import jakarta.persistence.*;

@Entity
@Table(name="venues")
public class Venue {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String description;
    private String imageUrl;
    private Integer totalSeats;
    private boolean active = true;

    public Venue() {}

    public Venue(Long id, String name, String address, String city, String state, String country, String pincode, String description, String imageUrl, Integer totalSeats, boolean active) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
        this.description = description;
        this.imageUrl = imageUrl;
        this.totalSeats = totalSeats;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

}