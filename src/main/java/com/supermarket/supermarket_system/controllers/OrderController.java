package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.models.Order;
import com.supermarket.supermarket_system.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Create order from user's cart
    @PostMapping("/checkout")
    public ResponseEntity<Order> createOrder(
            @RequestParam Long userId,
            @RequestBody(required = false) Map<String, String> request) {
        String paymentMethod = request != null ? request.get("paymentMethod") : null;
        Order order = orderService.createOrderFromCart(userId, paymentMethod);
        return ResponseEntity.ok(order);
    }

    // Get specific order by ID
    @GetMapping("/{orderId}/details")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    // Get user's order history
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    // Get all orders (admin)
    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // Get orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    // Get user's orders filtered by status
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Order>> getUserOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        List<Order> orders = orderService.getUserOrdersByStatus(userId, status);
        return ResponseEntity.ok(orders);
    }

    // Update order status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    // Cancel order
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Order cancelled successfully");
    }
}
