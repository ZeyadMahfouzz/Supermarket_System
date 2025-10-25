// ========================
// PACKAGE DECLARATION
// ========================
// This class belongs to the "controllers" package,
// where we expose REST endpoints for the outside world.
package com.supermarket.supermarket_system.controllers;
import com.supermarket.supermarket_system.models.Order;

// ========================
// IMPORTS
// ========================
import com.supermarket.supermarket_system.repositories.OrderRepository;
//import jakarta.persistence.criteria.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;       // Maps HTTP GET requests to methods
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;  // Marks this class as a REST controller
import java.util.HashMap;                                       // Implementation of Map
import java.util.Map;                                           // Key-value data structure interface
import java.util.List;

// ========================
// CONTROLLER CLASS
// ========================
// @RestController → Marks this class as a REST API controller.
// It combines @Controller + @ResponseBody, so all methods return JSON (not HTML).
@RestController
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    // ========================
    // READ: GET all orders
    // ========================
    // Maps GET /orders
    // Returns a list of all Order objects from the DB.
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
//        return orderRepository.findAll(); // Uses JpaRepository built-in method
    }

    // ========================
    // READ: GET a single order by ID
    // ========================
    // Maps GET /orders/{id}
    // @PathVariable → Extracts {id} from the URL
    // findById(id).orElse(null) → If not found, return null (later we can handle better errors).
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id).orElse(null);
    }


    // ========================
    // READ: GET all orders related to a single user by the user ID
    // ========================
    // Maps GET /orders/{user}/{id}
    // @PathVariable → Extracts {id} from the URL
    // findById(id).orElse(null) → If not found, return null (later we can handle better errors).
    @GetMapping("/{user}/{id}")
    public List<Order> getOrderByUser(@PathVariable long user) {
        return orderRepository.findByUser(user);
//        return orderRepository.findByUser(user);
    }
}
