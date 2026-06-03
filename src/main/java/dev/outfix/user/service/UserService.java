package dev.outfix.user.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.outfix.user.entity.User;
import dev.outfix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public User register(
            String username,
            String email,
            String password) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "Email already registered");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(
                        passwordEncoder.encode(password))
                .role("USER")
                .createdAt(
                        LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }
}