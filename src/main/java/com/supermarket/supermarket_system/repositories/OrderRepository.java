package com.supermarket.supermarket_system.repositories;

import com.supermarket.supermarket_system.models.Order; // The JPA entity we want to manage
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data interface for DB operations
import org.springframework.stereotype.Repository; // Marks this as a Spring-managed bean

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(long user);
}