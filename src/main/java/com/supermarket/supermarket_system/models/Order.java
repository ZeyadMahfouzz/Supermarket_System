package com.supermarket.supermarket_system.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Convert(converter = ItemsMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<Long, Integer> items = new HashMap<>(); // itemId -> quantity

    @Transient // Not persisted to database, used only for API responses
    private Map<String, Object> itemDetails = new HashMap<>(); // itemName -> {quantity, price, subtotal}

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private String status; // e.g., SHIPPING, COMPLETED, CANCELLED, SHIPPED

    @Column(nullable = false)
    private String paymentmethod; // e.g., CREDIT_CARD, PAYPAL, CASH_ON_DELIVERY

    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
        this.paymentmethod = "UNSPECIFIED";
    }


    public Order(User user, Map<Long, Integer> items) {
        this.user = user;
        this.items = items;
        this.orderDate = LocalDateTime.now();
        this.status = "SHIPPING";
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

    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentmethod() {
        return paymentmethod;
    }
    public void setPaymentmethod(String paymentmethod) {
        this.paymentmethod = paymentmethod;
    }

    public Map<String, Object> getItemDetails() {
        return itemDetails;
    }
    public void setItemDetails(Map<String, Object> itemDetails) {
        this.itemDetails = itemDetails;
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
}