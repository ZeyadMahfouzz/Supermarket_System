package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.repositories.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepo;

    public CartService(CartRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    public Cart getCart(Long userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            return cartRepo.save(cart);
        });
    }

    public Cart addItemToCart(Long userId, Long itemId, int quantity) {
        Cart cart = getCart(userId);
        cart.addItem(itemId, quantity);
        return cartRepo.save(cart);
    }

    public Cart updateItemQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = getCart(userId);
        cart.updateItemQuantity(itemId, quantity);
        return cartRepo.save(cart);
    }

    public Cart removeItem(Long userId, Long itemId) {
        Cart cart = getCart(userId);
        cart.removeItem(itemId);
        return cartRepo.save(cart);
    }

    public Cart clearCart(Long userId) {
        Cart cart = getCart(userId);
        cart.clearCart();
        return cartRepo.save(cart);
    }
}

