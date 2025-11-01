package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.Order;
import com.supermarket.supermarket_system.models.User;
import com.supermarket.supermarket_system.repositories.CartRepository;
import com.supermarket.supermarket_system.repositories.ItemRepository;
import com.supermarket.supermarket_system.repositories.OrderRepository;
import com.supermarket.supermarket_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Transactional
    public Order createOrderFromCart(Long userId, String paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found or empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Map<Long, Integer> orderItems = new HashMap<>(cart.getItems());

        Order order = new Order(user, orderItems);
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            order.setPaymentmethod(paymentMethod);
        }

        order = orderRepository.save(order);

        // Clear cart after order
        cart.setItems(new HashMap<>());
        cartRepository.save(cart);

        return order;
    }


    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    public List<Order> getUserOrdersByStatus(Long userId, String status) {
        return orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);

        // Validate status transitions if needed
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if ("DELIVERED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Add your business logic for valid status transitions
        // For example, can't go from DELIVERED back to SHIPPING
        if ("DELIVERED".equals(currentStatus) && !"DELIVERED".equals(newStatus)) {
            throw new RuntimeException("Cannot change status of delivered order");
        }
        if ("CANCELLED".equals(currentStatus)) {
            throw new RuntimeException("Cannot change status of cancelled order");
        }
    }
}

