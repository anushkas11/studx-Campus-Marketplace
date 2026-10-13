package com.studx.controller;

import com.studx.dto.OfferResponse;
import com.studx.dto.ProductResponse;
import com.studx.dto.UserProfileResponse;
import com.studx.security.UserPrincipal;
import com.studx.service.OfferService;
import com.studx.service.ProductService;
import com.studx.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "User Profile", description = "Endpoints for user profile and personal history")
public class UserController {

    private final UserService userService;
    private final ProductService productService;
    private final OfferService offerService;

    public UserController(UserService userService,
                          ProductService productService,
                          OfferService offerService) {
        this.userService = userService;
        this.productService = productService;
        this.offerService = offerService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get currently authenticated user's profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileResponse profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/user/listings")
    @Operation(summary = "Get listings posted by current user")
    public ResponseEntity<List<ProductResponse>> getMyListings(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<ProductResponse> listings = productService.getProductsBySeller(currentUser.getId());
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/user/offers")
    @Operation(summary = "Get offers submitted by current user")
    public ResponseEntity<List<OfferResponse>> getMyOffers(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<OfferResponse> offers = offerService.getOffersByBuyer(currentUser);
        return ResponseEntity.ok(offers);
    }
}
