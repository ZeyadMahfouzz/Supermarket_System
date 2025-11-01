package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.models.Cart;
import com.supermarket.supermarket_system.models.Item;
import com.supermarket.supermarket_system.models.Order;
import com.supermarket.supermarket_system.models.User;
import com.supermarket.supermarket_system.repositories.CartRepository;
import com.supermarket.supermarket_system.repositories.ItemRepository;
import com.supermarket.supermarket_system.repositories.OrderRepository;
import com.supermarket.supermarket_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Validates that the authenticated user matches the userId or is an admin
     */
    private void validateUserAccess(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        // Check if user is admin
        if (isAdmin(auth)) {
            return; // Admins can access anything
        }

        // Get email from JWT token
        String tokenEmail = auth.getName();

        // Get user from database
        Optional<User> targetUser = userRepository.findById(userId);

        if (targetUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Compare emails
        if (!tokenEmail.equals(targetUser.get().getEmail())) {
            throw new AccessDeniedException("You can only access your own orders");
        }
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Transactional
    public Order createOrderFromCart(Long userId, String paymentMethod) {
        validateUserAccess(userId);

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found or empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Map<Long, Integer> orderItems = new HashMap<>(cart.getItems());

        // Check stock availability and decrease quantities
        for (Map.Entry<Long, Integer> entry : orderItems.entrySet()) {
            Long itemId = entry.getKey();
            Integer quantityOrdered = entry.getValue();

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));

            if (item.getQuantity() < quantityOrdered) {
                throw new RuntimeException("Insufficient stock for item: " + item.getName() +
                        ". Available: " + item.getQuantity() + ", Requested: " + quantityOrdered);
            }

            // Decrease item quantity
            item.setQuantity(item.getQuantity() - quantityOrdered);
            itemRepository.save(item);
        }

        Order order = new Order(user, orderItems);
        order.setPaymentmethod(paymentMethod);
        order = orderRepository.save(order);

        // Clear cart after order
        cart.setItems(new HashMap<>());
        cart.setTotalPrice(0);
        cartRepository.save(cart);

        return order;
    }

    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("No order found with this id: " + orderId));

        // Validate user can access this order
        validateUserAccess(order.getUser().getId());

        enrichOrderWithItemDetails(order);
        return order;
    }

    public List<Order> getUserOrders(Long userId) {
        validateUserAccess(userId);

        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    public List<Order> getAllOrders() {
        // Only admins can access - will be enforced by SecurityConfig
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        // Only admins can access - will be enforced by SecurityConfig
        List<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status);
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    public List<Order> getUserOrdersByStatus(Long userId, String status) {
        validateUserAccess(userId);

        List<Order> orders = orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status);
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    private void enrichOrderWithItemDetails(Order order) {
        Map<String, Object> itemDetails = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : order.getItems().entrySet()) {
            Long itemId = entry.getKey();
            Integer quantity = entry.getValue();

            itemRepository.findById(itemId).ifPresent(item -> {
                Map<String, Object> details = new HashMap<>();
                details.put("itemId", item.getId());
                details.put("quantity", quantity);
                details.put("price", item.getPrice());
                details.put("subtotal", item.getPrice() * quantity);

                itemDetails.put(item.getName(), details);
            });
        }

        order.setItemDetails(itemDetails);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        // Only admins can update status - enforced by SecurityConfig

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("No order found with this id: " + orderId));

        // Validate status transitions
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("No order found with this id: " + orderId));

        // Validate user can cancel this order
        validateUserAccess(order.getUser().getId());

        if ("DELIVERED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }

        // Restore item quantities back to inventory
        Map<Long, Integer> orderItems = order.getItems();
        for (Map.Entry<Long, Integer> entry : orderItems.entrySet()) {
            Long itemId = entry.getKey();
            Integer quantityOrdered = entry.getValue();

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));

            // Restore quantity
            item.setQuantity(item.getQuantity() + quantityOrdered);
            itemRepository.save(item);
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if ("DELIVERED".equals(currentStatus) && !"DELIVERED".equals(newStatus)) {
            throw new RuntimeException("Cannot change status of delivered order");
        }
        if ("CANCELLED".equals(currentStatus)) {
            throw new RuntimeException("Cannot change status of cancelled order");
        }
    }
}