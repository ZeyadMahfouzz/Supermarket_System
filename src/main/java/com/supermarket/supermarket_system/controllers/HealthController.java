// ========================
// PACKAGE DECLARATION
// ========================
// This class belongs to the "controllers" package,
// where we expose REST endpoints for the outside world.
package com.supermarket.supermarket_system.controllers;

// ========================
// IMPORTS
// ========================
import org.springframework.web.bind.annotation.GetMapping;       // Maps HTTP GET requests to methods
import org.springframework.web.bind.annotation.RestController;  // Marks this class as a REST controller
import java.util.HashMap;                                       // Implementation of Map
import java.util.Map;                                           // Key-value data structure interface

// ========================
// CONTROLLER CLASS
// ========================
// @RestController → Marks this class as a REST API controller.
// It combines @Controller + @ResponseBody, so all methods return JSON (not HTML).
@RestController
public class HealthController {

    // ========================
    // HEALTH CHECK ENDPOINT
    // ========================
    // @GetMapping("/health") → Maps GET requests on path "/health"
    // Example: localhost:8080/health
    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        // Create a Map to hold response data
        // Map is the interface, HashMap is the actual implementation.
        Map<String, String> status = new HashMap<>();

        // Add key-value pairs to the response
        status.put("status", "UP");                  // Service health status
        status.put("service", "Supermarket Platform API");  // Service name

        // Return the map → Spring Boot automatically converts it into JSON:
        // {
        //   "status": "UP",
        //   "service": "Game Platform API"
        // }
        return status;
    }
}
