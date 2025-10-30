package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.Item;
import com.supermarket.supermarket_system.models.User;
import com.supermarket.supermarket_system.repositories.CartRepository;
import com.supermarket.supermarket_system.repositories.ItemRepository;
import com.supermarket.supermarket_system.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;

    public CartService(CartRepository cartRepo, UserRepository userRepo, ItemRepository itemRepo) {
        this.cartRepo = cartRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
    }

    public Cart getCart(Long userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            Cart cart = new Cart(user);
            return cartRepo.save(cart);
        });
    }

    public Cart addItemToCart(Long userId, Long itemId, int quantity) {
        Cart cart = getCart(userId);

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
        Cart cart = getCart(userId);

        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));

        // Validate the new quantity against available stock
        if (quantity > item.getQuantity()) {
            throw new IllegalArgumentException(
                    "Cannot set quantity to " + quantity + ". Only " +
                            item.getQuantity() + " available in stock"
            );
        }

        // Validate quantity is positive
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        cart.updateItemQuantity(itemId, quantity);
        cart.setTotalPrice(calculateTotal(cart));
        return cartRepo.save(cart);
    }

    public Cart removeItem(Long userId, Long itemId) {
        Cart cart = getCart(userId);

        if (!cart.getItems().containsKey(itemId)) {
            throw new NoSuchElementException("Item not found in cart");
        }

        cart.removeItem(itemId);
        cart.setTotalPrice(calculateTotal(cart));
        return cartRepo.save(cart);
    }

    public Cart clearCart(Long userId) {
        Cart cart = getCart(userId);
        cart.clearCart();
        return cartRepo.save(cart);
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