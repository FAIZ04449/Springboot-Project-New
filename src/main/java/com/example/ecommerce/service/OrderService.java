package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public OrderResponse createOrder(String userId) {
        // 1. Get cart items
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        double totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        // 2. Validate Stock & Calculate Total
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            totalAmount += product.getPrice() * cartItem.getQuantity();
        }

        // 3. Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");
        order = orderRepository.save(order);

        // 4. Create OrderItems and Update Stock
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId()).get();

            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // Create OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem = orderItemRepository.save(orderItem);

            orderItems.add(orderItem);
        }

        // 5. Clear Cart
        cartRepository.deleteByUserId(userId);

        // 6. Return Response
        return mapToResponse(order, orderItems, null);
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Payment payment = paymentRepository.findByOrderId(orderId);

        return mapToResponse(order, items, payment);
    }

    private OrderResponse mapToResponse(Order order, List<OrderItem> items, Payment payment) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(items);
        response.setPayment(payment);
        return response;
    }
}
