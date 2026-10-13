package com.studx.controller;

import com.studx.dto.ApiResponse;
import com.studx.dto.ProductResponse;
import com.studx.security.UserPrincipal;
import com.studx.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist", description = "Endpoints for saved items")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add a product to wishlist")
    public ResponseEntity<ApiResponse> addToWishlist(@PathVariable Long productId,
                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        wishlistService.addToWishlist(productId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Product added to wishlist"));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove a product from wishlist")
    public ResponseEntity<ApiResponse> removeFromWishlist(@PathVariable Long productId,
                                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        wishlistService.removeFromWishlist(productId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Product removed from wishlist"));
    }

    @GetMapping
    @Operation(summary = "Get all items in the current user's wishlist")
    public ResponseEntity<List<ProductResponse>> getWishlist(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<ProductResponse> wishlist = wishlistService.getUserWishlist(currentUser);
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check if product is in current user's wishlist")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(@PathVariable Long productId,
                                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean inWishlist = wishlistService.isProductInWishlist(productId, currentUser);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }
}
