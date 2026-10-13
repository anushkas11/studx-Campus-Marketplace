package com.studx.service;

import com.studx.dto.ProductResponse;
import com.studx.dto.UserProfileResponse;
import com.studx.entity.Product;
import com.studx.entity.ProductStatus;
import com.studx.entity.User;
import com.studx.exception.ResourceNotFoundException;
import com.studx.repository.ProductRepository;
import com.studx.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final UserService userService;

    public AdminService(ProductRepository productRepository,
                        UserRepository userRepository,
                        ProductService productService,
                        UserService userService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.userService = userService;
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse updateProductStatus(Long productId, ProductStatus status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        product.setStatus(status);
        Product saved = productRepository.save(product);
        return productService.toProductResponse(saved);
    }

    @Transactional
    public UserProfileResponse verifyUser(Long userId, boolean isVerified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setVerified(isVerified);
        User saved = userRepository.save(user);
        return userService.getUserProfile(saved.getId());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> userService.getUserProfile(u.getId()))
                .toList();
    }
}
