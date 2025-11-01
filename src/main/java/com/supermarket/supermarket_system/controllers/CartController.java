package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // View cart
    @GetMapping("/{userId}")
    public ResponseEntity<?> viewCart(@PathVariable Long userId) {
        try {
            Cart cart = cartService.getCart(userId);
            return ResponseEntity.ok(cart);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Add item to cart
    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addToCart(@PathVariable Long userId,
                                       @RequestBody Map<String, Object> body) {
        try {
            Long itemId = ((Number) body.get("itemId")).longValue();
            int quantity = ((Number) body.get("quantity")).intValue();

            Cart cart = cartService.addItemToCart(userId, itemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // Update item quantity
    @PutMapping("/{userId}/update")
    public ResponseEntity<?> updateItem(@PathVariable Long userId,
                                        @RequestBody Map<String, Object> body) {
        try {
            Long itemId = ((Number) body.get("itemId")).longValue();
            int quantity = ((Number) body.get("quantity")).intValue();

            Cart cart = cartService.updateItemQuantity(userId, itemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // Remove item from cart
    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<?> removeItem(@PathVariable Long userId,
                                        @RequestBody Map<String, Object> body) {
        try {
            Long itemId = ((Number) body.get("itemId")).longValue();
            Cart cart = cartService.removeItem(userId, itemId);
            return ResponseEntity.ok(cart);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // Clear entire cart
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            Cart cart = cartService.clearCart(userId);
            return ResponseEntity.ok(cart);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}