package com.supermarket.supermarket_system.repositories;

import com.supermarket.supermarket_system.models.Order; // The JPA entity we want to manage
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data interface for DB operations
import org.springframework.stereotype.Repository; // Marks this as a Spring-managed bean

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
//    Order findByTitle(String title); //i copied this from the lab thing and have no idea what it does so ill keep it here commented out unless we need it
}