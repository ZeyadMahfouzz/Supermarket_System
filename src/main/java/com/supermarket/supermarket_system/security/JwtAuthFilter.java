// ========================
// PACKAGE DECLARATION
// ========================
// Keeps this filter organized in the "security" package.
package com.supermarket.supermarket_system.security;

// ========================
// IMPORTS
// ========================
// Handles JWT validation and extraction
import com.supermarket.supermarket_system.utils.JwtUtils;

// Jakarta Servlet API (HTTP Request/Response handling)
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Spring Security core classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

// ========================
// COMPONENT: JwtAuthFilter
// ========================
// This filter runs ONCE per request (extends OncePerRequestFilter).
// It intercepts incoming HTTP requests and:
//   1. Extracts the JWT token from the Authorization header.
//   2. Validates the token using JwtUtils.
//   3. Builds an Authentication object (Username + Role).
//   4. Stores it in the SecurityContext (so Spring knows who the user is).
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // Inject our JWT utility helper for token validation and parsing
    @Autowired
    private JwtUtils jwtUtil;

    // ---------------------------------------------------------------
    // doFilterInternal() — Runs for every request before controllers
    // ---------------------------------------------------------------
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1️⃣ Get the Authorization header (should be in format "Bearer <token>")
        final String authHeader = request.getHeader("Authorization");

        // 2️⃣ If no header or wrong format → skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Extract the actual JWT token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // 4️⃣ Validate the token (check signature + expiration)
        if (jwtUtil.validateToken(token)) {

            // 5️⃣ Extract user info from token
            String email = jwtUtil.getEmail(token);
            String role = jwtUtil.getRole(token); // e.g., "ADMIN" or "USER"

            // 6️⃣ Convert role string into Spring’s "GrantedAuthority" format
            // Spring expects roles as a list like [new SimpleGrantedAuthority("ROLE_ADMIN")]
            List<SimpleGrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

            // 7️⃣ Build Authentication object that represents the logged-in user
            // "User" is a built-in Spring Security model implementing UserDetails
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            new User(email, "", authorities), // principal (user info)
                            null,                                // credentials (none for JWT)
                            authorities                          // granted authorities (roles)
                    );

            // 8️⃣ Add extra details (like client IP) for auditing/logging
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 9️⃣ Store the Authentication object inside the SecurityContext
            // This is how Spring "remembers" the user for the rest of the request
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 🔟 Continue the filter chain — pass control to the next filter or controller
        filterChain.doFilter(request, response);
    }
}