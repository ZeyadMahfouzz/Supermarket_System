package com.supermarket.supermarket_system.controllers;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.services.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public Cart viewCart(@PathVariable Long userId) {
        return service.getCart(userId);
    }

    @PostMapping("/{userId}/add")
    public Cart addToCart(@PathVariable Long userId,
                          @RequestParam Long itemId,
                          @RequestParam int quantity) {
        return service.addItemToCart(userId, itemId, quantity);
    }

    @PutMapping("/{userId}/update")
    public Cart updateItem(@PathVariable Long userId,
                           @RequestParam Long itemId,
                           @RequestParam int quantity) {
        return service.updateItemQuantity(userId, itemId, quantity);
    }

    @DeleteMapping("/{userId}/remove")
    public Cart removeItem(@PathVariable Long userId,
                           @RequestParam Long itemId) {
        return service.removeItem(userId, itemId);
    }

    @DeleteMapping("/{userId}/clear")
    public Cart clearCart(@PathVariable Long userId) {
        return service.clearCart(userId);
    }
}
