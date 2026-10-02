package com.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record Register(@NotBlank String name, @Email @NotBlank String email,
                           @Size(min = 6, max = 100) String password, String phone, String city) {}
    public record Login(@Email @NotBlank String email, @NotBlank String password) {}
    public record Token(String token, String role, String name) {}
}
