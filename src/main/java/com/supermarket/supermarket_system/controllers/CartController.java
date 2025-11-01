package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.User;
import com.supermarket.supermarket_system.repositories.UserRepository;
import com.supermarket.supermarket_system.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    /**
     * Validates that the authenticated user matches the userId in the path
     * This prevents users from accessing/modifying other users' carts
     */
    // Add this to your CartController and OrderController classes

    @Autowired
    private UserRepository userRepository;

    private boolean isAuthorizedUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            System.out.println("DEBUG: No authentication found");
            return false;
        }

        // Check if user is admin - admins can access any user's resources
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            System.out.println("DEBUG: User is admin, access granted");
            return true;
        }

        // Get the email from the JWT token
        String tokenEmail = auth.getName();
        System.out.println("DEBUG: Token email = " + tokenEmail);

        // Fetch the user from database using userId from URL
        Optional<User> targetUser = userRepository.findById(userId);

        if (targetUser.isEmpty()) {
            System.out.println("DEBUG: User with ID " + userId + " not found in database");
            return false; // User doesn't exist
        }

        String dbEmail = targetUser.get().getEmail();
        System.out.println("DEBUG: Database email = " + dbEmail);
        System.out.println("DEBUG: Emails match? " + tokenEmail.equals(dbEmail));

        // Compare: does the email from token match the email of the user with this userId?
        return tokenEmail.equals(dbEmail);
    }

    // View cart
    @GetMapping("/{userId}")
    public ResponseEntity<?> viewCart(@PathVariable Long userId) {
        if (!isAuthorizedUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only access your own cart"));
        }

        Cart cart = service.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    // Add item to cart
    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addToCart(@PathVariable Long userId,
                                       @RequestBody Map<String, Object> body) {
        if (!isAuthorizedUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only modify your own cart"));
        }

        try {
            Long itemId = ((Number) body.get("itemId")).longValue();
            int quantity = ((Number) body.get("quantity")).intValue();

            if (quantity <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quantity must be positive"));
            }

            Cart cart = service.addItemToCart(userId, itemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // Update item quantity
    @PutMapping("/{userId}/update")
    public ResponseEntity<?> updateItem(@PathVariable Long userId,
                                        @RequestBody Map<String, Object> body) {
        if (!isAuthorizedUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only modify your own cart"));
        }

        try {
            Long itemId = ((Number) body.get("itemId")).longValue();
            int quantity = ((Number) body.get("quantity")).intValue();

            if (quantity < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quantity cannot be negative"));
            }

            Cart cart = service.updateItemQuantity(userId, itemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // Remove item from cart
    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<?> removeItem(@PathVariable Long userId,
                                        @RequestBody Map<String, Object> body) {
        if (!isAuthorizedUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only modify your own cart"));
        }

        try {
            Long itemId = ((Number) body.get("itemId")).longValue();
            Cart cart = service.removeItem(userId, itemId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // Clear entire cart
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        if (!isAuthorizedUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only modify your own cart"));
        }

        Cart cart = service.clearCart(userId);
        return ResponseEntity.ok(cart);
    }
}