// ========================
// PACKAGE DECLARATION
// ========================
// This class is placed in the "config" package,
// which holds all Spring Boot configuration files.
package com.supermarket.supermarket_system.config;

// ========================
// IMPORTS
// ========================
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Import your custom JWT filter
import com.supermarket.supermarket_system.security.JwtAuthFilter;

// ========================
// CLASS: SecurityConfig
// ========================
// This class defines the global Spring Security configuration.
// It replaces the old WebSecurityConfigurerAdapter (deprecated in newer Spring versions).
//
// Responsibilities:
// 1. Define which routes are public/private
// 2. Plug in the JWT authentication filter
// 3. Disable unused features (like CSRF & sessions) for REST APIs
@Configuration
public class SecurityConfig {

    // ----------------------
    // DEPENDENCIES
    // ----------------------

    // Inject the custom JWT filter so we can add it into the Spring Security chain.
    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // ----------------------
    // SECURITY FILTER CHAIN
    // ----------------------
    /**
     * The SecurityFilterChain defines how security is applied to incoming HTTP requests.
     * - Configures which endpoints are open or protected
     * - Disables features not needed in stateless JWT-based authentication
     * - Registers our custom JwtAuthenticationFilter in the chain
     *
     * @param http The HttpSecurity builder that defines security behavior
     * @return A built SecurityFilterChain used by Spring at runtime
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ----------------------
                // 1. Disable CSRF
                // ----------------------
                // CSRF (Cross-Site Request Forgery) protection is designed for browser-based form logins.
                // Since JWTs are sent in headers, not stored in cookies, CSRF attacks are not possible here.
                .csrf(csrf -> csrf.disable())

                // ----------------------
                // 2. Disable HTTP Sessions (STATELESS mode)
                // ----------------------
                // By default, Spring Security creates an HTTP session for each user after login.
                // It then stores authentication info (the SecurityContext) in that session,
                // so the user remains logged in across requests — this is "stateful" behavior.
                //
                // But JWT-based authentication is *stateless*:
                //  - The server does NOT store any user session or token.
                //  - Every request must include its own valid JWT in the Authorization header.
                //  - The server validates the token each time and rebuilds the SecurityContext on the fly.
                //
                // This makes your API scalable — no server memory is used for user sessions.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ----------------------
                // 3. Define Access Rules
                // ----------------------
                // Here we specify which endpoints require authentication.
                // Public routes are accessible to anyone (no token required),
                // while all others must include a valid JWT in the Authorization header.
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/users/register", "/users/login", "/health", "/").permitAll()

                        // All other routes are protected
                        .anyRequest().authenticated()
                )

                // ----------------------
                // 4. Add Custom JWT Filter
                // ----------------------
                // By default, Spring uses UsernamePasswordAuthenticationFilter for form login.
                // We insert our JwtAuthenticationFilter BEFORE that,
                // so it runs first and validates the token in every request.
                //
                // In stateless JWT mode, the UsernamePasswordAuthenticationFilter never actually runs,
                // but keeping the order ensures our filter executes before any authentication logic.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // ----------------------
        // 5. Build Configuration
        // ----------------------
        // Finalizes and returns the configured security filter chain.
        return http.build();
    }
}