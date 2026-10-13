package com.studx.service;

import com.studx.dto.ProductCreateRequest;
import com.studx.dto.ProductResponse;
import com.studx.entity.*;
import com.studx.exception.ConflictException;
import com.studx.exception.UnauthorizedException;
import com.studx.repository.*;
import com.studx.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationAuditLogRepository auditLogRepository;

    private ProductService productService;

    private User seller;
    private User buyer;
    private Category category;
    private UserPrincipal sellerPrincipal;
    private UserPrincipal buyerPrincipal;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, userRepository, auditLogRepository);

        seller = new User("Seller Bob", "seller@college.edu", "passHash", "DU", "Delhi", Role.ROLE_USER);
        seller.setId(1L);

        buyer = new User("Buyer Alice", "buyer@college.edu", "passHash", "DU", "Delhi", Role.ROLE_USER);
        buyer.setId(2L);

        category = new Category("Laptops", "laptops", "/CategoryImages/Laptop.png");
        category.setId(10L);

        sellerPrincipal = new UserPrincipal(1L, "Seller Bob", "seller@college.edu", "pass", "DU", "Delhi", true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        buyerPrincipal = new UserPrincipal(2L, "Buyer Alice", "buyer@college.edu", "pass", "DU", "Delhi", true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testCreateProduct_shouldAssignCategoryAndSeller() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setTitle("ThinkPad T480");
        request.setDescription("Great coding laptop");
        request.setPrice(BigDecimal.valueOf(22000));
        request.setImage("https://image.url/laptop.jpg");
        request.setCategoryId(10L);
        request.setDetails("16GB RAM, 512GB SSD");

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        ProductResponse response = productService.createProduct(request, sellerPrincipal);

        assertNotNull(response);
        assertEquals("ThinkPad T480", response.getTitle());
        assertEquals(BigDecimal.valueOf(22000), response.getPrice());
        assertEquals("AVAILABLE", response.getStatus());
        assertEquals(2, response.getDetails().size());
        assertEquals(1L, response.getSeller().getId());
    }

    @Test
    void testReserveProduct_success() {
        Product product = new Product("MacBook Air", "M1 8GB", BigDecimal.valueOf(45000), "img.png", "dims",
                ProductCondition.LIKE_NEW, category, seller);
        product.setId(50L);
        product.setStatus(ProductStatus.AVAILABLE);

        when(productRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.reserveProduct(50L, buyerPrincipal);

        assertEquals("RESERVED", response.getStatus());
        verify(auditLogRepository).save(any(ReservationAuditLog.class));
    }

    @Test
    void testReserveProduct_alreadyReserved_shouldThrowConflictException() {
        Product product = new Product("MacBook Air", "M1 8GB", BigDecimal.valueOf(45000), "img.png", "dims",
                ProductCondition.LIKE_NEW, category, seller);
        product.setId(50L);
        product.setStatus(ProductStatus.RESERVED);

        when(productRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(product));

        assertThrows(ConflictException.class, () -> productService.reserveProduct(50L, buyerPrincipal));
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void testDeleteProduct_unauthorizedUser_shouldThrowUnauthorizedException() {
        Product product = new Product("Book", "Calculus", BigDecimal.valueOf(300), "img.png", "dims",
                ProductCondition.GOOD, category, seller);
        product.setId(30L);

        when(productRepository.findById(30L)).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedException.class, () -> productService.deleteProduct(30L, buyerPrincipal));
        verify(productRepository, never()).delete(any());
    }
}
