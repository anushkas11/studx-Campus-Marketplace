package com.studx.service;

import com.studx.dto.FairPriceResponse;
import com.studx.entity.Category;
import com.studx.entity.ProductCondition;
import com.studx.exception.ResourceNotFoundException;
import com.studx.repository.CategoryRepository;
import com.studx.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FairPriceService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public FairPriceService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public FairPriceResponse getFairPriceInsights(Long categoryId, String conditionStr) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        ProductCondition condition = null;
        if (conditionStr != null && !conditionStr.isBlank()) {
            try {
                condition = ProductCondition.valueOf(conditionStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<Object[]> stats = productRepository.findPriceStatsByCategoryAndCondition(categoryId, condition);

        if (stats == null || stats.isEmpty() || stats.get(0)[3] == null || ((Number) stats.get(0)[3]).longValue() == 0) {
            return new FairPriceResponse(
                    categoryId,
                    category.getName(),
                    condition != null ? condition.name() : "ALL",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    "Insufficient campus sales data in " + category.getName() + " to calculate fair-price insight."
            );
        }

        Object[] row = stats.get(0);
        BigDecimal avgPrice = row[0] != null ? BigDecimal.valueOf(((Number) row[0]).doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal minPrice = row[1] != null ? BigDecimal.valueOf(((Number) row[1]).doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal maxPrice = row[2] != null ? BigDecimal.valueOf(((Number) row[2]).doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        long count = row[3] != null ? ((Number) row[3]).longValue() : 0;

        // Apply condition multiplier for fair estimation
        BigDecimal multiplier = BigDecimal.valueOf(1.0);
        if (condition != null) {
            multiplier = switch (condition) {
                case NEW -> BigDecimal.valueOf(1.10);
                case LIKE_NEW -> BigDecimal.valueOf(1.00);
                case GOOD -> BigDecimal.valueOf(0.85);
                case FAIR -> BigDecimal.valueOf(0.70);
            };
        }

        BigDecimal suggestedPrice = avgPrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

        String message = String.format(
                "Fair price range for %s (%s): ₹%s - ₹%s (Recommended: ₹%s) based on %d comparable campus listings.",
                category.getName(),
                condition != null ? condition.name() : "Overall",
                minPrice,
                maxPrice,
                suggestedPrice,
                count
        );

        return new FairPriceResponse(
                categoryId,
                category.getName(),
                condition != null ? condition.name() : "ALL",
                minPrice,
                maxPrice,
                avgPrice,
                suggestedPrice,
                count,
                message
        );
    }
}
