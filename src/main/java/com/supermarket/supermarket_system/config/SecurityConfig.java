package com.supermarket.supermarket_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.supermarket.supermarket_system.security.JwtAuthFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT authentication
                .csrf(csrf -> csrf.disable())

                // Stateless session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Define access rules with JWT authentication
                .authorizeHttpRequests(auth -> auth
                        // ============================================
                        // PUBLIC ENDPOINTS (No authentication required)
                        // ============================================
                        .requestMatchers("/users/register", "/users/login", "/health").permitAll()

                        // ============================================
                        // ITEM ENDPOINTS
                        // ============================================
                        // Anyone can view items
                        .requestMatchers(HttpMethod.GET, "/items", "/items/**").permitAll()

                        // Only ADMIN can create, update, or delete items
                        .requestMatchers(HttpMethod.POST, "/items").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/items/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/items/**").hasRole("ADMIN")

                        // ============================================
                        // CART ENDPOINTS (Authenticated users only)
                        // ============================================
                        .requestMatchers("/cart/**").authenticated()

                        // ============================================
                        // ORDER ENDPOINTS
                        // ============================================
                        // Users can create orders and view their own orders
                        .requestMatchers(HttpMethod.POST, "/orders/*/checkout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/orders/*/details").authenticated()
                        .requestMatchers(HttpMethod.GET, "/orders/user/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/orders/*/cancel").authenticated()

                        // Only ADMIN can view all orders, filter by status, and update order status
                        .requestMatchers(HttpMethod.GET, "/orders/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/orders/status/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/status").hasRole("ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )

                // Add JWT filter before Spring Security's default authentication filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}