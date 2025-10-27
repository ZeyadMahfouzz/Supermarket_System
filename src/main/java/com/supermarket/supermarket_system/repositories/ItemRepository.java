package com.supermarket.supermarket_system.repositories;

import com.supermarket.supermarket_system.models.Item; // The JPA entity we want to manage
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data interface for DB operations
import org.springframework.stereotype.Repository; // Marks this as a Spring-managed bean

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}