package dev.outfix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import dev.outfix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {

        return username ->

        userRepository
                .findByEmail(username)
                .map(user ->

                org.springframework.security.core.userdetails.User
                        .withUsername(
                                user.getEmail())
                        .password(
                                user.getPassword())
                        .roles(
                                user.getRole())
                        .build()

                )
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/login",
                                "/register")
                        .permitAll()

                        .anyRequest()
                        .authenticated())

                .formLogin(form -> form

                        .loginPage("/login")

                        .defaultSuccessUrl(
                                "/home",
                                true)

                        .permitAll())

                .logout(logout -> logout

                        .logoutSuccessUrl(
                                "/login?logout"));

        return http.build();
    }
}