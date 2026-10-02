package com.eventhub.service;

import com.eventhub.dto.AuthDtos.Login;
import com.eventhub.dto.AuthDtos.Register;
import com.eventhub.dto.AuthDtos.Token;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.exception.ApiException;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public Token register(Register request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ApiException("Email already registered");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setCity(request.city());
        user.setRole(Role.USER);
        user.setEnabled(true);
        users.save(user);
        return tokenFor(user);
    }

    public Token login(Login request) {
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ApiException("Invalid email or password"));
        if (!user.isEnabled() || !encoder.matches(request.password(), user.getPassword())) {
            throw new ApiException("Invalid email or password");
        }
        return tokenFor(user);
    }

    private Token tokenFor(User user) {
        return new Token(jwtService.generate(user.getEmail(), user.getRole().name()),
                user.getRole().name(), user.getName());
    }
}
