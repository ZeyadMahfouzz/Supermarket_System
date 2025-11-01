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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("No order found with this id: " + orderId));
        enrichOrderWithItemDetails(order);
        return order;
    }

    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status);
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    public List<Order> getUserOrdersByStatus(Long userId, String status) {
        List<Order> orders = orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status);
        orders.forEach(this::enrichOrderWithItemDetails);
        return orders;
    }

    private void enrichOrderWithItemDetails(Order order) {
        Map<String, Object> itemDetails = new HashMap<>();
        double totalAmount = 0.0;

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

