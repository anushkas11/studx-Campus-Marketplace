package com.studx.service;

import com.studx.dto.ProductResponse;
import com.studx.entity.Product;
import com.studx.entity.User;
import com.studx.entity.Wishlist;
import com.studx.exception.ResourceNotFoundException;
import com.studx.repository.ProductRepository;
import com.studx.repository.UserRepository;
import com.studx.repository.WishlistRepository;
import com.studx.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public WishlistService(WishlistRepository wishlistRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           ProductService productService) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @Transactional
    public void addToWishlist(Long productId, UserPrincipal currentUser) {
        if (wishlistRepository.existsByUserIdAndProductId(currentUser.getId(), productId)) {
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = new Wishlist(user, product);
        wishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeFromWishlist(Long productId, UserPrincipal currentUser) {
        wishlistRepository.deleteByUserIdAndProductId(currentUser.getId(), productId);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getUserWishlist(UserPrincipal currentUser) {
        return wishlistRepository.findByUserId(currentUser.getId()).stream()
                .map(w -> productService.toProductResponse(w.getProduct()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isProductInWishlist(Long productId, UserPrincipal currentUser) {
        return wishlistRepository.existsByUserIdAndProductId(currentUser.getId(), productId);
    }
}
