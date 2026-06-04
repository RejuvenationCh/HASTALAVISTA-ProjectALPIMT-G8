package dev.outfix.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.outfix.auth.dto.AuthResponseDto;
import dev.outfix.auth.dto.LoginRequestDto;
import dev.outfix.auth.dto.RegisterRequestDto;
import dev.outfix.security.JwtService;
import dev.outfix.user.entity.User;
import dev.outfix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Handles user registration and login logic.
 * After success, returns a JWT token the client can use for future requests.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Creates a new user account.
     * The password is hashed with BCrypt before being saved.
     * Returns a JWT token immediately so the user is logged in right away.
     */
    public AuthResponseDto register(RegisterRequestDto request) {
        boolean emailAlreadyTaken = userRepository.existsByEmail(request.getEmail());
        if (emailAlreadyTaken) {
            throw new RuntimeException("Email already registered");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);

        String token = jwtService.generateToken(newUser.getEmail());

        return buildAuthResponse(newUser, token);
    }

    /**
     * Verifies the user's email and password.
     * If correct, returns a fresh JWT token.
     * Throws an exception automatically if credentials are wrong.
     */
    public AuthResponseDto login(LoginRequestDto request) {
        // This line throws an exception if email or password is incorrect
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user.getEmail());

        return buildAuthResponse(user, token);
    }

    /** Helper method to build the response object from a user and token. */
    private AuthResponseDto buildAuthResponse(User user, String token) {
        return AuthResponseDto.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .finishedTutorial(user.isFinishedTutorial())
                .build();
    }
}
