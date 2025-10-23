// ========================
// PACKAGE DECLARATION
// ========================
// Organizes this class under the "security" package.
// This helps structure the project logically by purpose.
package com.supermarket.supermarket_system.utils;

// ========================
// IMPORTS
// ========================
// JJWT library for creating and parsing JWTs
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

// Spring annotation to let Spring auto-detect and manage this class as a Bean
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

// ========================
// COMPONENT: JwtUtil
// ========================
// This class provides all JWT-related operations:
// 1. Token generation
// 2. Token validation
// 3. Extracting user info (username, role)
//
// Marked as a @Component so it can be auto-injected
// (e.g., into controllers, filters, or services).
@Component
public class JwtUtils {

    // ----------------------
    // CONFIGURATION CONSTANTS
    // ----------------------

    // Secret key for signing JWTs
    // In production: load from application.properties or environment variables.
    // Must be at least 32 bytes for HS256.
    private static final String SECRET_KEY = "ZeyadAmmarMariamNayerManar12345";

    // Token expiration time: 1 hour (in milliseconds)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    // ----------------------
    // SIGNING KEY HANDLER
    // ----------------------

    // Converts the secret string into a cryptographic signing key.
    // This ensures JJWT can safely sign and verify tokens.
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // ----------------------
    // TOKEN GENERATION
    // ----------------------

    /**
     * Generates a signed JWT token for a user.
     *
     * @param username The unique user identifier (will be stored as "sub" in the payload)
     * @param role The user’s role (stored as a custom claim)
     * @return A compact JWT string (Header.Payload.Signature)
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username) // Standard claim: "sub"
                .claim("role", role)  // Custom claim: "role"
                .setIssuedAt(new Date()) // "iat": issued at
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // "exp"
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // HMAC-SHA256 signing
                .compact(); // Serialize into final token string
    }

    // ----------------------
    // TOKEN PARSING
    // ----------------------

    /**
     * Parses a JWT string and returns its Claims (the payload data).
     * Automatically verifies the token signature using the secret key.
     *
     * @param token The JWT token string to decode
     * @return The parsed token as a Jws<Claims> object
     */
    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Must match key used in signing
                .build()
                .parseClaimsJws(token); // Throws exception if invalid or expired
    }

    // ----------------------
    // DATA EXTRACTION METHODS
    // ----------------------

    /**
     * Extracts the username (stored as the standard "sub" claim)
     * from the given JWT token.
     *
     * @param token The JWT token string to extract data from.
     * @return The username stored in the token's "sub" (subject) claim.
     */
    public String getUsername(String token) {
        return parseToken(token).getBody().getSubject(); // "sub" → standard JWT subject
    }

    /**
     * Extracts the user's role from the given JWT token.
     * This reads a custom claim named "role" that was added during token generation.
     *
     * @param token The JWT token string to extract data from.
     * @return The user's role (e.g., "USER", "ADMIN") stored in the "role" claim.
     */
    public String getRole(String token) {
        return parseToken(token).getBody().get("role", String.class);
    }

    /**
     * Retrieves all claims (payload data) contained in the JWT token.
     * This includes both standard claims (like "sub", "exp", "iat")
     * and any custom claims you added (like "role").
     *
     * @param token The JWT token string to extract claims from.
     * @return A {@link io.jsonwebtoken.Claims} object containing all token data.
     */
    public Claims getAllClaims(String token) {
        return parseToken(token).getBody();
    }

    // ----------------------
    // TOKEN VALIDATION
    // ----------------------

    /**
     * Validates a JWT token’s integrity and expiration.
     * Returns true if valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token); // If this doesn’t throw, the token is valid
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException covers: expired, malformed, signature issues, etc.
            return false;
        }
    }
}