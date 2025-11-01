package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.Item;
import com.supermarket.supermarket_system.models.User;
import com.supermarket.supermarket_system.repositories.CartRepository;
import com.supermarket.supermarket_system.repositories.ItemRepository;
import com.supermarket.supermarket_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;

    @Autowired
    public CartService(CartRepository cartRepo, UserRepository userRepo, ItemRepository itemRepo) {
        this.cartRepo = cartRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
    }

    /**
     * Validates that the authenticated user matches the userId
     * or is an admin
     */
    private void validateUserAccess(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return; // Admins can access any cart
        }

        // Get email from JWT token
        String tokenEmail = auth.getName();

        // Get user from database
        Optional<User> targetUser = userRepo.findById(userId);

        if (targetUser.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }

        // Compare emails
        if (!tokenEmail.equals(targetUser.get().getEmail())) {
            throw new AccessDeniedException("You can only access your own cart");
        }
    }

    public Cart getCart(Long userId) {
        validateUserAccess(userId);

        return cartRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            Cart cart = new Cart(user);
            return cartRepo.save(cart);
        });
    }

    public Cart addItemToCart(Long userId, Long itemId, int quantity) {
        validateUserAccess(userId);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Cart cart = getCartWithoutValidation(userId);

        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));

        // Get current quantity in cart (if any)
        int currentQuantity = cart.getItems().getOrDefault(itemId, 0);
        int newTotalQuantity = currentQuantity + quantity;

        // Validate against available stock
        if (newTotalQuantity > item.getQuantity()) {
            throw new IllegalArgumentException(
                    "Cannot add " + quantity + " items. Only " +
                            (item.getQuantity() - currentQuantity) + " available in stock"
            );
        }

        cart.addItem(itemId, quantity);
        cart.setTotalPrice(calculateTotal(cart));
        return cartRepo.save(cart);
    }

    public Cart updateItemQuantity(Long userId, Long itemId, int quantity) {
        validateUserAccess(userId);

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (quantity == 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getCartWithoutValidation(userId);

        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));

        // Validate the new quantity against available stock
        if (quantity > item.getQuantity()) {
            throw new IllegalArgumentException(
                    "Cannot set quantity to " + quantity + ". Only " +
                            item.getQuantity() + " available in stock"
            );
        }

        cart.updateItemQuantity(itemId, quantity);
        cart.setTotalPrice(calculateTotal(cart));
        return cartRepo.save(cart);
    }

    public Cart removeItem(Long userId, Long itemId) {
        validateUserAccess(userId);

        Cart cart = getCartWithoutValidation(userId);

        if (!cart.getItems().containsKey(itemId)) {
            throw new NoSuchElementException("Item not found in cart");
        }

        cart.removeItem(itemId);
        cart.setTotalPrice(calculateTotal(cart));
        return cartRepo.save(cart);
    }

    public Cart clearCart(Long userId) {
        validateUserAccess(userId);

        Cart cart = getCartWithoutValidation(userId);
        cart.clearCart();
        return cartRepo.save(cart);
    }

    /**
     * Internal method to get cart without validation
     * Used after validation has already been performed
     */
    private Cart getCartWithoutValidation(Long userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            Cart cart = new Cart(user);
            return cartRepo.save(cart);
        });
    }

    private double calculateTotal(Cart cart) {
        return cart.getItems().entrySet().stream()
                .mapToDouble(entry -> {
                    Long id = entry.getKey();
                    int qty = entry.getValue();
                    double price = itemRepo.findById(id)
                            .map(Item::getPrice)
                            .orElse(0.0);
                    return price * qty;
                })
                .sum();
    }
}