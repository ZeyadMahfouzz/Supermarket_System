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
public class OrderController {



}
