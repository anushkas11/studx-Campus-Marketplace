package com.studx.service;

import com.studx.dto.*;
import com.studx.entity.*;
import com.studx.exception.BadRequestException;
import com.studx.exception.ConflictException;
import com.studx.exception.ResourceNotFoundException;
import com.studx.exception.UnauthorizedException;
import com.studx.repository.*;
import com.studx.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReservationAuditLogRepository auditLogRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          ReservationAuditLogRepository auditLogRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndStatus(ProductStatus.AVAILABLE)
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getBestDeals() {
        return productRepository.findByIsDealTrueAndStatus(ProductStatus.AVAILABLE)
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword,
                                                Long categoryId,
                                                ProductCondition condition,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                ProductStatus status,
                                                Pageable pageable) {
        ProductStatus searchStatus = (status != null) ? status : ProductStatus.AVAILABLE;
        return productRepository.searchProducts(keyword, categoryId, condition, minPrice, maxPrice, searchStatus, pageable)
                .map(this::toProductResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, UserPrincipal currentUser) {
        // 1. Resolve Category
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }
        if (category == null && request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            category = categoryRepository.findByNameIgnoreCase(request.getCategoryName().trim()).orElse(null);
        }
        if (category == null) {
            // Default to first category available
            category = categoryRepository.findAll().stream().findFirst()
                    .orElseGet(() -> categoryRepository.save(new Category("General", "general", "/CategoryImages/Books.png")));
        }

        // 2. Resolve Seller
        User seller;
        if (currentUser != null) {
            seller = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } else if (request.getSeller() != null && request.getSeller().getEmail() != null) {
            String sellerEmail = request.getSeller().getEmail().trim().toLowerCase();
            seller = userRepository.findByEmail(sellerEmail)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setName(request.getSeller().getName() != null ? request.getSeller().getName() : "Student Seller");
                        newUser.setEmail(sellerEmail);
                        newUser.setPasswordHash("$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a"); // Default hash
                        newUser.setCollege(request.getSeller().getCollege());
                        newUser.setCity(request.getSeller().getCity());
                        newUser.setRole(Role.ROLE_USER);
                        newUser.setVerified(false);
                        return userRepository.save(newUser);
                    });
        } else {
            // Default fallback seller
            seller = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BadRequestException("No seller found. Please register or log in."));
        }

        // 3. Condition Grade
        ProductCondition condition = ProductCondition.GOOD;
        if (request.getConditionGrade() != null) {
            try {
                condition = ProductCondition.valueOf(request.getConditionGrade().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        // 4. Construct Product
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImage());
        product.setDimensions(request.getDimensions());
        product.setConditionGrade(condition);
        product.setStatus(ProductStatus.AVAILABLE);
        product.setCategory(category);
        product.setSeller(seller);
        product.setFeatured(true); // Default to true so newly listed items appear on Explore/All Products

        // 5. Parse details / features
        parseAndSetFeatures(product, request.getDetails());

        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request, UserPrincipal currentUser) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        verifyOwnership(product, currentUser);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            product.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            product.setPrice(request.getPrice());
        }
        if (request.getImage() != null && !request.getImage().isBlank()) {
            product.setImageUrl(request.getImage());
        }
        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }
        if (request.getConditionGrade() != null) {
            try {
                product.setConditionGrade(ProductCondition.valueOf(request.getConditionGrade().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (request.getStatus() != null) {
            try {
                product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).ifPresent(product::setCategory);
        }
        if (request.getDetails() != null) {
            product.clearFeatures();
            parseAndSetFeatures(product, request.getDetails());
        }

        Product updated = productRepository.save(product);
        return toProductResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id, UserPrincipal currentUser) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        verifyOwnership(product, currentUser);
        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse reserveProduct(Long productId, UserPrincipal currentUser) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new ConflictException("Product is already " + product.getStatus() + " and cannot be reserved.");
        }

        User buyer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new BadRequestException("You cannot reserve your own listed product.");
        }

        String prevStatus = product.getStatus().name();
        product.setStatus(ProductStatus.RESERVED);

        ReservationAuditLog auditLog = new ReservationAuditLog(
                product,
                buyer,
                prevStatus,
                ProductStatus.RESERVED.name(),
                "Direct buyer reservation"
        );
        auditLogRepository.save(auditLog);

        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    private void verifyOwnership(Product product, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!product.getSeller().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to modify or delete this product listing.");
        }
    }

    private void parseAndSetFeatures(Product product, Object detailsObj) {
        if (detailsObj == null) {
            return;
        }

        List<String> items = new ArrayList<>();
        if (detailsObj instanceof List<?> list) {
            for (Object obj : list) {
                if (obj != null && !obj.toString().isBlank()) {
                    items.add(obj.toString().trim());
                }
            }
        } else if (detailsObj instanceof String str && !str.isBlank()) {
            String[] split = str.split("[,\\n]+");
            for (String s : split) {
                if (!s.trim().isBlank()) {
                    items.add(s.trim());
                }
            }
        }

        int order = 1;
        for (String item : items) {
            product.addFeature(item, order++);
        }
    }

    public ProductResponse toProductResponse(Product product) {
        ProductResponse res = new ProductResponse();
        res.setId(product.getId());
        res.setTitle(product.getTitle());
        res.setDescription(product.getDescription());
        res.setPrice(product.getPrice());
        res.setImage(product.getImageUrl());
        res.setImageUrl(product.getImageUrl());
        res.setDimensions(product.getDimensions());
        res.setConditionGrade(product.getConditionGrade().name());
        res.setStatus(product.getStatus().name());
        res.setFeatured(product.isFeatured());
        res.setDeal(product.isDeal());
        res.setCreatedAt(product.getCreatedAt());

        if (product.getCategory() != null) {
            res.setCategoryId(product.getCategory().getId());
            res.setCategoryName(product.getCategory().getName());
        }

        if (product.getSeller() != null) {
            User seller = product.getSeller();
            SellerDto sellerDto = new SellerDto(
                    seller.getId(),
                    seller.getName(),
                    seller.getEmail(),
                    seller.getCity(),
                    seller.getCollege(),
                    seller.getCreatedAt()
            );
            res.setSeller(sellerDto);
        }

        if (product.getFeatures() != null) {
            List<String> features = product.getFeatures().stream()
                    .map(ProductFeature::getFeatureText)
                    .toList();
            res.setDetails(features);
        }

        return res;
    }
}
