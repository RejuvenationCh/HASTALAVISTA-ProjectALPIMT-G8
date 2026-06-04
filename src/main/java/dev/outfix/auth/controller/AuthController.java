package dev.outfix.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.outfix.auth.dto.AuthResponseDto;
import dev.outfix.auth.dto.LoginRequestDto;
import dev.outfix.auth.dto.RegisterRequestDto;
import dev.outfix.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Handles HTTP requests for user registration and login.
 * These endpoints are public — no JWT token is needed to access them.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Creates a new account and returns a JWT token.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request) {

        AuthResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/login
     * Verifies credentials and returns a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) {

        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
