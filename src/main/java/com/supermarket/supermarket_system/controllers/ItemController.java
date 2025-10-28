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


    // Delete an item
    @DeleteMapping("/{id}")
    public String deleteItem(@PathVariable Long id) {
        itemRepository.deleteById(id);
        return "Item deleted successfully!";
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

    // Modify existing item
    @PutMapping("/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item updatedItem) {
        return itemRepository.findById(id).map(item -> {
            if (updatedItem.getName() != null) {
                item.setName(updatedItem.getName());
            }
            if (updatedItem.getPrice() != null) {
                item.setPrice(updatedItem.getPrice());
            }
            if (updatedItem.getQuantity() != 0) {
                item.setQuantity(updatedItem.getQuantity());
            }
            if (updatedItem.getCategory() != null) {
                item.setCategory(updatedItem.getCategory());
            }
            if (updatedItem.getDescription() != null) {
                item.setDescription(updatedItem.getDescription());
            }
            return itemRepository.save(item);
        }).orElse(null);// If not found, return null
    }
}

