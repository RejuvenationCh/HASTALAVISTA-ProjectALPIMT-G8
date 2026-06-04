package dev.outfix.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Intercepts every HTTP request and checks for a valid JWT token.
 *
 * If a valid token is found, Spring Security is told who the user is
 * so that protected endpoints can be accessed without a session.
 *
 * Expected request header format:
 *   Authorization: Bearer <token>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        // If there is no token, skip this filter and continue normally
        boolean hasNoBearerToken = authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ");
        if (hasNoBearerToken) {
            filterChain.doFilter(request, response);
            return;
        }

        // Strip the "Bearer " prefix to get the raw token string
        String jwtToken = authorizationHeader.substring(7);
        String userEmail = jwtService.extractEmail(jwtToken);

        // Only proceed if we got an email and the user is not already authenticated
        boolean userIsNotYetAuthenticated =
                userEmail != null
                && SecurityContextHolder.getContext().getAuthentication() == null;

        if (userIsNotYetAuthenticated) {
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwtToken, userDetails)) {
                // Tell Spring Security this user is authenticated
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null,
                                userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
