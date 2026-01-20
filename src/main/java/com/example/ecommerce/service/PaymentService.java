package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentWebhookRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RestTemplate restTemplate;

    public Payment createPayment(PaymentRequest request) {
        // 1. Validate Order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in CREATED state");
        }

        // 2. Create Payment (PENDING)
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus("PENDING");
        payment.setPaymentId("pay_mock_" + UUID.randomUUID().toString().substring(0, 8));
        payment = paymentRepository.save(payment);

        // 3. Simulate Mock Payment Service triggering Webhook (Async)
        simulateExternalPaymentProcess(request.getOrderId());

        return payment;
    }

    private void simulateExternalPaymentProcess(String orderId) {
        CompletableFuture.runAsync(() -> {
            try {
                // Wait 3 seconds
                Thread.sleep(3000);

                // Call Webhook
                String webhookUrl = "http://localhost:8080/api/webhooks/payment";
                PaymentWebhookRequest payload = new PaymentWebhookRequest();
                payload.setOrderId(orderId);
                payload.setStatus("SUCCESS");

                restTemplate.postForEntity(webhookUrl, payload, Void.class);
                System.out.println("Mock Webhook triggered for Order: " + orderId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void processWebhook(PaymentWebhookRequest request) {
        // 1. Update Payment
        Payment payment = paymentRepository.findByOrderId(request.getOrderId());
        if (payment != null) {
            payment.setStatus(request.getStatus());
            paymentRepository.save(payment);
        }

        // 2. Update Order
        Order order = orderRepository.findById(request.getOrderId()).orElse(null);
        if (order != null) {
            if ("SUCCESS".equals(request.getStatus())) {
                order.setStatus("PAID");
            } else {
                order.setStatus("FAILED");
            }
            orderRepository.save(order);
        }
    }
}
