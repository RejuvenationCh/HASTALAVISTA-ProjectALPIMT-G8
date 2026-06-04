package dev.outfix.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Handles everything related to JWT (JSON Web Token).
 *
 * A JWT is a small, signed string that proves a user is logged in.
 * We send it to the client after login, and the client sends it back
 * on every protected request inside the "Authorization" header.
 */
@Service
public class JwtService {

    /** Secret key used to sign tokens. Stored in application.properties. */
    @Value("${jwt.secret}")
    private String secret;

    /** How long a token stays valid in milliseconds (default: 24 hours). */
    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Creates a new JWT token for the given email.
     * The token contains the user's email and an expiry date.
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Reads the email address stored inside a token.
     * Used to identify which user made the request.
     */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Checks that a token belongs to the given user and has not expired.
     * Returns true only if both conditions are met.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String emailInToken = extractEmail(token);
        boolean emailMatches = emailInToken.equals(userDetails.getUsername());
        boolean tokenNotExpired = !isTokenExpired(token);
        return emailMatches && tokenNotExpired;
    }

    /** Returns true if the token's expiry date is in the past. */
    private boolean isTokenExpired(String token) {
        Date expiryDate = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiryDate.before(new Date());
    }

    /** Converts the Base64 secret string into a cryptographic key object. */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
