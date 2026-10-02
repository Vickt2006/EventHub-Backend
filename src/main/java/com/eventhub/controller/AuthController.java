package com.eventhub.controller;

import com.eventhub.dto.AuthDtos.Login;
import com.eventhub.dto.AuthDtos.Register;
import com.eventhub.dto.AuthDtos.Token;
import com.eventhub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/register")
    public Token register(@Valid @RequestBody Register request) { return service.register(request); }

    @PostMapping("/login")
    public Token login(@Valid @RequestBody Login request) { return service.login(request); }
}
