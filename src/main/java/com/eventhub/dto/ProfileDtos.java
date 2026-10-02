package com.eventhub.dto;

import jakarta.validation.constraints.Size;

public final class ProfileDtos {
    private ProfileDtos() {}
    public record Update(@Size(min = 2, max = 100) String name, String phone, String city, String profileImageUrl) {}
}
