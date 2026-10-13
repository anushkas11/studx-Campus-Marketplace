package com.studx.controller;

import com.studx.dto.ApiResponse;
import com.studx.dto.ProductCreateRequest;
import com.studx.dto.ProductResponse;
import com.studx.dto.ProductUpdateRequest;
import com.studx.entity.ProductCondition;
import com.studx.entity.ProductStatus;
import com.studx.security.UserPrincipal;
import com.studx.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Products", description = "Endpoints for product listings, deals, search, and reservation")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/featured-products")
    @Operation(summary = "Get featured products for homepage carousel and highlights")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/deals")
    @Operation(summary = "Get best deals on campus")
    public ResponseEntity<List<ProductResponse>> getBestDeals() {
        return ResponseEntity.ok(productService.getBestDeals());
    }

    @GetMapping({"/product/{id}", "/products/{id}"})
    @Operation(summary = "Get product details by ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/products")
    @Operation(summary = "Create a new product listing (supports both authenticated user and seller info)")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        ProductResponse response = productService.createProduct(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"/products", "/products/search"})
    @Operation(summary = "Search and filter products with pagination and sorting")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        ProductCondition conditionEnum = null;
        if (condition != null && !condition.isBlank()) {
            try {
                conditionEnum = ProductCondition.valueOf(condition.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        ProductStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<ProductResponse> results = productService.searchProducts(query, categoryId, conditionEnum, minPrice, maxPrice, statusEnum, pageable);
        return ResponseEntity.ok(results);
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update an existing product listing (Owner or Admin)")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductUpdateRequest request,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        ProductResponse response = productService.updateProduct(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete a product listing (Owner or Admin)")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        productService.deleteProduct(id, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
    }

    @PostMapping("/products/{id}/reserve")
    @Operation(summary = "Reserve a product (concurrency safe with audit tracking)")
    public ResponseEntity<ProductResponse> reserveProduct(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        ProductResponse response = productService.reserveProduct(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
