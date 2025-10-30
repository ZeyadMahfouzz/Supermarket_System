package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.services.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    // View cart
    @GetMapping("/{userId}")
    public Cart viewCart(@PathVariable Long userId) {
        return service.getCart(userId);
    }

    // Add item to cart
    @PostMapping("/{userId}/add")
    public Cart addToCart(@PathVariable Long userId,
                          @RequestBody Map<String, Object> body) {
        Long itemId = ((Number) body.get("itemId")).longValue();
        int quantity = ((Number) body.get("quantity")).intValue();
        return service.addItemToCart(userId, itemId, quantity);
    }

    // Update item quantity
    @PutMapping("/{userId}/update")
    public Cart updateItem(@PathVariable Long userId,
                           @RequestBody Map<String, Object> body) {
        Long itemId = ((Number) body.get("itemId")).longValue();
        int quantity = ((Number) body.get("quantity")).intValue();
        return service.updateItemQuantity(userId, itemId, quantity);
    }

    // Remove item from cart
    @DeleteMapping("/{userId}/remove")
    public Cart removeItem(@PathVariable Long userId,
                           @RequestBody Map<String, Object> body) {
        Long itemId = ((Number) body.get("itemId")).longValue();
        return service.removeItem(userId, itemId);
    }

    // Clear entire cart
    @DeleteMapping("/{userId}/clear")
    public Cart clearCart(@PathVariable Long userId) {
        return service.clearCart(userId);
    }
}
