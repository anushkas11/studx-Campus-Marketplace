package com.studx.controller;

import com.studx.dto.ApiResponse;
import com.studx.dto.ProductResponse;
import com.studx.dto.UserProfileResponse;
import com.studx.entity.ProductStatus;
import com.studx.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin Moderation", description = "Endpoints for administrators to moderate listings and users")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all registered users (Admin only)")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/verify")
    @Operation(summary = "Update user student verification status (Admin only)")
    public ResponseEntity<UserProfileResponse> verifyUser(@PathVariable Long id, @RequestParam boolean verified) {
        UserProfileResponse updated = adminService.verifyUser(id, verified);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Remove inappropriate product listing (Admin only)")
    public ResponseEntity<ApiResponse> removeProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse(true, "Listing removed by administrator"));
    }

    @PutMapping("/products/{id}/status")
    @Operation(summary = "Override product status (Admin only)")
    public ResponseEntity<ProductResponse> updateProductStatus(@PathVariable Long id, @RequestParam ProductStatus status) {
        ProductResponse updated = adminService.updateProductStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
