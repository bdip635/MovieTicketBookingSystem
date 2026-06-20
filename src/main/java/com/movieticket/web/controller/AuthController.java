package com.movieticket.web.controller;

import com.movieticket.security.UserPrincipal;
import com.movieticket.service.auth.AuthService;
import com.movieticket.web.dto.auth.AuthResponse;
import com.movieticket.web.dto.auth.LoginRequest;
import com.movieticket.web.dto.auth.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.me(principal);
    }
}
