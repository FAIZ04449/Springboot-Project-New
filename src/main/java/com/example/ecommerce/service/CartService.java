package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public CartItem addToCart(AddToCartRequest request) {
        // Validate Product
        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found");
        }
        Product product = productOpt.get();

        // Check Stock
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        // Check if item already in cart (Implementation detail: Assumes separate
        // entries for simplicity or update logic)
        // Assignment diagram says: Item in cart? Yes -> Update quantity.
        // But for simplicity, let's just create a new entry or update if we implement
        // logic.
        // Let's implement update logic to be safe.
        List<CartItem> userCart = cartRepository.findByUserId(request.getUserId());
        Optional<CartItem> existingItem = userCart.stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(request.getUserId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            return cartRepository.save(newItem);
        }
    }

    public List<CartItemResponse> getUserCart(String userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);
        List<CartItemResponse> response = new ArrayList<>();

        for (CartItem item : items) {
            CartItemResponse dto = new CartItemResponse();
            dto.setId(item.getId());
            dto.setProductId(item.getProductId());
            dto.setQuantity(item.getQuantity());

            // Fetch product details
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            dto.setProduct(product);

            response.add(dto);
        }
        return response;
    }

    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
