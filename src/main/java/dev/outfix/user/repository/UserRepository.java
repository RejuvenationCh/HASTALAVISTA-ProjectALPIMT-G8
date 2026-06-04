package dev.outfix.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.outfix.user.entity.User;

/**
 * Handles all database queries for the User entity.
 * Spring Data JPA automatically generates the SQL behind these methods.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by their email address. Returns empty if not found. */
    Optional<User> findByEmail(String email);

    /** Check if an email is already registered, used during sign-up. */
    boolean existsByEmail(String email);
}
