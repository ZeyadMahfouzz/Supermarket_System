package com.supermarket.supermarket_system.models;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double price;
    private int quantity;
    private String category;
    private String description;

    public Item() {}

    public Item(String name, Double price, int quantity, String category, String description) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.description = description;
    }

    // Primary key (ID)
    public Long getId() { return id; }

    // Title
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Price
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    // Quantity
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Category
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // Description
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }




}