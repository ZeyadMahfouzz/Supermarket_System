package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.models.Order;
import com.supermarket.supermarket_system.models.User;
import com.supermarket.supermarket_system.repositories.UserRepository;
import com.supermarket.supermarket_system.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Check if user is authorized to access resources for the given userId
     */
    private boolean isAuthorizedUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // Check if user is admin - admins can access any user's resources
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }

        // Get the email from the JWT token
        String tokenEmail = auth.getName();

        // Fetch the user from database using userId from URL
        Optional<User> targetUser = userRepository.findById(userId);

        if (targetUser.isEmpty()) {
            return false;
        }

        // Compare emails
        return tokenEmail.equals(targetUser.get().getEmail());
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Create order from user's cart
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<?> createOrder(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {

        if (!isAuthorizedUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only create orders for yourself"));
        }

        try {
            String paymentMethod = request.get("paymentMethod");

            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Payment method is required"));
            }

            Order order = orderService.createOrderFromCart(userId, paymentMethod);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get specific order by ID
    @GetMapping("/{orderId}/details")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);

            // Check if user owns this order (unless they're admin)
            if (!isAdmin() && !isAuthorizedUser(order.getUser().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only view your own orders"));
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
        }
    }

    // Get user's order history
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        if (!isAuthorizedUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only view your own order history"));
        }

        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    // Get all orders (ADMIN ONLY - enforced by SecurityConfig)
    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // Get orders by status (ADMIN ONLY - enforced by SecurityConfig)
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid status: " + status));
        }
    }

    // Get user's orders filtered by status
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getUserOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {

        if (!isAuthorizedUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only view your own orders"));
        }

        try {
            List<Order> orders = orderService.getUserOrdersByStatus(userId, status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid status: " + status));
        }
    }

    // Update order status (ADMIN ONLY - enforced by SecurityConfig)
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        try {
            String status = request.get("status");

            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Status is required"));
            }

            Order order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Cancel order
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);

            // Users can only cancel their own orders (unless admin)
            if (!isAdmin() && !isAuthorizedUser(order.getUser().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only cancel your own orders"));
            }

            orderService.cancelOrder(orderId);
            return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}