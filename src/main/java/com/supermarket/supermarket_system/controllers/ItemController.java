// ========================
// PACKAGE DECLARATION
// ========================
// This class belongs to the "controllers" package,
// where we expose REST endpoints for the outside world.
package com.supermarket.supermarket_system.controllers;
import com.supermarket.supermarket_system.models.Item;

// ========================
// IMPORTS
// ========================
import com.supermarket.supermarket_system.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ========================
// CONTROLLER CLASS
// ========================
// @RestController → Marks this class as a REST API controller.
// It combines @Controller + @ResponseBody, so all methods return JSON (not HTML).
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    // Create a new item
    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    // Get all items
    @GetMapping
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // Get a single item by ID
    @GetMapping("/{id}")
    public Item getItemsById(@PathVariable Long id) {
        return itemRepository.findById(id).orElse(null);
    }
}
