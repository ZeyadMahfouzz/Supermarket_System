package com.supermarket.supermarket_system.repositories;

import com.supermarket.supermarket_system.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Find orders by user ID, sorted by date (newest first)
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    // Find orders by status
    List<Order> findByStatusOrderByOrderDateDesc(String status);

    // Find orders by user ID and status
    List<Order> findByUserIdAndStatusOrderByOrderDateDesc(Long userId, String status);

    // Find all orders sorted by date (newest first)
    List<Order> findAllByOrderByOrderDateDesc();
}
