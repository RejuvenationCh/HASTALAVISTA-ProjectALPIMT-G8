package dev.outfix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import dev.outfix.security.CustomUserDetailsService;
import dev.outfix.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

/**
 * Configures Spring Security for the entire application.
 *
 * Key decisions:
 * - Stateless: no HTTP sessions, every request must carry a JWT token.
 * - CSRF disabled: safe for stateless REST APIs.
 * - Public routes: /api/auth/** (login/register) and /api/tags and /uploads/**
 * - Everything else requires a valid JWT.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    /** Defines which routes are public and which require authentication. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Disable CSRF — not needed for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // These routes are open to everyone (no login needed)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/tags",
                                "/uploads/**")
                        .permitAll()
                        // All other routes require a valid JWT token
                        .anyRequest().authenticated())
                // Use stateless sessions — no server-side session storage
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                // Run our JWT filter before Spring's default login filter
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Sets up how Spring Security verifies username + password. */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** Exposes the AuthenticationManager so AuthService can trigger login. */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** Uses BCrypt to hash passwords. Industry-standard, one-way hashing. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
