package com.supermarket.supermarket_system.models;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Convert(converter = ItemsMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<Long, Integer> items = new HashMap<>(); // itemId -> quantity

    public Cart() {}

    public Cart(User user) {
        this.user = user;
        this.items = new HashMap<>();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Map<Long, Integer> getItems() {
        return items;
    }
    public void setItems(Map<Long, Integer> items) {
        this.items = items;
    }

    // Calculated total - requires ItemRepository to fetch prices
    public Double getTotal(Map<Long, Double> itemPrices) {
        return items.entrySet().stream()
                .mapToDouble(entry -> {
                    Long itemId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Double price = itemPrices.getOrDefault(itemId, 0.0);
                    return price * quantity;
                })
                .sum();
    }

    // Helper methods
    public void addItem(Long itemId, int quantity) {
        items.put(itemId, items.getOrDefault(itemId, 0) + quantity);
    }

    public void removeItem(Long itemId) {
        items.remove(itemId);
    }

    public void updateItemQuantity(Long itemId, int quantity) {
        if (quantity <= 0) {
            items.remove(itemId);
        } else {
            items.put(itemId, quantity);
        }
    }

    public void clearCart() {
        items.clear();
    }
}